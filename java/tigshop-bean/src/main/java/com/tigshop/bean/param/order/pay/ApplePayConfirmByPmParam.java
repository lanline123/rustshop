package com.tigshop.bean.param.order.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Apple Pay 按 paymentMethodId 确认请求参数（PSP 前端驱动模式，如 Stripe）。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "Apple Pay 按 paymentMethodId 确认请求参数")
public class ApplePayConfirmByPmParam {

    @NotBlank(message = "支付流水号不能为空")
    @Schema(description = "支付流水号；金额以服务端为准")
    private String paySn;

    @NotBlank(message = "paymentMethodId不能为空")
    @Schema(description = "前端 PSP SDK（如 Stripe.js）返回的 paymentMethodId")
    private String paymentMethodId;
}
