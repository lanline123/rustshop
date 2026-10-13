package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.promotion.LotteryUserStat;

/**
 * 用户抽奖统计Service
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
public interface ILotteryUserStatService extends IService<LotteryUserStat> {

    /**
     * 获取用户统计(自动重置今日次数)
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 用户统计
     */
    LotteryUserStat getStat(Integer activityId, Integer userId);

    /**
     * 更新用户统计
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param isWin 是否中奖
     */
    void updateStat(Integer activityId, Integer userId, boolean isWin);
}


