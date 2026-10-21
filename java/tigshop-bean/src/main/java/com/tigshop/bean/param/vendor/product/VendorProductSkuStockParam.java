package com.tigshop.bean.param.vendor.product;

import com.tigshop.bean.enums.vendor.VendorProductSkuStockBizEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 供应商商品规格库存变更参数
 *
 * @author kidd
 * @since 2025/7/14 11:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorProductSkuStockParam {

    @Schema(description = "供应商商品规格 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long vendorProductSkuId;

    @Schema(description = "变动数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer changeNum;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private VendorProductSkuStockBizEnum bizType;

    @Schema(description = "业务备注", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String bizRemark;

}
