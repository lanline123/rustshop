package com.tigshop.bean.feign.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小程序登录
 *
 * @author kidd
 * @since 2025/4/18 19:04
 */
@Data
public class Code2SessionResult {

    @Schema(description = "会话密钥")
    @JsonProperty("session_key")
    private String sessionKey;

    @Schema(description = "用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台帐号下会返回")
    private String unionid;

    @Schema(description = "错误信息，请求失败时返回")
    private String errmsg;

    @Schema(description = "用户唯一标识")
    private String openid;

    @Schema(description = "错误码，请求失败时返回")
    private Integer errcode;
}
