// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.product;

import com.tigshop.bean.dto.product.ProductSearchClientDTO;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 商品分组VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "商品分类筛选返回的筛选参数")
public class ProductSearchFilterSelectedVO {
    @Schema(description = "类型")
    private String intro;

    @Schema(description = "关键词")
    private String keyword;

    @JsonTranslate(dataType = 0)
    @Schema(description = "分类名")
    private String category;

    @Schema(description = "品牌名")
    private String brand;

    @Schema(description = "店铺分类名")
    private String shopCategory;

    @Schema(description = "属性")
    private List<ProductSearchClientDTO.AttrObj> attrs;

}