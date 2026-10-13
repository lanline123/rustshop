package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取用户 openid
 *
 * @author kidd
 * @since 2025/4/18 18:58
 */
@Data
public class LoginUpdateUserOpenIdParam {

    @NotBlank(message = "code不能为空")
    @Schema(description = "code")
    private String code;
}
