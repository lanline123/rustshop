package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 获取用户手机号
 *
 * @author kidd
 * @since 2025/4/18 18:57
 */
@Data
public class LoginGetMobileParam {

    @NotBlank(message = "code不能为空")
    @Schema(description = "code")
    private String code;
}
