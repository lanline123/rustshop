package com.tigshop.bean.vo.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺商品分类VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "店铺商品分类VO")
public class ShopProductCategoryVO {
    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "父级分类")
    private Integer parentId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "是否有子类")
    private Integer hasChildren;

    @Schema(description = "是否显示")
    private Integer isShow;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "图片")
    private String categoryPic;
}
