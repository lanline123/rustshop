package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.promotion.LotteryRecord;
import com.tigshop.bean.query.promotion.LotteryRecordQuery;
import com.tigshop.bean.vo.promotion.LotteryRecordVO;
import com.tigshop.bean.vo.promotion.LotteryWinnerVO;

import java.util.List;

/**
 * 抽奖记录Service
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
public interface ILotteryRecordService extends IService<LotteryRecord> {

    /**
     * 保存抽奖记录
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param prizeId 奖品ID(0表示未中奖)
     * @param costPoints 消耗积分
     * @param isWin 是否中奖
     * @return 记录ID
     */
    Integer saveRecord(Integer activityId, Integer userId, Integer prizeId, 
                      Integer costPoints, boolean isWin);

    /**
     * 查询用户中奖记录
     *
     * @param userId 用户ID
     * @param query 查询参数(含分页、状态筛选)
     * @return 记录列表
     */
    Page<LotteryRecordVO> getUserRecords(Integer userId, LotteryRecordQuery query);

    /**
     * 确认领取商品奖品
     *
     * @param recordId 记录ID
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void confirmReceive(Integer recordId, Integer orderId, Integer userId);

    /**
     * 获取中奖名单（公开展示，用户名脱敏）
     *
     * @param activityId 活动ID（为空则查询当前进行中的活动）
     * @return 中奖名单列表
     */
    List<LotteryWinnerVO> getWinnerList(Integer activityId);
}

