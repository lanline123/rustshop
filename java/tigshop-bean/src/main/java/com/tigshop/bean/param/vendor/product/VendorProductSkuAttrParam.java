package com.tigshop.bean.param.vendor.product;

import com.tigshop.bean.model.vendor.product.VendorProductSkuAttr;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 供应商商品规格属性参数
 *
 * @author kidd
 * @since 2025/7/10 09:11
 */
@Data
public class VendorProductSkuAttrParam {

    @NotBlank(message = "属性名称不能为空")
    @Schema(description = "属性所属类型的名称")
    private String attrName;

    @NotEmpty(message = "属性列表不能为空")
    @Schema(description = "属性列表")
    private List<VendorProductSkuAttrItemParam> attrs;

    public List<VendorProductSkuAttr> createVendorProductSkuAttr(Long vendorProductId) {
        return attrs.stream()
                .map(attr -> attr.createVendorProductSkuAttr(vendorProductId))
                .toList();
    }
}
