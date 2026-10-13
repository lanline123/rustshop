package com.tigshop.bean.vo.vendor.product;

import com.alibaba.fastjson.JSONArray;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供货商商品规格
 *
 * @author kidd
 * @since 2025/7/10 10:25
 */
@Data
public class VendorProductSkuVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商品规格值（拼接）")
    private String skuAttrVal;

    @Schema(description = "商品规格值（json）")
    private String skuAttrJson;

    @Schema(description = "规格编码")
    private String skuSn;

    @Schema(description = "规格条形码")
    private String skuTsn;

    @Schema(description = "规格重量")
    private BigDecimal skuWeight;

    @Schema(description = "规格库存")
    private Integer skuStock;

    @Schema(description = "供货价")
    private BigDecimal supplyPrice;

    @Schema(description = "限售金额类型；1-按金额，2-按比例")
    private Integer supplyPriceLimitType;

    @Schema(description = "限售金额值")
    private BigDecimal supplyPriceLimitVal;

    @Schema(description = "规格销量")
    private Integer salesVolume;

    // *** Other ***

    @Schema(description = "规格图片")
    private List<VendorProductSkuAttrItemVO> attrs;

    public VendorProductSkuVO(VendorProductSku vendorProductSku) {
        this.id = vendorProductSku.getId();
        this.skuAttrVal = vendorProductSku.getSkuAttrVal();
        this.skuAttrJson = vendorProductSku.getSkuAttrJson();
        this.skuSn = vendorProductSku.getSkuSn();
        this.skuTsn = vendorProductSku.getSkuTsn();
        this.skuWeight = vendorProductSku.getSkuWeight();
        this.skuStock = vendorProductSku.getSkuStock();
        this.supplyPrice = vendorProductSku.getSupplyPrice();
        this.supplyPriceLimitType = vendorProductSku.getSupplyPriceLimitType();
        this.supplyPriceLimitVal = vendorProductSku.getSupplyPriceLimitVal();
        this.salesVolume = vendorProductSku.getSalesVolume();

        this.attrs = JSONArray.parseArray(vendorProductSku.getSkuAttrJson(), VendorProductSkuAttrItemVO.class);
    }
}
