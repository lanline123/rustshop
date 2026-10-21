package com.tigshop.bean.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 通过微信code获取微信用户信息
 *
 * @author kidd
 * @since 2025/4/29 17:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxLoginInfoByCodeVO {

    /**
     * 登录结果类型：
     * 1 - 已注册用户，直接登录成功，返回token
     * 2 - 未注册用户且需要绑定手机号，返回openData供前端绑定使用
     */
    @Schema(description = "type: 1-直接登录成功 2-需要绑定手机号")
    private Integer type;

    @Schema(description = "登录token，type=1时返回")
    private String token;

    @Schema(description = "微信授权数据，type=2时返回，用于绑定手机号")
    private Map<String, Object> openData;
}
