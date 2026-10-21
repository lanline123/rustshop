package com.tigshop.bean.vo.order;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量发货导入失败数据（用于导出错误Excel）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(25)
public class OrderDeliverImportErrorExcelVO {

    @Schema(description = "Excel行号（从2开始，第1行为表头）")
    @ExcelProperty(value = "行号")
    @ColumnWidth(10)
    private Integer rowNum;

    @Schema(description = "订单编号")
    @ExcelProperty(value = "订单编号")
    @ColumnWidth(25)
    private String orderSn;

    @Schema(description = "商品名称")
    @ExcelProperty(value = "商品名称")
    @ColumnWidth(40)
    private String productName;

    @Schema(description = "商品规格")
    @ExcelProperty(value = "商品规格")
    @ColumnWidth(30)
    private String skuName;

    @Schema(description = "发货数量")
    @ExcelProperty(value = "发货数量")
    @ColumnWidth(15)
    private String deliveryQuantity;

    @Schema(description = "配送方式")
    @ExcelProperty(value = "配送方式")
    @ColumnWidth(18)
    private String shippingMethod;

    @Schema(description = "物流公司")
    @ExcelProperty(value = "物流公司")
    @ColumnWidth(20)
    private String logisticsName;

    @Schema(description = "物流单号")
    @ExcelProperty(value = "物流单号")
    @ColumnWidth(25)
    private String trackingNo;

    @Schema(description = "错误信息")
    @ExcelProperty(value = "错误信息")
    @ColumnWidth(40)
    private String errorMsg;
}
