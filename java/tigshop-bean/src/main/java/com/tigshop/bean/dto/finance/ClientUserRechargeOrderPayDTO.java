package com.tigshop.bean.dto.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户充值金额快捷表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "用户充值金额快捷表参数")
public class ClientUserRechargeOrderPayDTO {
    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "用户id")
    private Integer userId;
}
