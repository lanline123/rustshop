package com.tigshop.bean.vo.vendor.product;

import cn.hutool.core.collection.CollUtil;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.vendor.product.VendorProduct;
import com.tigshop.bean.model.vendor.product.VendorProductAuditLog;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 供应商商品列表
 *
 * @author kidd
 * @since 2025/7/10 09:48
 */
@Data
public class VendorProductPageVO {

    // *** VendorProduct ***

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商品缩略图片")
    private String picThumb;

    @Schema(description = "高清图")
    private String picLarge;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品状态；0-断供，1-在售")
    private Integer productState;

    @Schema(description = "商品审核状态；0-待审核，1-审核通过，2-审核失败")
    private Integer auditState;

    // *** VendorProductSku ***

    @Schema(description = "供货价")
    private BigDecimal supplyPrice;

    // *** Other ***

    @Schema(description = "规格总库存")
    private Integer sumSkuStock;

    @Schema(description = "规格总销量")
    private Integer sumSalesVolume;

    @Schema(description = "商品品牌名称")
    private String productBrandName;

    @Schema(description = "商品类目名称")
    private String productCategoryName;

    @Schema(description = "供货商商品规格")
    private List<VendorProductSkuVO> skus;

    @Schema(description = "审核失败原因")
    private String auditFailReason;

    public VendorProductPageVO(VendorProduct vendorProduct, List<VendorProductSku> vendorProductSkus, Brand brand, Category category, List<VendorProductAuditLog> productAuditLogs) {
        this.id = vendorProduct.getId();
        this.picThumb = vendorProduct.getPicThumb();
        this.productName = vendorProduct.getProductName();
        this.productState = vendorProduct.getProductState();
        this.auditState = vendorProduct.getAuditState();
        this.picLarge = vendorProduct.getPicLarge();

        // 获取供货价最小的供应商商品规格
        this.supplyPrice = vendorProductSkus.stream()
                .map(VendorProductSku::getSupplyPrice)
                .min(Comparator.comparing(BigDecimal::doubleValue)).orElse(null);

        this.sumSkuStock = vendorProductSkus.stream()
                .map(VendorProductSku::getSkuStock)
                .reduce(0, Integer::sum);

        this.sumSalesVolume = vendorProductSkus.stream()
                .map(VendorProductSku::getSalesVolume)
                .reduce(0, Integer::sum);

        if (brand != null) {
            this.productBrandName = brand.getBrandName();
        }

        if (category != null) {
            this.productCategoryName = category.getCategoryName();
        }

        this.skus = vendorProductSkus.stream()
                .map(VendorProductSkuVO::new)
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(productAuditLogs)) {
            this.auditFailReason = productAuditLogs.getFirst().getAuditFailReason();
        }
    }
}
