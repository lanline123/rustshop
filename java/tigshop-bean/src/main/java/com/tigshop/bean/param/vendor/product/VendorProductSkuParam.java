package com.tigshop.bean.param.vendor.product;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.common.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 供应商商品规格参数
 *
 * @author kidd
 * @since 2025/7/10 09:07
 */
@Data
public class VendorProductSkuParam {

    // *** Other ***

    @Schema(description = "规格编码")
    private String skuSn;

    @Schema(description = "规格条形码")
    private String skuTsn;

    @Schema(description = "规格重量")
    private BigDecimal skuWeight;

    @NotNull(message = "规格库存不能为空")
    @Schema(description = "规格库存")
    private Integer skuStock;

    @NotNull(message = "供货价不能为空")
    @Schema(description = "供货价")
    private BigDecimal supplyPrice;

    @NotNull(message = "限售金额类型不能为空")
    @Schema(description = "限售金额类型；1-按金额，2-按比例")
    private Integer supplyPriceLimitType;

    @NotNull(message = "限售金额值不能为空")
    @Schema(description = "限售金额值")
    private BigDecimal supplyPriceLimitVal;

    // *** Other ***

    @Schema(description = "商品规格属性")
    private List<VendorProductSkuAttrItemParam> attrs;

    public VendorProductSku createVendorProductSku(Long vendorProductId) {
        String skuAttrVal = "";
        String skuAttrJson = null;

        if (CollUtil.isNotEmpty(this.attrs)) {
            skuAttrVal = this.attrs.stream()
                    .map(skuAttr -> StrUtil.format("{}:{}", skuAttr.getAttrName(), skuAttr.getAttrValue()))
                    .collect(Collectors.joining("|"));

            skuAttrJson = JSONArray.toJSONString(this.attrs);
        }

        return VendorProductSku.builder()
                .vendorProductId(vendorProductId)
                .skuAttrVal(skuAttrVal)
                .skuAttrJson(skuAttrJson)
                .skuSn(this.skuSn)
                .skuTsn(this.skuTsn)
                .skuWeight(this.skuWeight)
                .skuStock(this.skuStock)
                .supplyPrice(this.supplyPrice)
                .supplyPriceLimitType(this.supplyPriceLimitType)
                .supplyPriceLimitVal(this.supplyPriceLimitVal)
                .salesVolume(0)
                .vendorId(HeaderUtils.getVendorId())
                .build();
    }
}
