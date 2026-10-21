package com.tigshop.bean.dto.order.deliver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop
 */
@Data
public class DeliverInfo {

    @Schema(description = "明细id")
    private Integer itemId;

    @Schema(description = "发货数量")
    private Integer toDeliveryQuantity;

}
