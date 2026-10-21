package com.tigshop.bean.param.promotion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 确认领取商品奖品参数
 *
 * @author Tigshop团队
 * @create 2024年12月16日
 */
@Data
@Schema(description = "确认领取商品奖品参数")
public class LotteryConfirmReceiveParam {

    /**
     * 中奖记录ID
     */
    @Schema(description = "中奖记录ID")
    @NotNull(message = "中奖记录ID不能为空")
    private Integer recordId;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    @NotNull(message = "订单ID不能为空")
    private Integer orderId;
}

