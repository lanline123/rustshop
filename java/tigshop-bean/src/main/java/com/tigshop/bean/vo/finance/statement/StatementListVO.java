package com.tigshop.bean.vo.finance.statement;

import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.core.BigDecimalSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * 对账单列表返回结果
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 14:55
 */
@RequiredArgsConstructor
@Schema(description = "对账单列表返回结果")
@Data
public class StatementListVO {
    /**
     * 店铺ID
     */
    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺名称")
    private String shopName;

    /**
     * 单据ID
     */
    @Schema(description = "单据号")
    private String recordSn;

    /**
     * 结算时间
     */
    @Schema(description = "结算时间")
    private String settlementTime;

    /**
     * 供应商id
     */
    @Schema(description = "供应商id")
    private Integer vendorId;

    /**
     * 供应商名称
     */
    @Schema(description = "供应商名称")
    private String vendorName;

    /**
     * 账户类型1.账户余额
     */
    @Schema(description = "账户类型1.账户余额")
    private Integer accountType;

    /**
     * 类型：1.手续费 2.店铺提现收支 3.店铺订单收支等
     */
    @Schema(description = "类型：1.手续费 2.店铺提现收支 3.店铺订单收支等")
    private Integer type;

    /**
     * 入账方式：1. 自动 2.手动
     */
    @Schema(description = "入账方式：1. 自动 2.手动")
    private String entryType;

    /**
     * 支付方式（如微信、支付宝等）
     */
    @Schema(description = "支付方式（如微信、支付宝等）")
    private String paymentType;

    /**
     * 账户余额
     */
    @Schema(description = "账户余额")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal accountBalance;

    /**
     * 交易金额（正为收入，负为支出）
     */
    @Schema(description = "交易金额（正为收入，负为支出）")
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal amount;

    /**
     * 结算状态
     */
    @Schema(description = "结算状态")
    private Integer settlementStatus;

    @Schema(description = "账户类型名称")
    @ExcelProperty("账户名称")
    private String accountTypeName;

    @Schema(description = "类型名称")
    @ExcelProperty("类型名称")
    private String typeName;

    @Schema(description = "入账方式名称")
    @ExcelProperty("入账方式名称")
    private String entryTypeName;

    @Schema(description = "支付方式名称")
    @ExcelProperty("支付方式名称")
    private String paymentTypeName;


    @Schema(description = "交易时间")
    @ExcelProperty("交易时间")
    private String recordTime;

}