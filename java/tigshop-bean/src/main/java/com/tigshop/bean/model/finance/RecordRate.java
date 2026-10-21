package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 记录费率表
 * @author Tigshop团队
 * @create 2025年08月03日 17:05
 */
@Data
@TableName("record_rate")
public class RecordRate {

    /**
     * record_rate_id
     */
    @TableId(value = "record_rate_id", type = IdType.AUTO)
    private Integer recordRateId;

    /**
     * 单据id
     */
    @TableField("record_id")
    private Integer recordId;

    /**
     * 单据类型
     */
    @TableField("record_type")
    private Integer recordType;

    /**
     * 店铺费率
     */
    @TableField("shop_service_fee")
    private BigDecimal shopServiceFee;

    /**
     * 店铺手续费
     */
    @TableField("shop_withdrawal_fee")
    private BigDecimal shopWithdrawalFee;

    /**
     * 门店服务费
     */
    @TableField("storefront_service_fee")
    private BigDecimal storefrontServiceFee;

    /**
     * 门店手续费
     */
    @TableField("storefront_withdrawal_fee")
    private BigDecimal storefrontWithdrawalFee;

    /**
     * 供应商服务费
     */
    @TableField("supplier_service_fee")
    private BigDecimal supplierServiceFee;

    /**
     * 供应商手续费
     */
    @TableField("supplier_withdrawal_fee")
    private BigDecimal supplierWithdrawalFee;

    /**
     * 按品类的费率明细(JSON)，订单类型写入；提现等为 null
     */
    @TableField("category_rate_details")
    private String categoryRateDetails;

    /**
     * 记录当前时间
     */
    @TableField("gmt_create")
    private String gmtCreate;

    private Integer shopId;

    private Integer vendorId;
}
