package com.tigshop.bean.param.vendor.product;

import com.tigshop.bean.model.vendor.product.VendorProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商商品编辑参数
 *
 * @author kidd
 * @since 2025/7/10 09:38
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VendorProductEditParam extends VendorProductSaveParam {

    @Schema(description = "供应商商品ID")
    private Long id;


    @Override
    public VendorProduct createVendorProduct(String size400,String size800) {
        VendorProduct vendorProduct = super.createVendorProduct(size400,size800);
        vendorProduct.setIsRecycle(null);
        vendorProduct.setAuditState(null);

        vendorProduct.setId(this.id);
        return vendorProduct;
    }
}
