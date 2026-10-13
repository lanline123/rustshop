package com.tigshop.bean.vo.vendor.product;

import com.tigshop.bean.model.vendor.product.VendorProductSkuAttr;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 供应商商品规格属性
 *
 * @author kidd
 * @since 2025/7/10 10:54
 */
@Data
public class VendorProductSkuAttrVO {

    @Schema(description = "属性所属类型的名称")
    private String attrName;

    @Schema(description = "属性列表")
    private List<VendorProductSkuAttrItemVO> attrs;

    public VendorProductSkuAttrVO(String attrName, List<VendorProductSkuAttr> attrs) {
        this.attrName = attrName;
        this.attrs = attrs.stream()
                .map(VendorProductSkuAttrItemVO::new)
                .toList();
    }
}
