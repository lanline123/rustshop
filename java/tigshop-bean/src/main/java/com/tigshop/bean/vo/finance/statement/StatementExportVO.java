package com.tigshop.bean.vo.finance.statement;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.format.NumberFormat;
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
@Schema(description = "对账单导出返回结果")
@Data
@ColumnWidth(22)
public class StatementExportVO {

    @ExcelProperty("门店名称")
    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "单据号")
    @ExcelProperty("单据号")
    private String recordSn;

    @Schema(description = "供应商名称")
    @ExcelProperty("供应商名称")
    private String vendorName;

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

    @Schema(description = "账户余额")
    @ExcelProperty("账户余额")
    @NumberFormat("#.##")
    private BigDecimal accountBalance;

    @Schema(description = "交易金额（正为收入，负为支出）")
    @ExcelProperty("交易金额")
    @NumberFormat("#.##")
    private BigDecimal amount;

    @Schema(description = "交易时间")
    @ExcelProperty("交易时间")
    private String recordTime;

    @Schema(description = "入账时间")
    @ExcelProperty("入账时间")
    private String settlementTime;
}