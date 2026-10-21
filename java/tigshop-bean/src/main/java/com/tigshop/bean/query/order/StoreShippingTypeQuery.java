package com.tigshop.bean.query.order;

import com.tigshop.bean.param.order.OrderCheckParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 获得店铺配送方式查询条件
 *
 * @author kidd
 * @since 2025/4/21 15:02
 */
@Data
@Schema(description = "获得店铺配送方式查询条件")
public class StoreShippingTypeQuery {

    @Schema(description = "地址ID")
    private Integer addressId;

    @Schema(description = "配送方式")
    private Map<Integer, OrderCheckParam.ShippingType> shippingType;

    @Schema(description = "支付方式ID")
    private Integer payTypeId;

    @Schema(description = "是否使用积分")
    private Integer usePoint;

    @Schema(description = "是否使用余额")
    private BigDecimal useBalance;

    @Schema(description = "使用的优惠券ID列表")
    private List<Integer> useCouponIds;

    @Schema(description = "备注")
    private String buyerNote;

    @Schema(description = "发票")
    private OrderCheckParam.InvoiceData invoiceData;

    @Schema(description = "流程类型")
    private Integer flowType;

    @Schema(description = "门店id")
    private Integer shopId;

    private Boolean excludeQuickProduct;

    @Schema(description = "用户ID")
    private Integer userId;
}
