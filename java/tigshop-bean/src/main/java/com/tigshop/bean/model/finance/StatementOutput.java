package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * @author Tigshop项目组
 */
@Data
@TableName("statement_output")
public class StatementOutput {

    /**
     * 主键ID
     */
    @TableId(value = "statement_output_id", type = IdType.AUTO)
    private Long statementOutputId;

    /**
     * 店铺id
     */
    @TableField("shop_id")
    private Integer shopId;

    /**
     * 供应商id
     */
    @TableField("vendor_id")
    private Integer vendorId;

    /**
     * 收入金额
     */
    @TableField("income")
    private BigDecimal income;

    /**
     * 支出金额
     */
    @TableField("expenditure")
    private BigDecimal expenditure;

    /**
     * 出账时间
     */
    @TableField("gmt_create")
    private Long gmtCreate;

    /**
     * 单据sn
     */
    @TableField("record_sn")
    private String recordSn;

    /**
     * 单据类型
     */
    @TableField("record_type")
    private Integer recordType;

    /**
     * 单据id
     */
    @TableField("record_id")
    private Integer recordId;

    /**
     * 入账状态
     */
    @TableField("settlement_status")
    private Integer settlementStatus;

    private Integer realSettlement;

    private Integer realSettlementTime;
}
