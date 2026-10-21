// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.salesman.SalesmanMaterialCategoryConstants.*;

/**
 * 素材分类创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "素材分类参数")
public class SalesmanMaterialCategoryCreateDTO {
    @Schema(description = "素材分类名称")
    @NotNull(message = SALESMAN_MATERIAL_CATEGORY_NOT_NULL)
    @Size(max = 50, message = SALESMAN_MATERIAL_CATEGORY_OVER_LENGTH)
    private String categoryName;

    @Schema(description = "素材分类排序")
    private Integer sortOrder;
}
