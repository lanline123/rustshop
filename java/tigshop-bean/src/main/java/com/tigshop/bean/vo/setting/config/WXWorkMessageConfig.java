package com.tigshop.bean.vo.setting.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class WXWorkMessageConfig {
    @Schema(description = "微信企业号webhookUrl")
    @NotEmpty(message = "微信企业号webhookUrl不能为空")
    private String wxworkWebhookUrl;
}
