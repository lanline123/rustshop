package com.tigshop.bean.dto.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductStockAllocateRecordDTO extends BasePage {

    @Schema(description = "出库组织")
    private Integer outboundOrganizationId;

    @Schema(description = "入库组织")
    private Integer warehousingOrganizationId;

    @Schema(description = "经手人")
    private String addBy;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "调拨单状态")
    private Integer status;
}
