package com.tigshop.bean.vo.vendor.product;

import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.model.vendor.product.VendorProduct;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.common.constant.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * 供应商商品客户端列表
 *
 * @author kidd
 * @since 2025/7/16 13:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductClientPageVO {

    // *** VendorProduct ***

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商品缩略图片")
    private String picThumb;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品状态；0-断供，1-在售")
    private Integer productState;

    // *** VendorProductSku ***

    @Schema(description = "供货价")
    private BigDecimal supplyPrice;

    // *** Vendor ***

    @Schema(description = "供应商名称")
    private String vendorName;

    // *** Product ***

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品状态; 0-下架，1-上架")
    private Integer productStatus;

    @Schema(description = "是否需要重新导入供应商商品")
    private Integer isNeedReImportVendorProduct;

    // *** Other ***

    @Schema(description = "商品类目名称")
    private String productCategoryName;

    @Schema(description = "是否已导入；0-否，1-是")
    private Integer isImported;

    public VendorProductClientPageVO(VendorProduct vendorProduct, List<VendorProductSku> vendorProductSkus, Vendor vendor, Category category, List<Long> vendorProductIds, Product product) {
        this.id = vendorProduct.getId();
        this.picThumb = vendorProduct.getPicThumb();
        this.productName = vendorProduct.getProductName();
        this.productState = vendorProduct.getProductState();

        // 获取供货价最小的供应商商品规格
        this.supplyPrice = vendorProductSkus.stream()
                .map(VendorProductSku::getSupplyPrice)
                .min(Comparator.comparing(BigDecimal::doubleValue)).orElse(null);

        this.vendorName = vendor.getVendorName();

        if (product != null) {
            this.productId = product.getProductId();
            this.productStatus = product.getProductStatus();
            this.isNeedReImportVendorProduct = product.getIsNeedReImportVendorProduct();
        } else {
            this.productStatus = Constants.NO;
            this.isNeedReImportVendorProduct = Constants.NO;
        }

        if (category != null) {
            this.productCategoryName = category.getCategoryName();
        }

        this.isImported = vendorProductIds.contains(vendorProduct.getId()) ? Constants.YES : Constants.NO;

    }
}
