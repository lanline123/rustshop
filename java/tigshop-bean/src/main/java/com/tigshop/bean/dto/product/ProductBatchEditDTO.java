package com.tigshop.bean.dto.product;

import com.alibaba.fastjson.JSON;
import com.tigshop.bean.model.product.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品批量修改 入参
 *
 * @author kidd
 * @since 2025/3/27 13:20
 */
@Data
public class ProductBatchEditDTO {

    private List<EditItem> editItems;

    @Data
    public static class EditItem {
        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "分类ID")
        private Integer categoryId;

        @Schema(description = "品牌ID")
        private Integer brandId;

        @Schema(description = "商品编号")
        private String productSn;

        @Schema(description = "商品临时编号")
        private String productTsn;

        @Schema(description = "商品价格")
        private BigDecimal productPrice;

        @Schema(description = "市场价（划线价）")
        private BigDecimal marketPrice;

        @Schema(description = "成本价")
        private BigDecimal costPrice;

        @Schema(description = "运费模板ID")
        private Long shippingTplId;

        @Schema(description = "是否包邮")
        private Integer freeShipping;

        @Schema(description = "是否新品")
        private Integer isNew;

        @Schema(description = "是否精品")
        private Integer isBest;

        @Schema(description = "是否热销")
        private Integer isHot;

        @Schema(description = "排序")
        private Integer sortOrder;

        @Schema(description = "商品状态")
        private Integer productStatus;

        @Schema(description = "商品服务")
        private List<Integer> productServiceIds;
    }

    public List<Product> buildProducts() {
        return editItems.stream()
                .map(item -> {
                    Product.ProductBuilder builder = Product.builder()
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .categoryId(item.getCategoryId())
                            .brandId(item.getBrandId())
                            .productSn(item.getProductSn())
                            .productTsn(item.getProductTsn())
                            .productPrice(item.getProductPrice())
                            .marketPrice(item.getMarketPrice())
                            .shippingTplId(item.getShippingTplId())
                            .freeShipping(item.getFreeShipping())
                            .isNew(item.getIsNew())
                            .isBest(item.getIsBest())
                            .isHot(item.getIsHot())
                            .sortOrder(item.getSortOrder())
                            .productStatus(item.getProductStatus());
                    // 商品服务
                    if (item.getProductServiceIds() != null && !item.getProductServiceIds().isEmpty()) {
                        builder.productServiceIds(JSON.toJSONString(item.getProductServiceIds()));
                    }
                    return builder.build();
                })
                .toList();
    }

}
