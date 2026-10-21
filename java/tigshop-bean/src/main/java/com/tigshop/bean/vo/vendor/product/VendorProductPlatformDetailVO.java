package com.tigshop.bean.vo.vendor.product;

import com.tigshop.bean.model.vendor.product.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 供应商商品详情
 *
 * @author kidd
 * @since 2025/7/10 09:48
 */
@Data
public class VendorProductPlatformDetailVO {

    // *** VendorProduct ***

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品品牌ID")
    private Integer productBrandId;

    @Schema(description = "商品类目ID")
    private Integer productCategoryId;

    @Schema(description = "商品编码生成类型；1-系统生成，2-手动输入")
    private Integer productSnGenerateType;

    @Schema(description = "商品描述")
    private String productBrief;

    @Schema(description = "商品状态；0-断供，1-在售")
    private Integer productState;

    @Schema(description = "规格类型，1-单规格，2-多规格")
    private Integer skuType;

    @Schema(description = "商品详情")
    private String productDesc;

    // *** Other ***

    @Schema(description = "商品相册")
    private List<VendorProductGalleryVO> galleries;

    @Schema(description = "商品视频")
    private VendorProductVideoVO video;

    @Schema(description = "商品规格")
    private List<VendorProductSkuVO> skus;

    @Schema(description = "商品规格属性")
    private List<VendorProductSkuAttrVO> skuAttrs;

    public VendorProductPlatformDetailVO(VendorProduct vendorProduct, List<VendorProductGallery> galleries, VendorProductVideo video, List<VendorProductSku> skus, List<VendorProductSkuAttr> skuAttrs) {
        this.id = vendorProduct.getId();
        this.productName = vendorProduct.getProductName();
        this.productBrandId = vendorProduct.getProductBrandId();
        this.productCategoryId = vendorProduct.getProductCategoryId();
        this.productSnGenerateType = vendorProduct.getProductSnGenerateType();
        this.productBrief = vendorProduct.getProductBrief();
        this.productState = vendorProduct.getProductState();
        this.skuType = vendorProduct.getSkuType();
        this.productDesc = vendorProduct.getProductDesc();

        this.galleries = galleries.stream().map(VendorProductGalleryVO::new).toList();
        this.video = video != null ? new VendorProductVideoVO(video) : null;
        this.skus = skus.stream().map(VendorProductSkuVO::new).toList();

        Map<String, List<VendorProductSkuAttr>> skuAttrsMap = skuAttrs.stream().collect(Collectors.groupingBy(VendorProductSkuAttr::getAttrName, Collectors.toList()));
        this.skuAttrs = skuAttrsMap.entrySet().stream()
                .map(entry -> new VendorProductSkuAttrVO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }
}
