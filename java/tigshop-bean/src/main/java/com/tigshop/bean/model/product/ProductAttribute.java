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

package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品属性
 *
 * @author Tigshop团队
 * @create 2024年11月27日 14:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品属性")
@TableName("product_attributes")
public class ProductAttribute implements Serializable {

    @Schema(description = "自增ID号")
    @TableId(value = "attributes_id", type = IdType.AUTO)
    private Integer attributesId;

    @TableField(value = "product_id")
    @Schema(description = "取值于goods的goods_id")
    private Integer productId;

    @TableField(value = "attr_type")
    @Schema(description = "属性类型，0:普通属性，1：规格属性，2：附加规格")
    private Integer attrType;

    @TableField(value = "attr_name")
    @Schema(description = "属性所属类型的名称")
    private String attrName;

    @TableField(value = "attr_value")
    @Schema(description = "具体属性的值")
    private String attrValue;

    @TableField(value = "attr_price")
    @Schema(description = "属性价格")
    private BigDecimal attrPrice;

    @TableField(value = "attr_color")
    @Schema(description = "属性颜色")
    private String attrColor;

    @TableField(value = "attr_pic")
    @Schema(description = "属性图片")
    private String attrPic;

    @TableField(value = "attr_pic_thumb")
    @Schema(description = "属性缩略图")
    private String attrPicThumb;

    @TableField(value = "sort_order")
    @Schema(description = "排序")
    private Integer sortOrder;
}

