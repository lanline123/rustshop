package com.tigshop.bean.dto.order.deliver;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量发货Excel导入导出DTO
 *
 * @author Tigshop团队
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "批量发货Excel导入导出DTO")
public class OrderDeliverExcelDTO {

    @Schema(description = "订单编号")
    @ExcelProperty(value = "订单编号")
    @ColumnWidth(25)
    private String orderSn;



    @Schema(description = "会员名称")
    @ExcelProperty(value = "会员名称")
    @ColumnWidth(15)
    private String username;

    @Schema(description = "收货人")
    @ExcelProperty(value = "收货人")
    @ColumnWidth(12)
    private String consignee;

    @Schema(description = "手机号")
    @ExcelProperty(value = "手机号")
    @ColumnWidth(15)
    private String mobile;

    @Schema(description = "收货地址")
    @ExcelProperty(value = "收货地址")
    @ColumnWidth(40)
    private String address;

    @Schema(description = "订单金额")
    @ExcelProperty(value = "订单金额")
    @ColumnWidth(12)
    private String totalAmount;

    @Schema(description = "商品名称")
    @ExcelProperty(value = "商品名称")
    @ColumnWidth(60)
    private String productName;

    @Schema(description = "商品规格")
    @ExcelProperty(value = "商品规格")
    @ColumnWidth(40)
    private String skuName;


    @Schema(description = "商品数量")
    @ExcelProperty(value = "商品数量")
    @ColumnWidth(20)
    private String quantity;

    @Schema(description = "发货数量")
    @ExcelProperty(value = "*发货数量")
    @ColumnWidth(15)
    private String deliveryQuantity;

    @Schema(description = "*配送方式")
    @ExcelProperty(value = "配送方式")
    @ColumnWidth(15)
    private String shippingMethod;

    @Schema(description = "*物流公司")
    @ExcelProperty(value = "物流公司")
    @ColumnWidth(15)
    private String logisticsName;

    @Schema(description = "*物流代号")
    @ExcelProperty(value = "物流代号")
    @ColumnWidth(15)
    private String logisticsCode;

    @Schema(description = "*物流单号")
    @ExcelProperty(value = "物流单号")
    @ColumnWidth(20)
    private String trackingNo;


    @Schema(description = "下单时间")
    @ExcelProperty(value = "下单时间")
    @ColumnWidth(20)
    private String addTime;
}
