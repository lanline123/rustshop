package com.tigshop.bean.bo.vendort;

import com.tigshop.bean.enums.product.CheckStatus;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.model.product.*;
import com.tigshop.bean.model.vendor.product.*;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商产品
 *
 * @author kidd
 * @since 2025/7/16 17:21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductBO {

    @Schema(description = "供应商产品")
    private VendorProduct vendorProduct;

    @Schema(description = "供应商产品视频")
    private VendorProductVideo video;

    @Schema(description = "供应商产品图片")
    private List<VendorProductGallery> galleries;

    @Schema(description = "供应商产品属性")
    private List<VendorProductSkuAttr> attrs;

    @Schema(description = "供应商产品SKU")
    private List<VendorProductSku> skus;

    @Schema(description = "店铺产品")
    private Product product;

    @Schema(description = "店铺产品视频")
    private ProductVideo productVideo;

    @Schema(description = "店铺产品图片")
    private List<ProductGallery> productGalleries;

    @Schema(description = "店铺产品属性")
    private List<ProductAttribute> productAttributes;

    @Schema(description = "店铺产品规格")
    private List<ProductSku> productSkus;

    public VendorProductBO(VendorProduct vendorProduct, VendorProductVideo video, List<VendorProductGallery> galleries, List<VendorProductSkuAttr> attrs, List<VendorProductSku> skus) {
        this.vendorProduct = vendorProduct;
        this.video = video;
        this.galleries = galleries;
        this.attrs = attrs;
        this.skus = skus;
    }

    public void transferInit() {
        this.toProduct();
        this.toProductVideo();
        this.toProductGalleries();
        this.toProductAttributes();
        this.toProductSkus();
    }

    public void toProduct() {
        Integer productStock = skus.stream().map(VendorProductSku::getSkuStock).reduce(0, Integer::sum);
        BigDecimal productPrice = skus.getFirst().getSupplyPrice();
        Long currentTime = StringUtils.getCurrentTime();

        this.product = Product.builder()
                .productName(this.vendorProduct.getProductName())
                .productTsn(null)
                .productStock(productStock)
                .productPrice(productPrice)
                .marketPrice(null)
                .shippingTplId(null)
                .productStatus(Constants.NO)
                .productType(ProductType.PRODUCT_TYPE_NORMAL.getCode())
                .categoryId(this.vendorProduct.getProductCategoryId())
                .brandId(this.vendorProduct.getProductBrandId())
                .shopId(HeaderUtils.getShopId())
                .keywords(null)
                .shopCategoryId(null)
                .checkStatus(CheckStatus.APPROVED.getCode())
                .checkReason(null)
                .clickCount(null)
                .productWeight(null)
                .isPromote(null)
                .isPromoteActivity(null)
                .promotePrice(null)
                .promoteStartDate(null)
                .promoteEndDate(null)
                .seckillMaxNum(null)
                .productBrief(this.vendorProduct.getProductBrief())
                .productDesc(this.vendorProduct.getProductDesc())
                .picUrl(this.vendorProduct.getPicUrl())
                .picThumb(this.vendorProduct.getPicUrl())
                .picOriginal(this.vendorProduct.getPicUrl())
                .commentTag(null)
                .freeShipping(null)
                .integral(null)
                .addTime(currentTime)
                .sortOrder(null)
                .storeSortOrder(null)
                .isDelete(Constants.NO)
                .isBest(null)
                .isNew(null)
                .isHot(null)
                .lastUpdate(currentTime)
                .remark(null)
                .giveIntegral(null)
                .rankIntegral(null)
                .suppliersId(null)
                .virtualSales(null)
                .limitNumber(null)
                .productCare(null)
                .productRelated(null)
                .productServiceIds(null)
                .isSupportReturn(null)
                .isSupportCod(null)
                .productVideo(null)
                .prepayPrice(null)
                .cardGroupId(null)
                .virtualSample(null)
                .paidContent(null)
                .noShipping(null)
                .fixedShippingType(1)
                .fixedShippingFee(BigDecimal.ZERO)
                .vendorProductId(this.vendorProduct.getId().intValue())
                .vendorId(this.vendorProduct.getVendorId())
                .build();
    }

    public void toProductVideo() {
        if (this.video != null) {
            this.productVideo = ProductVideo.builder()
                    .videoId(this.video.getId().intValue())
                    .videoUrl(this.video.getVideoUrl())
                    .videoCover(this.video.getVideoCover())
                    .format(this.video.getFormat())
                    .build();
        }
    }

    public void toProductGalleries() {
        this.productGalleries = this.galleries.stream()
                .map(gallery -> ProductGallery.builder()
                        .picUrl(gallery.getPicUrl())
                        .picDesc(gallery.getPicDesc())
                        .picThumb(gallery.getPicThumb())
                        .picOriginal(gallery.getPicOriginal())
                        .picLarge(gallery.getPicLarge())
                        .sortOrder(gallery.getSortOrder())
                        .build())
                .toList();
    }

    public void toProductAttributes() {
        if (this.attrs != null) {
            this.productAttributes = this.attrs.stream()
                    .map(attr -> ProductAttribute.builder()
                            .attrType(attr.getAttrType())
                            .attrName(attr.getAttrName())
                            .attrValue(attr.getAttrValue())
                            .attrPrice(attr.getAttrPrice())
                            .attrColor(attr.getAttrColor())
                            .attrPic(attr.getAttrPic())
                            .attrPicThumb(attr.getAttrPicThumb())
                            .build()
                    )
                    .toList();
        }
    }

    public void toProductSkus() {
        this.productSkus = this.skus.stream()
                .map(sku -> ProductSku.builder()
                        .skuValue(sku.getSkuAttrVal())
                        .skuData(sku.getSkuAttrJson())
                        .skuSn(sku.getSkuSn())
                        .skuStock(sku.getSkuStock())
                        .skuTsn(sku.getSkuTsn())
                        .skuPrice(sku.getSupplyPrice())
                        .marketPrice(BigDecimal.ZERO)
                        .costPrice(sku.getSupplyPrice())
                        .vendorProductSkuId(sku.getId().intValue())
                        .build()
                )
                .toList();
    }
}
