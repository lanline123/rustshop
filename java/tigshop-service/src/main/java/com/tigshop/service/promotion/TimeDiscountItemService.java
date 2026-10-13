package com.tigshop.service.promotion;

import com.tigshop.bean.model.promotion.TimeDiscount;
import com.tigshop.bean.model.promotion.TimeDiscountItem;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 限时折扣商品服务
 *
 * @author kidd
 * @create 2025/7/1
 */
public interface TimeDiscountItemService extends BaseService<TimeDiscountItem> {

    List<TimeDiscountItem> getTimeDiscountItemByDiscountId(Integer productId);

    /**
     * 批量保存限时折扣商品
     */
    void saveBatchTimeDiscountItem(List<TimeDiscountItem> timeDiscountItems, TimeDiscount timeDiscount);

    /**
     * 批量更新限时折扣商品
     */
    void updateBatchTimeDiscountItem(List<TimeDiscountItem> timeDiscountItems, TimeDiscount timeDiscount);
}
