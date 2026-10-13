package com.tigshop.bean.param.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改订单状态参数
 *
 * @author kidd
 * @since 2025/7/7 15:00
 */
@Data
public class OrderChangeStatusParam {

    @NotNull(message = "主键")
    @Schema(description = "主键")
    private Integer id;

    @NotNull(message = "字段不能为空")
    @Schema(description = "字段")
    private String field;

    @NotNull(message = "订单状态不能为空")
    @Schema(description = "订单的状态：0 待确定，待支付 1 已确定，待发货 2处理中，已发货 3 已取消 4 无效 5 已完成")
    private Integer val;

    @Schema(description = "备注")
    private String adminNote;
}
