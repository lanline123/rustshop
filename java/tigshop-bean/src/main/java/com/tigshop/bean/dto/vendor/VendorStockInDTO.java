package com.tigshop.bean.dto.vendor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 供应商入库/出库参数（入库与出库共用）
 *
 * @author Tigshop团队
 */
@Data
public class VendorStockInDTO {

    @Schema(description = "出入库子类型 11/12/10/21/22/23")
    private Integer type;

    @Schema(description = "备注")
    private String comment;

    @Schema(description = "出入库明细")
    private List<VendorStockRecordDetailParam> productStockRecordDetailList;

    @Schema(description = "出入库总数")
    private Integer productTotal;

    @Data
    public static class VendorStockRecordDetailParam {

        @Schema(description = "供应商商品id（vendor_product.id）")
        private Integer productId;

        @Schema(description = "供应商商品规格id（vendor_product_sku.id）")
        private Long skuId;

        @Schema(description = "出入库数量")
        private Integer quantity;
    }
}
