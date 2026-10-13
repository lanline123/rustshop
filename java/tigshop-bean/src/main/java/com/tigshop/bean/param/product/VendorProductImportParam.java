package com.tigshop.bean.param.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 供应商商品导入
 *
 * @author kidd
 * @since 2025/7/16 16:22
 */
@Data
public class VendorProductImportParam {

    @NotEmpty(message = "供应商商品ID不能为空")
    @Schema(description = "供应商商品ID")
    private List<Long> vendorProductIds;
}
