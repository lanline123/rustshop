// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品属性
 *
 * @author Tigshop团队
 * @create 2024年11月27日 14:30
 */
@Data
@Schema(description = "商品属性")
public class ProductAttributeDTO {

    @Schema(description = "自增ID号")
    private Integer attributesId;

    @Schema(description = "取值于goods的goods_id")
    private Integer productId;

    @Schema(description = "属性类型，0:普通属性，1：规格属性，2：附加规格")
    private Integer attrType;

    @Schema(description = "属性所属类型的名称")
    @JsonTranslate(dataType = 8)
    private String attrName;

    @Schema(description = "具体属性的值")
    @JsonTranslate(dataType = 8)
    private String attrValue;

    @Schema(description = "属性价格")
    private BigDecimal attrPrice;
    @JsonTranslate
    @Schema(description = "属性颜色")
    private String attrColor;

    @Schema(description = "属性图片")
    private String attrPic;

    @Schema(description = "属性缩略图")
    private String attrPicThumb;

    @Schema(description = "排序")
    private Integer sortOrder;
}

