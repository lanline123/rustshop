// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品分类列表查询入参
 *
 * @author Jayce
 * @create 2024年10月31日 16:45
 */
@Getter
@Setter
@Schema(description = "商品分类列表查询入参")
public class CategoryListDTO extends BasePage {

    @Schema(description = "是否显示")
    private String isShow;

    @Schema(description = "父级id")
    private Integer parentId;
}
