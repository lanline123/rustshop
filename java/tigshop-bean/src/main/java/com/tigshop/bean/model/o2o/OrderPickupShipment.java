package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自提订单发货表
 * @author Tigshop团队
 * @create 2025年08月03日 17:04
 */
@Data
@TableName("order_pickup_shipment")
@NoArgsConstructor
public class OrderPickupShipment {

    /**
     * 发货ID
     */
    @TableId(value = "shipment_id", type = IdType.AUTO)
    private Integer shipmentId;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Integer orderId;

    /**
     * 门店ID
     */
    @TableField("shop_id")
    private Integer shopId;

    /**
     * 自提点ID
     */
    @TableField("pickup_id")
    private Integer pickupId;

    /**
     * 提货码
     */
    @TableField("pickup_sn")
    private String pickupSn;

    /**
     * 提货二维码
     */
    @TableField("pickup_qr_code")
    private String pickupQrCode;

    /**
     * 发货状态
     */
    @TableField("shipment_status")
    private Integer shipmentStatus;

    /**
     * 核销时间
     */
    @TableField("check_time")
    private Integer checkTime;

    public OrderPickupShipment(Integer orderId, Integer shopId) {
        this.orderId = orderId;
        this.shopId = shopId;
    }
}
