package com.tigshop.bean.vo.product;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品批量导入 Excel 行（读取），列与 classpath 模板 {@code 多商户商品导入模板.xlsx} 一致。
 *
 * @author kidd
 * @since 2025/3/26 16:25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(30)
public class ProductImportExcelPrueVO {

    @ExcelProperty("商品编码")
    @Schema(description = "商品编码")
    private String productSn;

    @ExcelProperty("商品名称")
    @Schema(description = "商品名称")
    private String productName;

    @ExcelProperty("商品图片")
    @Schema(description = "商品图片")
    private String picUrl;

    @ExcelProperty("商品分类")
    @Schema(description = "商品分类")
    private String categoryName;

    @ExcelProperty("配送方式")
    @Schema(description = "配送方式")
    private String shippingType;

    @ExcelProperty("快递运费模板")
    @Schema(description = "快递运费模板")
    private String shippingTpl;

    @ExcelProperty("是否上架（默认1）")
    @Schema(description = "是否上架")
    private String productStatus;

    @ExcelProperty("商品规格")
    @Schema(description = "商品规格")
    private String specName;

    @ExcelProperty("商品规格图")
    @Schema(description = "商品规格图")
    private String specPicUrl;

    @ExcelProperty("规格编码")
    @Schema(description = "规格编码")
    private String skuSn;

    @ExcelProperty("规格条码")
    @Schema(description = "规格条码")
    private String skuTsn;

    @ExcelProperty("商品详情图")
    @Schema(description = "商品详情图")
    private String descPicUrl;

    @ExcelProperty("商品售价")
    @Schema(description = "商品售价")
    private String productPrice;

    @ExcelProperty("库存")
    @Schema(description = "库存")
    private String productStock;

    @ExcelProperty("市场价")
    @Schema(description = "市场价")
    private String marketPrice;

    @ExcelProperty("商品条码")
    @Schema(description = "商品条码")
    private String productTsn;

    @ExcelProperty("搜索关键词")
    @Schema(description = "搜索关键词")
    private String keywords;

    @ExcelProperty("商品描述")
    @Schema(description = "商品描述")
    private String productBrief;

    @ExcelProperty("成本价")
    @Schema(description = "成本价")
    private String costPrice;
}
