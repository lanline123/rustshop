package com.tigshop.service.promotion.impl;

import cn.hutool.core.bean.BeanUtil;
import com.tigshop.bean.model.promotion.LotteryActivity;
import com.tigshop.bean.model.promotion.LotteryPrize;
import com.tigshop.bean.model.promotion.LotteryUserStat;
import com.tigshop.bean.model.promotion.lottery.GridPosition;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.user.UserMessage;
import com.tigshop.bean.vo.promotion.GridPositionVO;
import com.tigshop.bean.vo.promotion.LotteryActivityInfoVO;
import com.tigshop.bean.vo.promotion.LotteryResultVO;
import com.tigshop.bean.dto.promotion.ProductPrizeValue;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tigshop.service.promotion.*;
import com.tigshop.service.user.UserMessageService;
import com.tigshop.service.user.UserPointsLogService;
import com.tigshop.service.user.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 抽奖核心ServiceImpl
 *
 * @author Tigshop团队
 * &#064;create  2024年12月12日
 */
@Service
@Slf4j
public class LotteryDrawServiceImpl implements ILotteryDrawService {

    @Resource
    private ILotteryActivityService lotteryActivityService;

    @Resource
    private ILotteryPrizeService lotteryPrizeService;

    @Resource
    private ILotteryRecordService lotteryRecordService;

    @Resource
    private ILotteryUserStatService lotteryUserStatService;

    @Resource
    private UserService userService;

    @Resource
    private UserPointsLogService userPointsLogService;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private ProductService productService;

    @Resource
    private ProductSkuService productSkuService;

    /**
     * 解析商品奖品的prizeValue
     * 格式：{"productId":11715} 或 {"productId":11715,"skuId":1195}
     */
    private ProductPrizeValue parseProductPrizeValue(String prizeValue) {
        try {
            JSONObject json = JSONUtil.parseObj(prizeValue);
            Integer productId = json.getInt("productId");
            Integer skuId = json.getInt("skuId");
            return new ProductPrizeValue(productId, skuId);
        } catch (Exception e) {
            log.error("解析商品奖品值失败,prizeValue:{}", prizeValue, e);
            return null;
        }
    }

    @Override
    public LotteryActivityInfoVO getActivityInfo(Integer activityId, Integer userId) {
        // 查询活动信息
        LotteryActivity activity = lotteryActivityService.getById(activityId);
        validateActivity(activity);
        // 转换VO
        LotteryActivityInfoVO vo = new LotteryActivityInfoVO();
        BeanUtil.copyProperties(activity, vo, "startTime", "endTime", "gridConfig");
        // 时间戳转字符串(由@JsonSerialize注解自动格式化)
        vo.setStartTime(String.valueOf(activity.getStartTime()));
        vo.setEndTime(String.valueOf(activity.getEndTime()));

        // 构建九宫格配置(填充奖品详情)
        vo.setGridConfig(buildGridPositionVO(activity));

        // 查询用户积分
        User user = userService.getById(userId);
        int userPoints = user.getPoints() != null ? user.getPoints() : 0;
        vo.setUserPoints(userPoints);

        // 计算用户积分可抽奖次数(免费模式不受积分限制,返回-1表示无限制)
        if (isNeedCostPoints(activity)) {
            int costPoints = activity.getCostPoints() != null && activity.getCostPoints() > 0
                    ? activity.getCostPoints() : 1;
            vo.setAvailableDrawCount(userPoints / costPoints);
        } else {
            vo.setAvailableDrawCount(-1);
        }

        // 查询用户统计,计算剩余次数
        LotteryUserStat stat = lotteryUserStatService.getStat(activityId, userId);

        // 今日剩余次数（0表示不限制，返回-1表示无限制）
        int todayRemaining = activity.getDailyLimit() > 0 
                ? Math.max(0, activity.getDailyLimit() - stat.getTodayCount())
                : -1;

        // 总剩余次数（0表示不限制，返回-1表示无限制）
        int totalRemaining = activity.getTotalLimit() > 0 
                ? Math.max(0, activity.getTotalLimit() - stat.getTotalCount())
                : -1;

        // 可中奖剩余次数（0表示不限制，返回-1表示无限制）
        int winRemaining = activity.getWinLimit() > 0 
                ? Math.max(0, activity.getWinLimit() - stat.getWinCount())
                : -1;

        vo.setTodayRemaining(todayRemaining);
        vo.setTotalRemaining(totalRemaining);
        vo.setWinRemaining(winRemaining);

        return vo;
    }

    /**
     * 构建九宫格配置VO(填充奖品详情)
     */
    private List<GridPositionVO> buildGridPositionVO(LotteryActivity activity) {
        List<GridPosition> gridConfig = activity.getGridConfig();
        if (gridConfig == null || gridConfig.isEmpty()) {
            return List.of();
        }

        return gridConfig.stream().map(grid -> {
            GridPositionVO vo = new GridPositionVO();
            vo.setPosition(grid.getPosition());
            vo.setPrizeId(grid.getPrizeId());
            vo.setProbability(grid.getProbability());

            if (grid.getPrizeId() != null) {
                // 从奖品库查询奖品信息
                LotteryPrize prize = lotteryPrizeService.getPrizeById(grid.getPrizeId());
                if (prize != null) {
                    vo.setPrizeName(prize.getPrizeName());
                    vo.setPrizeImage(prize.getPrizeImage());
                    vo.setPrizeType(prize.getPrizeType());
                    vo.setPrizeValue(prize.getPrizeValue());
                } else {
                    // 奖品不存在，显示未中奖
                    vo.setPrizeName(activity.getNoWinName());
                    vo.setPrizeImage(activity.getNoWinImage());
                    vo.setPrizeType(0);
                }
            } else {
                // 未选择奖品，显示未中奖
                vo.setPrizeName(activity.getNoWinName());
                vo.setPrizeImage(activity.getNoWinImage());
                vo.setPrizeType(0);
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LotteryResultVO drawLottery(Integer activityId, Integer userId) {
        // 1. 校验活动状态
        LotteryActivity activity = lotteryActivityService.getById(activityId);
        validateActivity(activity);

        // 2. 校验用户资格
        LotteryUserStat stat = lotteryUserStatService.getStat(activityId, userId);
        validateUserQualification(activity, stat);

        // 3. 校验用户积分(仅消耗积分模式校验,免费模式跳过)
        boolean needCostPoints = isNeedCostPoints(activity);
        if (needCostPoints) {
            User user = userService.getById(userId);
            int userPoints = user.getPoints() != null ? user.getPoints() : 0;
            if (userPoints < activity.getCostPoints()) {
                throw new GlobalException("积分不足,无法参与抽奖");
            }
        }

        // 4. 执行抽奖算法(基于gridConfig)
        DrawResult drawResult = executeLotteryAlgorithm(activity);

        // 5. 处理中奖结果
        boolean isWin = drawResult.isWin;
        Integer prizeId = drawResult.prizeId;
        Integer position = drawResult.position;
        LotteryPrize prize = null;

        if (isWin && prizeId != null) {
            prize = lotteryPrizeService.getPrizeById(prizeId);
            if (prize != null) {
                // 扣减奖品库存
                boolean success = lotteryPrizeService.deductStock(prizeId);
                if (!success) {
                    // 库存不足,改为未中奖
                    log.warn("奖品库存不足,改为未中奖,奖品ID:{}", prizeId);
                    isWin = false;
                    prizeId = null;
                    prize = null;
                } else {
                    // 商品奖品(prizeType=2)需要扣减商品库存
                    if (prize.getPrizeType() == 2) {
                        // 解析prizeValue，支持格式：{"productId":11715} 或 {"productId":11715,"skuId":1195}
                        ProductPrizeValue prizeValue = parseProductPrizeValue(prize.getPrizeValue());
                        if (prizeValue != null && prizeValue.getProductId() != null) {
                            String stockDesc = "抽奖活动中奖扣减库存:" + activity.getActivityName();
                            if (prizeValue.hasSkuId()) {
                                // 多规格商品，扣减SKU库存
                                productSkuService.decStock(prizeValue.getSkuId(), 1, stockDesc);
                                log.info("扣减商品SKU库存成功,用户:{},skuId:{},奖品:{}", userId, prizeValue.getSkuId(), prize.getPrizeName());
                            } else {
                                // 单规格商品，扣减商品库存
                                productService.decStock(prizeValue.getProductId(), 1, stockDesc);
                                log.info("扣减商品库存成功,用户:{},商品ID:{},奖品:{}", userId, prizeValue.getProductId(), prize.getPrizeName());
                            }
                        }
                    }
                    // 发放奖品(积分和优惠券自动发放,商品需要用户确认)
                    lotteryPrizeService.grantPrize(userId, prize);
                    // 发送中奖站内信通知
                    sendWinMessage(userId, activity, prize);
                }
            } else {
                // 奖品不存在,改为未中奖
                isWin = false;
                prizeId = null;
            }
        }

        // 6. 记录抽奖记录(免费模式消耗积分记为0)
        int actualCostPoints = needCostPoints && activity.getCostPoints() != null
                ? activity.getCostPoints() : 0;
        Integer recordId = lotteryRecordService.saveRecord(activityId, userId,
                prizeId != null ? prizeId : 0, actualCostPoints, isWin);

        // 7. 扣减积分并记录日志(仅消耗积分模式)
        if (needCostPoints && actualCostPoints > 0) {
            userPointsLogService.subPoints(
                    actualCostPoints,
                    userId,
                    "参与抽奖活动:" + activity.getActivityName(),
                    activity.getActivityId()
            );
        }

        // 8. 更新用户统计
        lotteryUserStatService.updateStat(activityId, userId, isWin);

        // 9. 返回结果
        return buildResult(isWin, prize, activity, stat, position, recordId);
    }

    /**
     * 抽奖结果内部类
     */
    private static class DrawResult {
        boolean isWin;
        Integer prizeId;
        Integer position;

        DrawResult(boolean isWin, Integer prizeId, Integer position) {
            this.isWin = isWin;
            this.prizeId = prizeId;
            this.position = position;
        }
    }

    /**
     * 是否需要消耗积分
     * costType: 0-无限制(免费),1-消耗积分;为空时按消耗积分处理(兼容旧数据)
     */
    private boolean isNeedCostPoints(LotteryActivity activity) {
        Integer costType = activity.getCostType();
        return costType == null || costType != 0;
    }

    /**
     * 校验活动状态
     */
    private void validateActivity(LotteryActivity activity) {
        if (activity == null || activity.getIsDelete() == 1) {
            throw new GlobalException("活动不存在");
        }
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime < activity.getStartTime()) {
            throw new GlobalException("活动未开始");
        }
        if (currentTime > activity.getEndTime()) {
            throw new GlobalException("活动已结束");
        }
    }

    /**
     * 校验用户资格
     */
    private void validateUserQualification(LotteryActivity activity, LotteryUserStat stat) {
        // 今日次数限制
        if (activity.getDailyLimit() > 0 && stat.getTodayCount() >= activity.getDailyLimit()) {
            throw new GlobalException("今日抽奖次数已用完");
        }

        // 总次数限制
        if (activity.getTotalLimit() > 0 && stat.getTotalCount() >= activity.getTotalLimit()) {
            throw new GlobalException("抽奖次数已用完");
        }

        // 中奖次数限制
        if (activity.getWinLimit() > 0 && stat.getWinCount() >= activity.getWinLimit()) {
            throw new GlobalException("中奖次数已达上限");
        }
    }

    /**
     * 执行抽奖算法(基于gridConfig加权随机)
     * 概率支持4位小数，使用BigDecimal进行精确计算
     * 商品奖品(prizeType=2)需要检查商品库存，库存为0时概率为0
     * 支持多规格商品：{"productId":11715,"skuId":1195}，检查SKU库存
     */
    private DrawResult executeLotteryAlgorithm(LotteryActivity activity) {
        List<GridPosition> gridConfig = activity.getGridConfig();
        if (gridConfig == null || gridConfig.isEmpty()) {
            return new DrawResult(false, null, 1);
        }

        // 构建有效的抽奖项(有奖品且有库存的位置)
        List<GridPosition> availablePositions = new ArrayList<>();
        BigDecimal totalProbability = BigDecimal.ZERO;

        for (GridPosition grid : gridConfig) {
            if (grid.getPrizeId() != null && grid.getProbability() != null 
                    && grid.getProbability().compareTo(BigDecimal.ZERO) > 0) {
                // 检查奖品库存
                LotteryPrize prize = lotteryPrizeService.getPrizeById(grid.getPrizeId());
                if (prize != null && prize.getPrizeStock() > prize.getPrizeStockUsed()) {
                    // 商品奖品(prizeType=2)需要额外检查商品库存
                    if (prize.getPrizeType() == 2) {
                        // 解析prizeValue，支持格式：{"productId":11715} 或 {"productId":11715,"skuId":1195}
                        ProductPrizeValue prizeValue = parseProductPrizeValue(prize.getPrizeValue());
                        if (prizeValue == null || prizeValue.getProductId() == null) {
                            log.warn("商品奖品值解析失败，不参与抽奖，奖品ID:{}", prize.getPrizeId());
                            continue;
                        }

                        // 检查库存
                        if (prizeValue.hasSkuId()) {
                            // 多规格商品，检查SKU库存
                            ProductSku sku = productSkuService.getById(prizeValue.getSkuId());
                            if (sku == null || Constants.NO.equals(sku.getIsEnabled())
                                    || sku.getSkuStock() == null || sku.getSkuStock() <= 0) {
                                log.info("商品奖品SKU库存不足，不参与抽奖，奖品ID:{}, skuId:{}", prize.getPrizeId(), prizeValue.getSkuId());
                                continue;
                            }
                        } else {
                            // 单规格商品，检查商品库存
                            Product product = productService.getById(prizeValue.getProductId());
                            if (product == null || product.getProductStock() == null || product.getProductStock() <= 0) {
                                log.info("商品奖品库存不足，不参与抽奖，奖品ID:{}, 商品ID:{}", prize.getPrizeId(), prizeValue.getProductId());
                                continue;
                            }
                        }
                    }
                    availablePositions.add(grid);
                    totalProbability = totalProbability.add(grid.getProbability());
                }
            }
        }

        // 如果没有可用奖品,直接返回未中奖
        if (availablePositions.isEmpty() || totalProbability.compareTo(BigDecimal.ZERO) == 0) {
            // 找一个未配置奖品的位置作为未中奖位置
            Integer noWinPosition = findNoWinPosition(gridConfig);
            return new DrawResult(false, null, noWinPosition);
        }

        // 生成随机数(0-100)，精度到4位小数（0-1000000整数后除以10000）
        Random random = new Random();
        // 生成0到999999的随机整数，然后转换为0.0000到99.9999的BigDecimal
        int randomInt = random.nextInt(1000000);
        BigDecimal randomNum = new BigDecimal(randomInt).divide(new BigDecimal("10000"));

        // 判断中奖
        BigDecimal currentWeight = BigDecimal.ZERO;
        for (GridPosition grid : availablePositions) {
            currentWeight = currentWeight.add(grid.getProbability());
            if (randomNum.compareTo(currentWeight) < 0) {
                return new DrawResult(true, grid.getPrizeId(), grid.getPosition());
            }
        }

        // 未中奖,找一个未配置奖品的位置
        Integer noWinPosition = findNoWinPosition(gridConfig);
        return new DrawResult(false, null, noWinPosition);
    }

    /**
     * 找一个未配置奖品的位置作为未中奖位置
     */
    private Integer findNoWinPosition(List<GridPosition> gridConfig) {
        for (GridPosition grid : gridConfig) {
            if (grid.getPrizeId() == null) {
                return grid.getPosition();
            }
        }
        // 如果所有位置都有奖品,返回第一个位置
        return 1;
    }

    /**
     * 发送中奖站内信通知
     *
     * @param userId   用户ID
     * @param activity 抽奖活动
     * @param prize    中奖奖品
     */
    private void sendWinMessage(Integer userId, LotteryActivity activity, LotteryPrize prize) {
        try {

            UserMessage userMessage = new UserMessage();
            userMessage.setUserId(userId);
            userMessage.setTitle("恭喜您中奖了！");
            userMessage.setContent(String.format("恭喜您在【%s】活动中抽中了【%s】，请及时查收！",
                    activity.getActivityName(), prize.getPrizeName()));
            userMessage.setAddTime(System.currentTimeMillis()/1000);
            userMessageService.save(userMessage);
        } catch (Exception e) {
            // 站内信发送失败不影响抽奖流程
            log.error("发送中奖站内信失败,userId:{},prizeId:{}", userId, prize.getPrizeId(), e);
        }
    }

    @Override
    public LotteryActivityInfoVO getOngoingActivityInfo(Integer userId) {
        // 查询进行中的活动
        long currentTime = System.currentTimeMillis() / 1000;
        LotteryActivity activity = lotteryActivityService.lambdaQuery()
                .eq(LotteryActivity::getIsDelete, 0)
                .le(LotteryActivity::getStartTime, currentTime)
                .ge(LotteryActivity::getEndTime, currentTime)
                .orderByDesc(LotteryActivity::getCreateTime)
                .last("LIMIT 1")
                .one();

        if (activity == null) {
            return null;
        }

        return getActivityInfo(activity.getActivityId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LotteryResultVO drawOngoingActivity(Integer userId) {
        // 查询进行中的活动
        long currentTime = System.currentTimeMillis() / 1000;
        LotteryActivity activity = lotteryActivityService.lambdaQuery()
                .eq(LotteryActivity::getIsDelete, 0)
                .le(LotteryActivity::getStartTime, currentTime)
                .ge(LotteryActivity::getEndTime, currentTime)
                .orderByDesc(LotteryActivity::getCreateTime)
                .last("LIMIT 1")
                .one();

        if (activity == null) {
            throw new GlobalException("当前没有进行中的抽奖活动");
        }

        return drawLottery(activity.getActivityId(), userId);
    }

    /**
     * 构建返回结果
     */
    private LotteryResultVO buildResult(boolean isWin, LotteryPrize prize, 
                                       LotteryActivity activity, LotteryUserStat stat,
                                        Integer position, Integer recordId) {
        LotteryResultVO result = new LotteryResultVO();
        result.setIsWin(isWin);
        result.setPosition(position);

        if (isWin && prize != null) {
            result.setPrizeId(prize.getPrizeId());
            result.setPrizeName(prize.getPrizeName());
            result.setPrizeType(prize.getPrizeType());
            result.setPrizeValue(prize.getPrizeValue());
            result.setPrizeImage(prize.getPrizeImage());
        } else {
            result.setPrizeId(0);
            result.setPrizeName(activity.getNoWinName());
            result.setPrizeType(0);
            result.setPrizeValue("0");
            result.setPrizeImage(activity.getNoWinImage());
        }

        // 计算剩余次数（0表示不限制，返回-1表示无限制）
        int todayRemaining = activity.getDailyLimit() > 0 
                ? Math.max(0, activity.getDailyLimit() - stat.getTodayCount() - 1)
                : -1;
        int totalRemaining = activity.getTotalLimit() > 0 
                ? Math.max(0, activity.getTotalLimit() - stat.getTotalCount() - 1)
                : -1;

        result.setTodayRemaining(todayRemaining);
        result.setTotalRemaining(totalRemaining);
        result.setRecordId(recordId);

        return result;
    }
}
