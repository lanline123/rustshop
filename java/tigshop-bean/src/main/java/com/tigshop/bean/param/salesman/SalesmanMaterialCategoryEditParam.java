package com.tigshop.bean.param.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.salesman.SalesmanMaterialCategoryConstants.SALESMAN_MATERIAL_CATEGORY_NOT_NULL;
import static com.tigshop.common.constant.salesman.SalesmanMaterialCategoryConstants.SALESMAN_MATERIAL_CATEGORY_OVER_LENGTH;

/**
 * 素材分类更新参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "素材分类参数")
public class SalesmanMaterialCategoryEditParam {
    @Schema(description = "素材分类id")
    private Integer categoryId;

    @Schema(description = "素材分类名称")
    @NotNull(message = SALESMAN_MATERIAL_CATEGORY_NOT_NULL)
    @Size(max = 50, message = SALESMAN_MATERIAL_CATEGORY_OVER_LENGTH)
    private String categoryName;

    @Schema(description = "素材分类排序")
    private Integer sortOrder;
}
