package com.tigshop.bean.model.vendor.product;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.common.core.entity.BasePO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "供应商商品规格属性")
@EqualsAndHashCode(callSuper = true)
@TableName(value ="vendor_product_sku_attr")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductSkuAttr extends BasePO implements Serializable {

    @Schema(description = "供应商商品 ID")
    private Long vendorProductId;

    @Schema(description = "属性类型，0-普通属性，1-规格属性，2-附加规格")
    private Integer attrType;

    @Schema(description = "属性所属类型的名称")
    private String attrName;

    @Schema(description = "具体属性的值")
    private String attrValue;

    @Schema(description = "属性价格")
    private BigDecimal attrPrice;

    @Schema(description = "属性颜色")
    private String attrColor;

    @Schema(description = "属性图片")
    private String attrPic;

    @Schema(description = "属性缩略图")
    private String attrPicThumb;

    @Schema(description = "供应商ID")
    private Integer vendorId;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}