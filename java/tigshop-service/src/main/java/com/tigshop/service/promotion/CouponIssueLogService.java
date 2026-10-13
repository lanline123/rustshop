package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.CouponIssueLog;
import com.tigshop.bean.query.promotion.coupon.CouponIssueLogPageQuery;
import com.tigshop.bean.vo.promotion.CouponIssueLogVO;
import com.tigshop.service.common.BaseService;

/**
 * 优惠券发放日志服务
 */
public interface CouponIssueLogService extends BaseService<CouponIssueLog> {

    /**
     * 记录优惠券发放日志
     *
     * @param batchId     发放批次ID
     * @param couponId    优惠券活动ID
     * @param userCouponId 用户优惠券ID
     * @param userId      用户ID
     * @param sourceType  发放来源：0-用户领取，1-抽奖中奖，2-手动发放
     */
    void log(Long batchId, Integer couponId, Integer userCouponId, Integer userId, Integer sourceType);

    /**
     * 分页查询发放日志
     */
    Page<CouponIssueLogVO> list(CouponIssueLogPageQuery query);
}
