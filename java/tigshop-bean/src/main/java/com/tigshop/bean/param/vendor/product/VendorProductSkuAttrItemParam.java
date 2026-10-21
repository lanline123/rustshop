package com.tigshop.bean.param.vendor.product;

import com.alibaba.fastjson.annotation.JSONField;
import com.tigshop.bean.model.vendor.product.VendorProductSkuAttr;
import com.tigshop.common.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 供应商商品规格属性项参数
 *
 * @author kidd
 * @since 2025/7/10 09:11
 */
@Data
public class VendorProductSkuAttrItemParam {

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

    public VendorProductSkuAttr createVendorProductSkuAttr(Long vendorProductId) {
        return VendorProductSkuAttr.builder()
                .vendorProductId(vendorProductId)
                .attrType(this.attrType)
                .attrName(this.attrName)
                .attrValue(this.attrValue)
                .attrPrice(this.attrPrice)
                .attrColor(this.attrColor)
                .attrPic(this.attrPic)
                .attrPicThumb(this.attrPicThumb)
                .vendorId(HeaderUtils.getVendorId())
                .build();
    }
}
