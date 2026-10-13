package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop项目组
 * @create 2025年10月30日 09:54
 */
@Data
@Schema(description = "商品SKU信息")
public class ProductSkuVO {

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品分类ID")
    private Integer categoryId;

    @Schema(description = "商品图片")
    private String picUrl;

    @Schema(description = "商品SKU ID")
    private Integer skuId;

    @Schema(description = "商品SKU数据")
    private String skuData;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "商品SKU价格")
    private Integer skuPrice;

    @Schema(description = "商品价格")
    private Integer productPrice;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "货品库存")
    private Integer skuStock;


}