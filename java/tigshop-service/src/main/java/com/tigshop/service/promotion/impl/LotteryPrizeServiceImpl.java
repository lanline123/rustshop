package com.tigshop.service.promotion.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.promotion.LotteryPrize;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.param.promotion.LotteryPrizeItemParam;
import com.tigshop.bean.vo.promotion.LotteryPrizeVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.promotion.LotteryPrizeMapper;
import com.tigshop.service.promotion.CouponService;
import com.tigshop.service.promotion.ILotteryPrizeService;
import com.tigshop.service.user.UserCouponService;
import com.tigshop.service.user.UserPointsLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 抽奖奖品ServiceImpl
 * 
 * 注意：奖品跟随活动一起保存，不提供单独的增删改接口
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Service
@Slf4j
public class LotteryPrizeServiceImpl extends ServiceImpl<LotteryPrizeMapper, LotteryPrize>
        implements ILotteryPrizeService {

    @Resource
    private LotteryPrizeMapper lotteryPrizeMapper;

    @Resource
    private UserPointsLogService userPointsLogService;

    @Resource
    private CouponService couponService;

    @Resource
    private UserCouponService userCouponService;

    @Override
    public List<LotteryPrizeVO> getListByActivity(Integer activityId) {
        if (activityId == null) {
            throw new GlobalException("活动ID不能为空");
        }

        List<LotteryPrize> prizes = this.lambdaQuery()
                .eq(LotteryPrize::getActivityId, activityId)
                .orderByDesc(LotteryPrize::getCreateTime)
                .list();

        return prizes.stream().map(prize -> {
            LotteryPrizeVO vo = new LotteryPrizeVO();
            BeanUtil.copyProperties(prize, vo);
            vo.setPrizeTypeText(getPrizeTypeText(prize.getPrizeType()));
            vo.setRemainStock(prize.getPrizeStock() - prize.getPrizeStockUsed());
            vo.setCreateTime(TigUtils.handelTime(prize.getCreateTime()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public LotteryPrize getPrizeById(Integer prizeId) {
        return this.getById(prizeId);
    }

    @Override
    public boolean deductStock(Integer prizeId) {
        long currentTime = System.currentTimeMillis() / 1000;
        int rows = lotteryPrizeMapper.deductStock(prizeId, currentTime);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantPrize(Integer userId, LotteryPrize prize) {
        Integer prizeType = prize.getPrizeType();

        // 1-积分
        if (prizeType == 1) {
            grantPoints(userId, prize);
        }
        // 3-优惠券
        else if (prizeType == 3) {
            grantCoupon(userId, prize);
        }
        // 2-商品,不自动发放,需要用户确认下单后调用confirmReceive接口
    }

    /**
     * 获取奖品类型文本
     */
    private String getPrizeTypeText(Integer prizeType) {
        return switch (prizeType) {
            case 1 -> "积分";
            case 2 -> "商品";
            case 3 -> "优惠券";
            default -> "未知";
        };
    }

    /**
     * 发放积分奖品
     */
    private void grantPoints(Integer userId, LotteryPrize prize) {
        Integer points = Integer.parseInt(prize.getPrizeValue());

        // 使用标准方法增加积分并记录日志
        userPointsLogService.incPoints(
            points,
            userId,
            "抽奖中奖奖励积分",
            prize.getPrizeId()
        );

        log.info("发放积分奖品成功,用户:{},奖品:{},积分:{}", userId, prize.getPrizeName(), points);
    }

    /**
     * 发放优惠券奖品
     */
    private void grantCoupon(Integer userId, LotteryPrize prize) {
        Integer couponId = Integer.parseInt(prize.getPrizeValue());

        // 查询优惠券信息
        Coupon coupon = couponService.getById(couponId);
        if (coupon == null) {
            throw new GlobalException("优惠券不存在");
        }

        // 发放到用户账户，设置sourceType=1表示抽奖中奖获得
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .startDate(coupon.getUseStartDate())
                .endDate(coupon.getUseEndDate())
                .sourceType(1)
                .build();
        userCouponService.save(userCoupon);

        log.info("发放优惠券奖品成功,用户:{},奖品:{},优惠券ID:{}", userId, prize.getPrizeName(), couponId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByActivityId(Integer activityId) {
        if (activityId == null) {
            return;
        }

        // 物理删除该活动下所有奖品
        this.lambdaUpdate()
                .eq(LotteryPrize::getActivityId, activityId)
                .remove();

        log.info("删除活动奖品成功,活动ID:{}", activityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Integer, Integer> savePrizeList(Integer activityId, List<LotteryPrizeItemParam> prizeList) {
        Map<Integer, Integer> tempIndexToPrizeIdMap = new HashMap<>();

        if (prizeList == null || prizeList.isEmpty()) {
            return tempIndexToPrizeIdMap;
        }

        long currentTime = System.currentTimeMillis() / 1000;

        // 读取该活动下已有奖品，用于更新并保留 prizeStockUsed 等统计字段
        List<LotteryPrize> existingPrizeList = this.lambdaQuery()
                .eq(LotteryPrize::getActivityId, activityId)
                .list();
        Map<Integer, LotteryPrize> existingById = existingPrizeList.stream()
                .filter(p -> p.getPrizeId() != null)
                .collect(Collectors.toMap(LotteryPrize::getPrizeId, p -> p, (a, b) -> a));

        Set<Integer> keepPrizeIds = new HashSet<>();

        for (LotteryPrizeItemParam prizeItem : prizeList) {
            Integer prizeId = prizeItem.getPrizeId();

            // 1) 编辑已有奖品：更新基础信息，但保留已使用库存(prizeStockUsed)与创建时间
            if (prizeId != null) {
                LotteryPrize existing = existingById.get(prizeId);
                if (existing == null) {
                    LotteryPrize dbPrize = this.getById(prizeId);
                    if (dbPrize == null) {
                        throw new GlobalException("奖品不存在，prizeId:" + prizeId);
                    }
                    if (dbPrize.getActivityId() == null || !dbPrize.getActivityId().equals(activityId)) {
                        throw new GlobalException("奖品不属于当前活动，prizeId:" + prizeId);
                    }
                    existing = dbPrize;
                }

                existing.setPrizeName(prizeItem.getPrizeName());
                existing.setPrizeType(prizeItem.getPrizeType());
                existing.setPrizeValue(prizeItem.getPrizeValue());
                existing.setPrizeImage(prizeItem.getPrizeImage());
                existing.setPrizeStock(prizeItem.getPrizeStock());
                existing.setUpdateTime(currentTime);
                this.updateById(existing);

                keepPrizeIds.add(prizeId);

                if (prizeItem.getTempIndex() != null) {
                    tempIndexToPrizeIdMap.put(prizeItem.getTempIndex(), prizeId);
                }

                log.info("更新奖品成功,活动ID:{},奖品ID:{},tempIndex:{}",
                        activityId, prizeId, prizeItem.getTempIndex());
                continue;
            }

            // 2) 新增奖品：创建新记录，已使用库存从0开始
            LotteryPrize prize = new LotteryPrize();
            BeanUtil.copyProperties(prizeItem, prize, "prizeId");
            prize.setActivityId(activityId);
            prize.setPrizeStockUsed(0);
            prize.setCreateTime(currentTime);
            prize.setUpdateTime(currentTime);
            this.save(prize);
            keepPrizeIds.add(prize.getPrizeId());

            // 记录临时索引到真实ID的映射
            if (prizeItem.getTempIndex() != null) {
                tempIndexToPrizeIdMap.put(prizeItem.getTempIndex(), prize.getPrizeId());
            }

            log.info("保存奖品成功,活动ID:{},奖品ID:{},tempIndex:{}", 
                    activityId, prize.getPrizeId(), prizeItem.getTempIndex());
        }

        // 3) 清理本次未保留的旧奖品（仅允许删除未使用过的奖品）
        if (!existingPrizeList.isEmpty()) {
            List<LotteryPrize> needDelete = existingPrizeList.stream()
                    .filter(p -> p.getPrizeId() != null && !keepPrizeIds.contains(p.getPrizeId()))
                    .collect(Collectors.toList());

            for (LotteryPrize p : needDelete) {
                Integer used = p.getPrizeStockUsed();
                if (used != null && used > 0) {
                    throw new GlobalException("奖品【" + p.getPrizeName() + "】已发放，不能删除/移除，请保留该奖品");
                }
            }

            if (!needDelete.isEmpty()) {
                this.removeByIds(needDelete.stream().map(LotteryPrize::getPrizeId).collect(Collectors.toList()));
                log.info("清理未保留的活动奖品成功,活动ID:{},数量:{}", activityId, needDelete.size());
            }
        }

        return tempIndexToPrizeIdMap;
    }
}
