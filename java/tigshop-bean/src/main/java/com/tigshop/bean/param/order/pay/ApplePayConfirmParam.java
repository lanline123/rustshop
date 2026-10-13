package com.tigshop.bean.param.order.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Apple Pay 支付确认请求参数。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "Apple Pay 支付确认请求参数")
public class ApplePayConfirmParam {

    @NotBlank(message = "支付流水号不能为空")
    @Schema(description = "支付流水号；金额、币种一律以服务端为准")
    private String paySn;

    @Schema(description = "ApplePayPayment.token 原样透传（含加密 paymentData）")
    private Object paymentToken;
}
