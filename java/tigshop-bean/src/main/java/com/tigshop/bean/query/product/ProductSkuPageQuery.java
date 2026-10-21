package com.tigshop.bean.query.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年10月30日 10:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "商品SKU分页查询参数")
public class ProductSkuPageQuery extends BasePage {
    @Schema(description = "商品分类ID")
    Integer categoryId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "商品类型（多选）")
    private List<Integer> productType;

    @Schema(description = "是否包含预售商品（1-是，0-否），默认为是")
    private Integer isSaleMode = 1;

    // *** 周期购相关字段 ***
    @Schema(description = "是否包含周期购商品（1-是，0-否）")
    private Integer isIncludeCycleProduct;

    // *** 排除商品类型相关字段 ***
    @Schema(description = "排除的商品类型（多选）")
    private List<Integer> excludeProductTypes;
}