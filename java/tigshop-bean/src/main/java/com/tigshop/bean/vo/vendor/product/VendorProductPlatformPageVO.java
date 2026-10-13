package com.tigshop.bean.vo.vendor.product;

import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.vendor.Vendor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 平台端-供应商商品列表
 *
 * @author kidd
 * @since 2025/7/10 09:48
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductPlatformPageVO {

    // *** VendorProduct ***

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商品品牌ID")
    private Integer productBrandId;

    @Schema(description = "商品类目ID")
    private Integer productCategoryId;

    @Schema(description = "商品缩略图片")
    private String picThumb;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品状态；0-断供，1-在售")
    private Integer productState;

    @Schema(description = "商品审核状态；0-待审核，1-审核通过，2-审核失败")
    private Integer auditState;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    // *** Other ***

    @Schema(description = "供货价")
    private BigDecimal supplyPrice;

    @Schema(description = "规格总库存")
    private Integer sumSkuStock;

    @Schema(description = "规格总销量")
    private Integer sumSalesVolume;

    @Schema(description = "供应商名称")
    private String vendorName;

    @Schema(description = "商品品牌名称")
    private String productBrandName;

    @Schema(description = "商品类目名称")
    private String productCategoryName;

    public void assembleData(Brand brand, Category category, Vendor vendor) {
        if (brand != null) {
            this.productBrandName = brand.getBrandName();
        }

        if (category != null) {
            this.productCategoryName = category.getCategoryName();
        }

        if (vendor != null) {
            this.vendorName = vendor.getVendorName();
        }
    }
}
