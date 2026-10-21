package com.tigshop.bean.vo.vendor.product;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import com.tigshop.bean.model.vendor.product.VendorProduct;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.common.utils.SpecImportUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供应商商品导入导出 Excel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(30)
public class VendorProductImportExcelVO {

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

    @ExcelIgnore
    @Schema(description = "品牌ID")
    private Integer brandId;

    @ExcelIgnore
    @Schema(description = "分类ID")
    private Integer categoryId;

    public VendorProductImportExcelVO(VendorProduct vp, String categoryName, String brandName, VendorProductSku sku) {
        this.picUrl = vp.getPicUrl();
        this.productName = vp.getProductName();
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.productBrief = vp.getProductBrief();
        this.productState = vp.getProductState() != null ? vp.getProductState().toString() : "0";
        if (sku != null) {
            this.specName = SpecImportUtils.toImportSpecText(sku.getSkuAttrVal());
            this.skuSn = sku.getSkuSn();
            this.supplyPrice = sku.getSupplyPrice() != null ? sku.getSupplyPrice().toString() : null;
            this.supplyPriceLimitType = sku.getSupplyPriceLimitType() != null ? sku.getSupplyPriceLimitType().toString() : "1";
            this.supplyPriceLimitVal = sku.getSupplyPriceLimitVal() != null ? sku.getSupplyPriceLimitVal().toString() : "0";
            this.skuStock = sku.getSkuStock() != null ? sku.getSkuStock().toString() : "0";
        }
    }
}
