package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 对账单
 *
 * @author Tigshop项目组
 * @create 2025年08月02日 10:17
 */
@Data
@Schema(description = "对账单")
@TableName(value ="statement")
@NoArgsConstructor
@AllArgsConstructor
public class Statement {
    /**
     * 主键ID
     */
    @TableId(value = "statement_id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long statementId;

    /**
     * 店铺ID
     */
    @Schema(description = "店铺ID")
    private Integer shopId;

    /**
     * 单据ID
     */
    @Schema(description = "单据ID")
    private Integer recordId;

    /**
     * 单据号
     */
    @Schema(description = "单据号")
    private String recordSn;

    /**
     * 结算时间
     */
    @Schema(description = "结算时间")
    private Long settlementTime;

    /**
     * 供应商id
     */
    @Schema(description = "供应商id")
    private Integer vendorId;

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
    private BigDecimal accountBalance;

    /**
     * 交易金额（正为收入，负为支出）
     */
    @Schema(description = "交易金额（正为收入，负为支出）")
    private BigDecimal amount;

    /**
     * 记录创建时间
     */
    @Schema(description = "记录创建时间")
    private String gmtCreate;

    /**
     * 当前记录年
     */
    @Schema(description = "当前记录年")
    private Integer statementYear;

    /**
     * 当前记录月
     */
    @Schema(description = "当前记录月")
    private Integer statementMonth;

    /**
     * 当前记录日
     */
    @Schema(description = "当前记录日")
    private Integer statementDay;

    /**
     * 结算状态
     */
    @Schema(description = "结算状态")
    private Integer settlementStatus;

    /**
     * 下单时间
     */
    @Schema(description = "下单时间")
    private Long recordTime;
}