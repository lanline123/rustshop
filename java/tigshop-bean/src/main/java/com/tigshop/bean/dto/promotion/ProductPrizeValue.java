package com.tigshop.bean.dto.promotion;

import lombok.Data;

/**
 * 商品奖品值解析结果
 * 用于解析抽奖商品奖品的prizeValue
 * 格式：{"productId":11715} 或 {"productId":11715,"skuId":1195}
 *
 * @author Tigshop团队
 * @create 2024年12月25日
 */
@Data
public class ProductPrizeValue {

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * SKU ID（多规格商品时有值）
     */
    private Integer skuId;

    public ProductPrizeValue() {
    }

    public ProductPrizeValue(Integer productId, Integer skuId) {
        this.productId = productId;
        this.skuId = skuId;
    }

    /**
     * 是否有SKU ID（判断是否为多规格商品）
     */
    public boolean hasSkuId() {
        return skuId != null && skuId > 0;
    }
}

