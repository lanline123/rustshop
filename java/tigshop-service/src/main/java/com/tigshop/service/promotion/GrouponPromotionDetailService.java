package com.tigshop.service.promotion;

import com.tigshop.bean.model.promotion.GrouponPromotionDetail;
import com.tigshop.service.common.BaseService;

/**
 * @author kidd
 * @description 针对表【groupon_promotion_detail(拼团信息)】的数据库操作Service
 * @createDate 2025-08-28 13:37:26
 */
public interface GrouponPromotionDetailService extends BaseService<GrouponPromotionDetail> {

    void decLimitNum(Integer quantity, Long grouponDetailId);

    void incLimitNum(Integer quantity, Long grouponDetailId);
}
