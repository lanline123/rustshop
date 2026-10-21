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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

import static com.tigshop.common.constant.product.AttributesTplConstants.TPL_NAME_IS_NULL;
import static com.tigshop.common.constant.product.AttributesTplConstants.TPL_NAME_OVER_LENGTH;

/**
 * 商品属性模板参数
 *
 * @author Jayce
 * @create 2024年11月04日 16:29
 */
@Data
@Schema(description = "商品属性模板参数")
public class AttributesTplDTO implements Serializable {

    @Schema(description = "商品属性模板表ID")
    private Integer tplId;

    @Schema(description = "模板名称")
    @NotNull(message = TPL_NAME_IS_NULL)
    @Size(max = 100, message = TPL_NAME_OVER_LENGTH)
    private String tplName;

    @Schema(description = "模板数据")
    private AttributeTplDataDTO tplData;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "关联商品类目ID")
    @NotNull(message = "商品类目不能为空")
    private Integer categoryId;
}
