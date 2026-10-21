package com.tigshop.bean.vo.vendor.product;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供应商商品导入 Excel 解析
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(30)
public class VendorProductImportExcelPrueVO {

    @ExcelProperty("商品图片")
    @Schema(description = "商品图片URL")
    private String picUrl;

    @ExcelProperty("商品名称（必须）")
    @Schema(description = "商品名称")
    private String productName;

    @ExcelProperty("分类（必须）")
    @Schema(description = "分类，多级用|分隔")
    private String categoryName;

    @ExcelProperty("品牌")
    @Schema(description = "品牌")
    private String brandName;

    @ExcelProperty("商品简介")
    @Schema(description = "商品简介")
    private String productBrief;

    @ExcelProperty("商品状态（默认0）")
    @Schema(description = "商品状态；0-断供，1-在售")
    private String productState;

    @ExcelProperty("商品规格")
    @Schema(description = "商品规格，多组用；分隔，形如 颜色：红色；尺码：37")
    private String specName;

    @ExcelProperty("商品规格图")
    @Schema(description = "商品规格图")
    private String specPicUrl;

    @ExcelProperty("规格编码")
    @Schema(description = "规格编码")
    private String skuSn;

    @ExcelProperty("供货价（必须）")
    @Schema(description = "供货价")
    private String supplyPrice;

    @ExcelProperty("限售金额类型（默认1）")
    @Schema(description = "限售金额类型；1-按金额，2-按比例")
    private String supplyPriceLimitType;

    @ExcelProperty("限售金额值（默认0）")
    @Schema(description = "限售金额值")
    private String supplyPriceLimitVal;

    @ExcelProperty("库存（默认0）")
    @Schema(description = "库存")
    private String skuStock;
}
