package com.tigshop.bean.dto.product;

import com.tigshop.bean.model.product.ProductStockAllocateRecordDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ProductStockAllocateRecordCreateDTO {

    @Schema(description = "出库组织")
    private Integer outboundOrganizationId;

    @Schema(description = "入库组织")
    private Integer warehousingOrganizationId;

    @Schema(description = "出库备注")
    private String comment;

    @Schema(description = "调拨商品明细")
    private List<ProductStockAllocateRecordDetail> productStockAllocateRecordDetailList;
}
