package com.tigshop.service.promotion;

import com.tigshop.bean.vo.promotion.LotteryActivityInfoVO;
import com.tigshop.bean.vo.promotion.LotteryResultVO;

/**
 * 抽奖核心Service
 *
 * @author Tigshop团队
 * &#064;create  2024年12月12日
 */
public interface ILotteryDrawService {

    /**
     * 获取活动信息(用户端)
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 活动信息
     */
    LotteryActivityInfoVO getActivityInfo(Integer activityId, Integer userId);

    /**
     * 执行抽奖
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 抽奖结果
     */
    LotteryResultVO drawLottery(Integer activityId, Integer userId);

    /**
     * 获取进行中的活动信息(用户端)
     * 说明: 获取当前进行中的抽奖活动信息，用于客户端展示
     *
     * @param userId 用户ID
     * @return 活动信息，如果没有进行中的活动返回null
     */
    LotteryActivityInfoVO getOngoingActivityInfo(Integer userId);

    /**
     * 参与进行中的活动抽奖
     * 说明: 参与当前进行中的抽奖活动
     *
     * @param userId 用户ID
     * @return 抽奖结果
     */
    LotteryResultVO drawOngoingActivity(Integer userId);
}

