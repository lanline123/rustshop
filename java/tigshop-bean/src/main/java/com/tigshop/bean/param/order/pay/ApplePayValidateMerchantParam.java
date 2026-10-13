package com.tigshop.bean.param.order.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Apple Pay 商户校验请求参数。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "Apple Pay 商户校验请求参数")
public class ApplePayValidateMerchantParam {

    @NotBlank(message = "支付流水号不能为空")
    @Schema(description = "支付流水号")
    private String paySn;

    @NotBlank(message = "validationUrl不能为空")
    @Schema(description = "Apple 面板回调的 validationURL")
    private String validationUrl;

    @Schema(description = "前端 window.location.hostname")
    private String displayDomain;
}
