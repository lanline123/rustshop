// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.salesman;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 素材分类model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("salesman_material_category")
@Schema(description = "素材分类")
public class SalesmanMaterialCategory {
    @TableId(value = "category_id", type = IdType.AUTO)
    @Schema(description = "素材分类ID")
    private Integer categoryId;

    @Schema(description = "名称")
    private String categoryName;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "所属店铺ID")
    private Integer shopId;

    @Schema(description = "排序")
    private Integer sortOrder;
}