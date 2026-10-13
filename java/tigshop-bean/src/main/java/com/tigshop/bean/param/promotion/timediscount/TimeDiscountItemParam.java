package com.tigshop.bean.param.promotion.timediscount;

import cn.hutool.core.collection.CollUtil;
import com.tigshop.bean.model.promotion.TimeDiscountItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 限时折扣活动商品信息
 *
 * @author kidd
 * @create 2025/7/1
 */
@Data
@Schema(description = "限时折扣活动商品信息")
public class TimeDiscountItemParam {

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "规格ID")
    private List<Integer> skuIds;

    @Schema(description = "促销价格，减的金额或打的折数")
    private BigDecimal value;

    @Schema(description = "1打折2减钱3促销")
    private Integer discountType;

    public TimeDiscountItem createTimeDiscountItem() {

        String skuIds = CollUtil.isNotEmpty(this.skuIds) ? this.skuIds.toString() : "[]";

        return TimeDiscountItem.builder()
                .productId(this.productId)
                .skuIds(skuIds)
                .value(this.value)
                .discountType(this.discountType)
                .build();
    }

}
