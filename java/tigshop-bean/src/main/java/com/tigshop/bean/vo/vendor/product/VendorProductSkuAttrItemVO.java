package com.tigshop.bean.vo.vendor.product;

import com.alibaba.fastjson.annotation.JSONField;
import com.tigshop.bean.model.vendor.product.VendorProductSkuAttr;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 供应商商品规格属性
 *
 * @author kidd
 * @since 2025/7/10 10:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductSkuAttrItemVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "属性类型，0-普通属性，1-规格属性，2-附加规格")
    private Integer attrType;

    @JSONField(name = "name")
    @Schema(description = "属性所属类型的名称")
    private String attrName;

    @JSONField(name = "value")
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

    public VendorProductSkuAttrItemVO(VendorProductSkuAttr attr) {
        this.id = attr.getId();
        this.attrType = attr.getAttrType();
        this.attrName = attr.getAttrName();
        this.attrValue = attr.getAttrValue();
        this.attrPrice = attr.getAttrPrice();
        this.attrColor = attr.getAttrColor();
        this.attrPic = attr.getAttrPic();
        this.attrPicThumb = attr.getAttrPicThumb();
    }
}
