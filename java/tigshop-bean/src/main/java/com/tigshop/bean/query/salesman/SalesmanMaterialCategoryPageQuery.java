package com.tigshop.bean.query.salesman;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 素材分类列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "素材分类列表参数")
public class SalesmanMaterialCategoryPageQuery extends BasePage {
    @Schema(description = "分类名称")
    private String categoryName;
}
