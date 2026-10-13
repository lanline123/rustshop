package com.tigshop.bean.model.vendor.product;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.core.entity.BasePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "供货商商品规格")
@EqualsAndHashCode(callSuper = true)
@TableName(value ="vendor_product_sku")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductSku extends BasePO implements Serializable {

    @Schema(description = "自建供应商关联的来源SKU ID；单规格商品为0")
    private Integer sourceSkuId;

    @Schema(description = "最低采购数量")
    private Integer minPurchaseQty;

    @Schema(description = "供应商商品 ID")
    private Long vendorProductId;

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

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public BigDecimal getMaxPrice() {
        if (this.supplyPriceLimitType == 1) {
            return this.supplyPrice.add(this.supplyPriceLimitVal);
        }

        if (this.supplyPriceLimitType == 2) {
            BigDecimal limitPrice = this.supplyPrice.multiply(this.supplyPriceLimitVal);
            return this.supplyPrice.add(limitPrice);
        }

        return this.supplyPrice;
    }
}
