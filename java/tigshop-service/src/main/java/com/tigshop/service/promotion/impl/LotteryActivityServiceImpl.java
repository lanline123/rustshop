package com.tigshop.service.promotion.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.enums.user.FromTag;
import com.tigshop.bean.model.promotion.*;
import com.tigshop.bean.model.promotion.lottery.GridPosition;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.promotion.GridPositionParam;
import com.tigshop.bean.param.promotion.LotteryActivityParam;
import com.tigshop.bean.param.promotion.LotteryPrizeItemParam;
import com.tigshop.bean.query.promotion.LotteryActivityQuery;
import com.tigshop.bean.query.promotion.LotteryUserManageQuery;
import com.tigshop.bean.query.promotion.LotteryUserRecordQuery;
import com.tigshop.bean.vo.promotion.*;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.promotion.LotteryActivityMapper;
import com.tigshop.mapper.promotion.LotteryRecordMapper;
import com.tigshop.mapper.promotion.LotteryUserStatMapper;
import com.tigshop.service.promotion.ILotteryActivityService;
import com.tigshop.service.promotion.ILotteryPrizeService;
import com.tigshop.service.promotion.PromotionService;
import com.tigshop.service.user.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抽奖活动ServiceImpl
 *
 * @author Tigshop团队
 * &#064;create  2024年12月12日
 */
@Service
@Slf4j
public class LotteryActivityServiceImpl extends ServiceImpl<LotteryActivityMapper, LotteryActivity>
        implements ILotteryActivityService {

    @Resource
    private ILotteryPrizeService lotteryPrizeService;

    @Resource
    private PromotionService promotionService;

    @Resource
    private LotteryRecordMapper lotteryRecordMapper;

    @Resource
    private LotteryUserStatMapper lotteryUserStatMapper;

    @Resource
    private UserService userService;

    @Override
    public Page<LotteryActivityListVO> getList(LotteryActivityQuery query) {
        Page<LotteryActivity> pageParam = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<LotteryActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LotteryActivity::getIsDelete, 0);

        // 活动名称搜索
        String keyword = query.getKeyword();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(LotteryActivity::getActivityName, keyword);
        }

        // 活动名称搜索（兼容）
        if (StrUtil.isNotBlank(query.getActivityName())) {
            wrapper.like(LotteryActivity::getActivityName, query.getActivityName());
        }

        // 状态筛选（根据开始/结束时间计算）
        long currentTime = System.currentTimeMillis() / 1000;
        if (query.getStatus() != null) {
            if (query.getStatus() == 0) {
                // 未开始
                wrapper.gt(LotteryActivity::getStartTime, currentTime);
            } else if (query.getStatus() == 1) {
                // 进行中
                wrapper.le(LotteryActivity::getStartTime, currentTime);
                wrapper.ge(LotteryActivity::getEndTime, currentTime);
            } else if (query.getStatus() == 2) {
                // 已结束
                wrapper.lt(LotteryActivity::getEndTime, currentTime);
            }
        }

        wrapper.orderByDesc(LotteryActivity::getCreateTime);

        Page<LotteryActivity> activityPage = this.page(pageParam, wrapper);

        // 转换为ListVO
        Page<LotteryActivityListVO> voPage = new Page<>();
        BeanUtil.copyProperties(activityPage, voPage, "records");

        List<LotteryActivityListVO> voList = activityPage.getRecords().stream().map(activity -> {
            LotteryActivityListVO vo = new LotteryActivityListVO();
            vo.setActivityId(activity.getActivityId());
            vo.setActivityName(activity.getActivityName());

            // 时间戳转字符串(由@JsonSerialize注解自动格式化)
            vo.setCreateTime(String.valueOf(activity.getCreateTime()));
            vo.setStartTime(String.valueOf(activity.getStartTime()));
            vo.setEndTime(String.valueOf(activity.getEndTime()));

            // 计算活动状态
            int status = calculateActivityStatus(activity.getStartTime(), activity.getEndTime());
            vo.setStatus(status);
            vo.setStatusText(getStatusText(status));

            // 统计参与人数
            Integer participantCount = countParticipants(activity.getActivityId());
            vo.setParticipantCount(participantCount);

            // 判断是否可以编辑/删除
            vo.setCanEdit(status == 0); // 只有未开始的活动可以编辑
            vo.setCanDelete(status != 1); // 进行中的活动不能删除

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public LotteryActivityVO getDetail(Integer activityId) {
        LotteryActivity activity = this.getById(activityId);
        if (activity == null || activity.getIsDelete() == 1) {
            throw new GlobalException("活动不存在");
        }

        LotteryActivityVO vo = new LotteryActivityVO();
        BeanUtil.copyProperties(activity, vo, "startTime", "endTime", "createTime", "gridConfig", "prizeList");
        // 时间戳转字符串(由@JsonSerialize注解自动格式化)
        vo.setStartTime(String.valueOf(activity.getStartTime()));
        vo.setEndTime(String.valueOf(activity.getEndTime()));
        vo.setCreateTime(String.valueOf(activity.getCreateTime()));

        // 计算活动状态(根据时间)
        int status = calculateActivityStatus(activity.getStartTime(), activity.getEndTime());
        vo.setStatus(status);
        vo.setStatusText(getStatusText(status));

        // 查询活动关联的奖品列表
        vo.setPrizeList(lotteryPrizeService.getListByActivity(activityId));

        // 构建九宫格配置(填充奖品详情)
        List<GridPositionVO> gridPositionVOList = buildGridPositionVO(activity);
        vo.setGridConfig(gridPositionVOList);

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
    public Integer create(LotteryActivityParam param) {
        // 时间字符串转时间戳
        long startTimestamp = TigUtils.toTimestampYmdHms(param.getStartTime());
        long endTimestamp = TigUtils.toTimestampYmdHms(param.getEndTime());

        // 基础参数校验（不校验奖品，因为奖品还没有保存）
        validateActivityBasicParam(param, null, startTimestamp, endTimestamp);

        // 检查时间冲突
        if (checkTimeConflict(null, startTimestamp, endTimestamp)) {
            throw new GlobalException("活动时间与其他活动冲突，每个时段只能有一个抽奖活动");
        }

        // 1. 先保存活动基本信息（gridConfig暂时为空）
        LotteryActivity activity = new LotteryActivity();
        BeanUtil.copyProperties(param, activity, "startTime", "endTime", "gridConfig", "prizeList");
        normalizeCostConfig(activity);
        activity.setStartTime(startTimestamp);
        activity.setEndTime(endTimestamp);
        activity.setShopId(0);
        activity.setIsDelete(0);
        long currentTime = System.currentTimeMillis() / 1000;
        activity.setCreateTime(currentTime);
        activity.setUpdateTime(currentTime);
        // 先设置空的gridConfig
        activity.setGridConfig(new ArrayList<>());

        this.save(activity);
        Integer activityId = activity.getActivityId();

        // 2. 保存奖品列表，获取 tempIndex → prizeId 映射
        Map<Integer, Integer> tempIndexToPrizeIdMap = lotteryPrizeService.savePrizeList(
                activityId, param.getPrizeList());

        // 3. 转换并校验 gridConfig（将 tempIndex 转换为 prizeId）
        List<GridPosition> gridConfig = convertAndValidateGridConfig(
                param.getGridConfig(), tempIndexToPrizeIdMap);

        // 4. 更新活动的 gridConfig
        activity.setGridConfig(gridConfig);
        this.updateById(activity);

        // 关联到Promotion表
        savePromotion(activity);

        log.info("创建抽奖活动成功,活动ID:{},活动名称:{}", activity.getActivityId(), activity.getActivityName());
        return activity.getActivityId();
    }

    /**
     * 转换并校验GridConfig参数
     * 将 tempIndex 转换为 prizeId，并进行奖品和概率校验
     *
     * @param params 九宫格参数列表
     * @param tempIndexToPrizeIdMap tempIndex到prizeId的映射
     * @return 转换后的GridPosition列表
     */
    private List<GridPosition> convertAndValidateGridConfig(
            List<GridPositionParam> params,
            Map<Integer, Integer> tempIndexToPrizeIdMap) {
        
        if (params == null || params.size() != 8) {
            throw new GlobalException("必须配置8个九宫格位置");
        }

        // 校验位置唯一性
        long distinctCount = params.stream()
                .map(GridPositionParam::getPosition)
                .distinct()
                .count();
        if (distinctCount != 8) {
            throw new GlobalException("九宫格位置不能重复");
        }

        List<GridPosition> gridConfig = new ArrayList<>();
        BigDecimal totalProbability = BigDecimal.ZERO;
        BigDecimal hundred = new BigDecimal("100");
        // 有奖品的位置数量
        int prizeCount = 0;

        for (GridPositionParam param : params) {
            GridPosition grid = new GridPosition();
            grid.setPosition(param.getPosition());
            grid.setProbability(param.getProbability());

            // 使用 tempIndex 转换为 prizeId
            Integer prizeId = null;
            if (param.getTempIndex() != null) {
                prizeId = tempIndexToPrizeIdMap.get(param.getTempIndex());
                if (prizeId == null) {
                    throw new GlobalException("位置" + param.getPosition() + "关联的奖品不存在，tempIndex:" + param.getTempIndex());
                }
            }

            grid.setPrizeId(prizeId);

            // 校验有奖品的位置
            if (prizeId != null) {
                prizeCount++;
                
                // 校验奖品是否存在
                LotteryPrize prize = lotteryPrizeService.getPrizeById(prizeId);
                if (prize == null) {
                    throw new GlobalException("位置" + param.getPosition() + "配置的奖品不存在");
                }

                // 校验概率
                if (param.getProbability() == null || param.getProbability().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new GlobalException("有奖品的位置概率必须大于0");
                }
                if (param.getProbability().compareTo(hundred) > 0) {
                    throw new GlobalException("单个奖品概率不能超过100%");
                }
                totalProbability = totalProbability.add(param.getProbability());
            }

            gridConfig.add(grid);
        }

        // 校验至少有一个奖品位置
        if (prizeCount == 0) {
            throw new GlobalException("九宫格至少需要配置一个奖品位置");
        }

        // 校验总概率
        if (totalProbability.compareTo(hundred) >= 0) {
            throw new GlobalException("奖品概率总和必须小于100%，剩余概率为未中奖概率");
        }

        return gridConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(LotteryActivityParam param) {
        if (param.getActivityId() == null) {
            throw new GlobalException("活动ID不能为空");
        }

        LotteryActivity activity = this.getById(param.getActivityId());
        if (activity == null || activity.getIsDelete() == 1) {
            throw new GlobalException("活动不存在");
        }

        // 判断活动状态
        calculateActivityStatus(activity.getStartTime(), activity.getEndTime());

        // 时间字符串转时间戳
        long startTimestamp = TigUtils.toTimestampYmdHms(param.getStartTime());
        long endTimestamp = TigUtils.toTimestampYmdHms(param.getEndTime());

        // 基础参数校验
        validateActivityBasicParam(param, param.getActivityId(), startTimestamp, endTimestamp);

        // 检查时间冲突
        if (checkTimeConflict(param.getActivityId(), startTimestamp, endTimestamp)) {
            throw new GlobalException("活动时间与其他活动冲突，每个时段只能有一个抽奖活动");
        }

        Integer activityId = param.getActivityId();

        // 1. 保存/更新奖品列表（保留已使用库存等统计字段），获取 tempIndex → prizeId 映射
        Map<Integer, Integer> tempIndexToPrizeIdMap = lotteryPrizeService.savePrizeList(
                activityId, param.getPrizeList());

        // 2. 转换并校验 gridConfig（将 tempIndex 转换为 prizeId）
        List<GridPosition> gridConfig = convertAndValidateGridConfig(
                param.getGridConfig(), tempIndexToPrizeIdMap);

        // 3. 更新活动
        BeanUtil.copyProperties(param, activity, "activityId", "shopId", "isDelete", "createTime", "startTime", "endTime", "gridConfig", "prizeList");
        normalizeCostConfig(activity);
        activity.setStartTime(startTimestamp);
        activity.setEndTime(endTimestamp);
        activity.setUpdateTime(System.currentTimeMillis() / 1000);
        activity.setGridConfig(gridConfig);

        this.updateById(activity);

        // 更新Promotion关联
        savePromotion(activity);

        log.info("更新抽奖活动成功,活动ID:{}", activity.getActivityId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer activityId) {
        LotteryActivity activity = this.getById(activityId);
        if (activity == null || activity.getIsDelete() == 1) {
            throw new GlobalException("活动不存在");
        }

        // 判断活动状态
        int status = calculateActivityStatus(activity.getStartTime(), activity.getEndTime());
        if (status == 1) {
            throw new GlobalException("活动进行中，不能删除。请先结束活动");
        }

        // 软删除
        activity.setIsDelete(1);
        activity.setUpdateTime(System.currentTimeMillis() / 1000);
        this.updateById(activity);

        // 删除Promotion关联
        deletePromotion(activityId);

        log.info("删除抽奖活动成功,活动ID:{}", activityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endActivity(Integer activityId) {
        LotteryActivity activity = this.getById(activityId);
        if (activity == null || activity.getIsDelete() == 1) {
            throw new GlobalException("活动不存在");
        }

        // 判断活动状态
        int status = calculateActivityStatus(activity.getStartTime(), activity.getEndTime());
        if (status == 0) {
            throw new GlobalException("活动未开始，不能结束");
        }
        if (status == 2) {
            throw new GlobalException("活动已结束");
        }

        // 将结束时间设置为当前时间
        long currentTime = (System.currentTimeMillis() - 1000) / 1000;
        activity.setEndTime(currentTime);
        activity.setUpdateTime(currentTime);
        this.updateById(activity);

        // 同步更新Promotion 
        promotionService.update(new LambdaUpdateWrapper<Promotion>()
                .eq(Promotion::getRelationId, activityId)
                .eq(Promotion::getType, PromotionType.TYPE_LOTTERY.getCode())
                .set(Promotion::getEndTime, currentTime));

        log.info("结束抽奖活动成功,活动ID:{}", activityId);
    }

    @Override
    public boolean checkTimeConflict(Integer activityId, Long startTime, Long endTime) {
        LambdaQueryWrapper<LotteryActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LotteryActivity::getIsDelete, 0);

        // 排除自己（编辑时）
        if (activityId != null) {
            wrapper.ne(LotteryActivity::getActivityId, activityId);
        }

        // 时间交叉判断：
        // 新活动的开始时间 < 已有活动的结束时间 AND 新活动的结束时间 > 已有活动的开始时间
        wrapper.and(w -> w
                .lt(LotteryActivity::getStartTime, endTime)
                .gt(LotteryActivity::getEndTime, startTime)
        );

        long count = this.count(wrapper);
        return count > 0;
    }

    /**
     * 计算活动状态
     * <p>
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 0-未开始, 1-进行中, 2-已结束
     */
    private int calculateActivityStatus(Long startTime, Long endTime) {
        long currentTime = System.currentTimeMillis() / 1000;
        // 未开始
        if (currentTime < startTime) {
            return 0;
        }
        // 进行中
        else if (currentTime <= endTime) {
            return 1;
        }
        // 已结束
        else {
            return 2;
        }
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(int status) {
        return switch (status) {
            case 0 -> "未开始";
            case 1 -> "进行中";
            case 2 -> "已结束";
            default -> "未知";
        };
    }

    /**
     * 统计参与人数（去重）
     */
    private Integer countParticipants(Integer activityId) {
        LambdaQueryWrapper<LotteryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LotteryRecord::getActivityId, activityId);
        wrapper.select(LotteryRecord::getUserId);
        wrapper.groupBy(LotteryRecord::getUserId);

        List<LotteryRecord> records = lotteryRecordMapper.selectList(wrapper);
        return records != null ? records.size() : 0;
    }

    /**
     * 保存/更新Promotion关联
     */
    private void savePromotion(LotteryActivity activity) {
        Integer relationId = activity.getActivityId();

        // 根据时间计算活动状态（1-进行中为可用）
        int activityStatus = calculateActivityStatus(activity.getStartTime(), activity.getEndTime());
        int isAvailable = (activityStatus == 1) ? 1 : 0;

        // 查询是否已存在
        long count = promotionService.count(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getRelationId, relationId)
                .eq(Promotion::getType, PromotionType.TYPE_LOTTERY.getCode()));

        if (count > 0) {
            // 更新
            promotionService.update(new LambdaUpdateWrapper<Promotion>()
                    .eq(Promotion::getRelationId, relationId)
                    .eq(Promotion::getType, PromotionType.TYPE_LOTTERY.getCode())
                    .set(Promotion::getStartTime, activity.getStartTime())
                    .set(Promotion::getEndTime, activity.getEndTime())
                    .set(Promotion::getPromotionName, activity.getActivityName())
                    .set(Promotion::getIsAvailable, isAvailable));
        } else {
            // 新增
            Promotion promotion = getPromotion(activity, isAvailable);
            boolean success = promotionService.save(promotion);
            if (!success) {
                throw new GlobalException("关联Promotion表失败");
            }
        }
    }

    @NotNull
    private static Promotion getPromotion(LotteryActivity activity, int isAvailable) {
        Promotion promotion = new Promotion();
        promotion.setPromotionName(activity.getActivityName());
        promotion.setStartTime(activity.getStartTime());
        promotion.setEndTime(activity.getEndTime());
        promotion.setType(PromotionType.TYPE_LOTTERY.getCode());
        promotion.setShopId(activity.getShopId());
        promotion.setRelationId(activity.getActivityId());
        promotion.setIsAvailable(isAvailable);
        promotion.setIsDelete(0);
        return promotion;
    }

    /**
     * 删除Promotion关联
     */
    private void deletePromotion(Integer activityId) {
        promotionService.update(new LambdaUpdateWrapper<Promotion>()
                .eq(Promotion::getRelationId, activityId)
                .eq(Promotion::getType, PromotionType.TYPE_LOTTERY.getCode())
                .set(Promotion::getIsDelete, 1));
    }

    /**
     * 归一化抽奖参与条件配置
     * costType 为空时默认消耗积分(兼容旧数据);免费模式强制消耗积分为0
     */
    private void normalizeCostConfig(LotteryActivity activity) {
        Integer costType = activity.getCostType() == null ? 1 : activity.getCostType();
        activity.setCostType(costType);
        if (costType == 0) {
            // 免费(无限制)模式,不扣积分
            activity.setCostPoints(0);
        } else if (activity.getCostPoints() == null) {
            activity.setCostPoints(0);
        }
    }

    /**
     * 基础参数校验（不校验奖品，奖品在保存后单独校验）
     */
    private void validateActivityBasicParam(LotteryActivityParam param, Integer activityId, long startTimestamp, long endTimestamp) {
        // 时间校验
        if (endTimestamp <= startTimestamp) {
            throw new GlobalException("活动结束时间必须大于开始时间");
        }

        // 抽奖参与条件校验:0-无限制(免费),1-消耗积分(默认,兼容旧数据)
        Integer costType = param.getCostType() == null ? 1 : param.getCostType();
        if (costType != 0 && costType != 1) {
            throw new GlobalException("抽奖参与条件参数错误");
        }
        // 消耗积分模式下,消耗积分必须大于0;免费模式不校验积分
        if (costType == 1) {
            if (param.getCostPoints() == null || param.getCostPoints() <= 0) {
                throw new GlobalException("消耗积分必须大于0");
            }
        }

        // 九宫格配置基础校验（不校验奖品是否存在，因为奖品可能还没保存）
        List<GridPositionParam> gridConfig = param.getGridConfig();
        if (gridConfig == null || gridConfig.size() != 8) {
            throw new GlobalException("必须配置8个九宫格位置");
        }

        // 校验位置唯一性
        long distinctCount = gridConfig.stream()
                .map(GridPositionParam::getPosition)
                .distinct()
                .count();
        if (distinctCount != 8) {
            throw new GlobalException("九宫格位置不能重复");
        }

        // 未中奖配置校验
        if (StrUtil.isBlank(param.getNoWinName())) {
            throw new GlobalException("未中奖名称不能为空");
        }
        if (StrUtil.isBlank(param.getNoWinImage())) {
            throw new GlobalException("未中奖图片不能为空");
        }
        if (StrUtil.isNotBlank(param.getNoWinImage()) && !param.getNoWinImage().startsWith("http")) {
            throw new GlobalException("未中奖图片URL格式不正确");
        }

        // 校验奖品列表
        List<LotteryPrizeItemParam> prizeList = param.getPrizeList();
        if (prizeList == null || prizeList.isEmpty()) {
            throw new GlobalException("奖品列表不能为空");
        }
        if (prizeList.size() > 7) {
            throw new GlobalException("奖品数量不能超过7个");
        }
    }

    @Override
    public Page<LotteryUserManageVO> getUserManageList(LotteryUserManageQuery query) {
        Page<LotteryUserStat> pageParam = new Page<>(query.getPage(), query.getSize());

        // 构建查询条件
        LambdaQueryWrapper<LotteryUserStat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LotteryUserStat::getActivityId, query.getActivityId());

        // 仅展示实际参与过抽奖的用户，过滤未抽奖的初始化记录
        wrapper.gt(LotteryUserStat::getTotalCount, 0);

        // 最后参与时间范围筛选
        if (StrUtil.isNotBlank(query.getStartTime())) {
            long startTimestamp = TigUtils.toTimestampYmdHms(query.getStartTime());
            wrapper.ge(LotteryUserStat::getLastLotteryTime, startTimestamp);
        }
        if (StrUtil.isNotBlank(query.getEndTime())) {
            long endTimestamp = TigUtils.toTimestampYmdHms(query.getEndTime());
            wrapper.le(LotteryUserStat::getLastLotteryTime, endTimestamp);
        }

        // 按最后参与时间倒序排序
        wrapper.orderByDesc(LotteryUserStat::getLastLotteryTime);

        // 分页查询
        Page<LotteryUserStat> statPage = lotteryUserStatMapper.selectPage(pageParam, wrapper);

        // 获取所有用户ID
        List<Integer> userIds = statPage.getRecords().stream()
                .map(LotteryUserStat::getUserId)
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Integer, User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getUserId, u -> u));
        }

        // 用户名称筛选（如果有userName筛选条件，需要重新查询）
        if (StrUtil.isNotBlank(query.getUserName())) {
            Map<Integer, User> finalUserMap = userMap;
            List<LotteryUserStat> filteredStats = statPage.getRecords().stream()
                    .filter(stat -> {
                        User user = finalUserMap.get(stat.getUserId());
                        return user != null && user.getUsername() != null
                                && user.getUsername().contains(query.getUserName());
                    })
                    .collect(Collectors.toList());
            statPage.setRecords(filteredStats);
            statPage.setTotal(filteredStats.size());
        }

        // 转换为VO
        Page<LotteryUserManageVO> voPage = new Page<>();
        BeanUtil.copyProperties(statPage, voPage, "records");

        Map<Integer, User> finalUserMap = userMap;
        List<LotteryUserManageVO> voList = statPage.getRecords().stream().map(stat -> {
            LotteryUserManageVO vo = new LotteryUserManageVO();
            vo.setUserId(stat.getUserId());
            vo.setTotalCount(stat.getTotalCount());

            // 获取用户信息
            User user = finalUserMap.get(stat.getUserId());
            vo.setUserName(user != null ? user.getUsername() : "未知用户");
            vo.setAvatar(user != null ? user.getAvatar() : null);
            vo.setFromTagName(user != null && user.getFromTag() != null 
                    ? FromTag.getFromTagName(user.getFromTag()) : "");

            // 最后参与时间
            if (stat.getLastLotteryTime() != null) {
                vo.setLastLotteryTime(TigUtils.handelTime(stat.getLastLotteryTime()));
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<LotteryUserRecordVO> getUserRecordList(LotteryUserRecordQuery query) {
        Page<LotteryRecord> pageParam = new Page<>(query.getPage(), query.getSize());

        // 查询用户在该活动中的抽奖记录
        LambdaQueryWrapper<LotteryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LotteryRecord::getActivityId, query.getActivityId());
        wrapper.eq(LotteryRecord::getUserId, query.getUserId());
        wrapper.orderByDesc(LotteryRecord::getCreateTime);

        Page<LotteryRecord> recordPage = lotteryRecordMapper.selectPage(pageParam, wrapper);

        // 获取用户信息
        User user = userService.getById(query.getUserId());
        String userName = user != null ? user.getUsername() : "未知用户";
        String avatar = user != null ? user.getAvatar() : null;
        String fromTagName = user != null && user.getFromTag() != null 
                ? FromTag.getFromTagName(user.getFromTag()) : "";

        // 转换为VO
        Page<LotteryUserRecordVO> voPage = new Page<>();
        BeanUtil.copyProperties(recordPage, voPage, "records");

        List<LotteryUserRecordVO> voList = recordPage.getRecords().stream().map(record -> {
            LotteryUserRecordVO vo = new LotteryUserRecordVO();
            vo.setRecordId(record.getRecordId());
            vo.setUserName(userName);
            vo.setAvatar(avatar);
            vo.setFromTagName(fromTagName);

            // 参与时间
            if (record.getCreateTime() != null) {
                vo.setCreateTime(TigUtils.handelTime(record.getCreateTime()));
            }

            // 获得奖品名称
            vo.setPrizeName(getPrizeDisplayName(record));

            // 奖品状态文本
            vo.setStatusText(getPrizeStatusText(record));

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取奖品显示名称
     * 积分显示"积分 x ?"，商品和优惠券显示奖品名称，未中奖显示"未中奖"
     */
    private String getPrizeDisplayName(LotteryRecord record) {
        if (record.getIsWin() == 0 || record.getPrizeType() == null || record.getPrizeType() == 0) {
            return "未中奖";
        }

        return switch (record.getPrizeType()) {
            // 积分
            case 1 -> "积分 x " + record.getPrizeValue();
            // 商品 // 优惠券
            case 2, 3 -> record.getPrizeName();
            default -> record.getPrizeName();
        };
    }

    /**
     * 获取奖品状态文本
     * 商品：已兑换/未兑换，积分/优惠券：已发放/未发放，未中奖：-
     */
    private String getPrizeStatusText(LotteryRecord record) {
        if (record.getIsWin() == 0 || record.getPrizeType() == null || record.getPrizeType() == 0) {
            return "-";
        }

        return switch (record.getPrizeType()) {
            // 积分// 优惠券
            case 1, 3 -> record.getStatus() == 1 ? "已发放" : "未发放";
            // 商品
            case 2 -> record.getStatus() == 1 ? "已兑换" : "未兑换";
            default -> "-";
        };
    }
}
