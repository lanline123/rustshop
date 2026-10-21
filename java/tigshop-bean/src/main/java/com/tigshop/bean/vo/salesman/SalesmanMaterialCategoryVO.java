// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 素材分类VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "素材分类参数")
public class SalesmanMaterialCategoryVO {
    @Schema(description = "素材分类ID")
    private Integer categoryId;

    @Schema(description = "素材分类名称")
    private String categoryName;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "素材分类排序")
    private Integer sortOrder;
}