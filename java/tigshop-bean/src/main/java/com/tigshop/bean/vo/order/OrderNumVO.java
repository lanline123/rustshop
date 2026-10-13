package com.tigshop.bean.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单数量VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "订单数量VO")
public class OrderNumVO {
    @Schema(description = "待付款数量")
    private Long awaitPay;

    @Schema(description = "待发货数量")
    private Long awaitShipping;

    @Schema(description = "待收货数量")
    private Long awaitReceived;

    @Schema(description = "待评价数量")
    private Long awaitComment;

    @Schema(description = "订单完成数量")
    private Long orderCompleted;

    @Schema(description = "产品收藏数量")
    private Long productCollect;

    @Schema(description = "店铺收藏数量")
    private Long shopCollect;

    @Schema(description = "售后中订单数")
    private Long awaitAftersalesCollect;
}
