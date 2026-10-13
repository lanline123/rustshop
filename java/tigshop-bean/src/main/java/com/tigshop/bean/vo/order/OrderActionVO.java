package com.tigshop.bean.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单操作权限VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "订单操作权限参数")
public class OrderActionVO {
    @Schema(description = "取消订单")
    private Boolean cancelOrder;

    @Schema(description = "确认收货")
    private Boolean confirmReceipt;

    @Schema(description = "删除订单")
    private Boolean delOrder;

    @Schema(description = "发货")
    private Boolean deliver;

    @Schema(description = "修改订单")
    private Boolean modifyOrder;

    @Schema(description = "修改订单收货人")
    private Boolean modifyOrderConsignee;

    @Schema(description = "修改订单金额")
    private Boolean modifyOrderMoney;

    @Schema(description = "修改订单商品")
    private Boolean modifyOrderProduct;

    @Schema(description = "修改配送信息")
    private Boolean modifyShippingInfo;

    @Schema(description = "再次购买")
    private Boolean rebuy;

    @Schema(description = "设置已确认")
    private Boolean setConfirm;

    @Schema(description = "设置已支付")
    private Boolean setPaid;

    @Schema(description = "设置未支付")
    private Boolean setUnpaid;

    @Schema(description = "拆分订单")
    private Boolean splitOrder;

    @Schema(description = "转售后")
    private Boolean toAftersales;

    @Schema(description = "转评论")
    private Boolean toComment;

    @Schema(description = "转支付")
    private Boolean toPay;

    public OrderActionVO() {
        this.cancelOrder = false;
        this.confirmReceipt = false;
        this.delOrder = false;
        this.deliver = false;
        this.modifyOrder = false;
        this.modifyOrderConsignee = false;
        this.modifyOrderMoney = false;
        this.modifyOrderProduct = false;
        this.modifyShippingInfo = false;
        this.rebuy = false;
        this.setConfirm = false;
        this.setPaid = false;
        this.setUnpaid = false;
        this.splitOrder = false;
        this.toAftersales = false;
        this.toComment = false;
        this.toPay = false;
    }
}
