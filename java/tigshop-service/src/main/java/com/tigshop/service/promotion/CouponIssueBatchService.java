package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.promotion.CouponIssueBatch;
import com.tigshop.bean.query.promotion.coupon.CouponIssueBatchPageQuery;
import com.tigshop.bean.vo.promotion.CouponIssueBatchVO;
import com.tigshop.service.common.BaseService;

/**
 * 优惠券发放批次服务
 */
public interface CouponIssueBatchService extends BaseService<CouponIssueBatch> {

    /**
     * 创建发放批次
     *
     * @param couponId    优惠券活动ID
     * @param userCount   发放用户数
     * @param couponCount 发放张数
     * @param sourceType  发放来源
     * @return 批次ID
     */
    Long createBatch(Integer couponId, Integer userCount, Integer couponCount, Integer sourceType);

    /**
     * 分页查询发放批次
     */
    Page<CouponIssueBatchVO> list(CouponIssueBatchPageQuery query);
}
