package com.tigshop.bean.vo.setting.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WXWorkMessagePushConfigVo {

    @Schema(description = "微信企业号webhookUrl")
    private String wxworkWebhookUrl;
}
