package com.tigshop.bean.dto.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺商品分类表更新DTO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "店铺商品分类表添加DTO")
public class ShopProductCategoryUpdateDTO {
    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "父级分类ID")
    private Integer parentId;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "图片")
    private String categoryPic;
}
