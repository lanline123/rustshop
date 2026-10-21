package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class DingMessageConfigDTO {

    @Schema(description = "钉钉appsecret")
    @NotEmpty(message = "钉钉appsecret不能为空")
    private String dingAppSecret;

    @Schema(description = "钉钉机器人token")
    @NotEmpty(message = "钉钉机器人token不能为空")
    private String dingTalkRobotToken;

}
