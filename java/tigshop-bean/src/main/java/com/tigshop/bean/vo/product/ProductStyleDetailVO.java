package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品样式详情VO")
@Data
public class ProductStyleDetailVO extends ProductStyleVO {

    @Schema(description = "关联商品ID列表")
    private List<Integer> productIds;

    @Schema(description = "关联分组ID列表")
    private List<Integer> groupIds;

    @Schema(description = "关联品牌ID列表")
    private List<Integer> brandIds;

    @Schema(description = "关联商品信息列表")
    private List<ProductItemVO> productList;

    @Schema(description = "关联分组信息列表")
    private List<GroupItemVO> groupList;

    @Schema(description = "关联品牌信息列表")
    private List<BrandItemVO> brandList;

    @Schema(description = "商品简要信息")
    @Data
    public static class ProductItemVO {

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品图片")
        private String picThumb;

        @Schema(description = "商品价格")
        private BigDecimal productPrice;
    }

    @Schema(description = "分组简要信息")
    @Data
    public static class GroupItemVO {

        @Schema(description = "分组ID")
        private Integer productGroupId;

        @Schema(description = "分组名称")
        private String productGroupName;
    }

    @Schema(description = "品牌简要信息")
    @Data
    public static class BrandItemVO {

        @Schema(description = "品牌ID")
        private Integer brandId;

        @Schema(description = "品牌名称")
        private String brandName;
    }
}
