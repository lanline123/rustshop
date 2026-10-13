package com.tigshop.bean.vo.vendor.product;

import cn.hutool.core.collection.CollUtil;
import com.tigshop.bean.dto.product.ProductDescDTO;
import com.tigshop.bean.model.vendor.product.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.LinkedHashMap;
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
public class VendorProductDetailVO {

    // *** VendorProduct ***

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品品牌ID")
    private Integer brandId;

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

    @Schema(description = "商品审核状态；0-待审核，1-审核通过，2-审核失败")
    private Integer auditState;

    // *** Other ***

    @Schema(description = "商品详情")
    private List<ProductDescDTO> productDesc;

    @Schema(description = "商品相册")
    private List<VendorProductGalleryVO> galleries;

    @Schema(description = "商品视频")
    private List<VendorProductVideoVO> video;

    @Schema(description = "商品规格")
    private List<VendorProductSkuVO> skus;

    @Schema(description = "商品规格属性")
    private List<VendorProductSkuAttrVO> skuAttrs;

    @Schema(description = "审核失败原因")
    private String auditFailReason;

    public VendorProductDetailVO(VendorProduct vendorProduct, List<VendorProductGallery> galleries, List<VendorProductVideo> video, List<VendorProductSku> skus, List<VendorProductSkuAttr> skuAttrs, List<VendorProductAuditLog> productAuditLogs) {
        this.id = vendorProduct.getId();
        this.productName = vendorProduct.getProductName();
        this.brandId = vendorProduct.getProductBrandId();
        this.productCategoryId = vendorProduct.getProductCategoryId();
        this.productSnGenerateType = vendorProduct.getProductSnGenerateType();
        this.productBrief = vendorProduct.getProductBrief();
        this.productState = vendorProduct.getProductState();
        this.skuType = vendorProduct.getSkuType();
        this.productDesc = ProductDescDTO.getProductDescArr(vendorProduct.getProductDesc());

        this.galleries = galleries.stream().map(VendorProductGalleryVO::new).toList();

        if (CollUtil.isNotEmpty(video)) {
            this.video = video.stream().map(VendorProductVideoVO::new).toList();
        }

        this.skus = skus.stream().map(VendorProductSkuVO::new).toList();

        Map<String, List<VendorProductSkuAttr>> skuAttrsMap = skuAttrs.stream()
                .collect(Collectors.groupingBy(
                        VendorProductSkuAttr::getAttrName,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        this.skuAttrs = skuAttrsMap.entrySet().stream()
                .map(entry -> new VendorProductSkuAttrVO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(productAuditLogs)) {
            this.auditFailReason = productAuditLogs.getFirst().getAuditFailReason();
        }
    }
}
