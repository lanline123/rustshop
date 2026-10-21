package com.tigshop.bean.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * orderId
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Data
@Schema(description = "orderId")
public class OrderIdDTO {

    @Schema(description = "订单ID")
    private Integer id;

    @Schema(description = "取消原因")
    private String description;

}
