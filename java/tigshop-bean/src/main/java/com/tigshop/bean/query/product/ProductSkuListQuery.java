package com.tigshop.bean.query.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品 SKU 分页列表查询参数（对应 detail.productList 的分页版本）
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "商品SKU分页列表查询参数")
public class ProductSkuListQuery {

    @Schema(description = "商品ID（productId），必填")
    private Integer id;

    @Schema(description = "页码，从1开始；为空默认1")
    private Integer page;

    @Schema(description = "每页数量；为空默认50，上限500")
    private Integer size;

    @Schema(description = "关键字：匹配 skuSn / skuTsn / skuValue（模糊）")
    private String keyword;

    @Schema(description = "库存下限（含），按 product_sku.sku_stock 过滤")
    private Integer stockMin;

    @Schema(description = "库存上限（含），按 product_sku.sku_stock 过滤")
    private Integer stockMax;

    @Schema(description = "排序字段：skuId / skuStock / skuPrice / skuSn；非法值回退 skuId")
    private String sortField;

    @Schema(description = "排序方向：asc / desc；为空默认 asc")
    private String sortOrder;
}
