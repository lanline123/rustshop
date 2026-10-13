package com.tigshop.bean.vo.product;


import cn.hutool.core.bean.BeanUtil;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author wzh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "产品信息")
public class ProductInfoVo extends Product {

    @Schema(description = "产品属性信息")
    private List<ProductSku> productSku;

    @Schema(description = "店铺")
    private Shop shop;

    public ProductInfoVo(Product product,List<ProductSku> productSku, Shop shop) {
        BeanUtil.copyProperties(product,this);
        this.productSku = productSku;
        this.shop = shop;
    }

}
