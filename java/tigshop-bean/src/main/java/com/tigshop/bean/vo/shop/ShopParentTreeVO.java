package com.tigshop.bean.vo.shop;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
/**
 * 店铺表父级树VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "父级分类")
public class ShopParentTreeVO {
    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "父级分类")
    private Integer parentId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "子级分类")
    private List<ShopParentTreeVO> catList;
}
