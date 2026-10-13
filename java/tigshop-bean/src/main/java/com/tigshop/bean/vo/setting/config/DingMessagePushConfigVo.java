package com.tigshop.bean.vo.setting.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DingMessagePushConfigVo {

    @Schema(description = "钉钉appsecret")
    private String dingAppSecret;

    @Schema(description = "钉钉机器人token")
    private String dingTalkRobotToken;
}
