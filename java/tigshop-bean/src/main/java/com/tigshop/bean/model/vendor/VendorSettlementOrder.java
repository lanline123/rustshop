package com.tigshop.bean.model.vendor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 供应商结算订单表
 * @TableName vendor_settlement_order
 */
@TableName(value ="vendor_settlement_order")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorSettlementOrder {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer vendorSettlementOrderId;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 供应商ID
     */
    private Integer vendorId;

    /**
     * 收益总额
     */
    private BigDecimal amount;

    /**
     * 生成时间
     */
    private Long addTime;
}