package com.tigshop.bean.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前台提交的修改密码信息
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "验证邮箱")
public class CheckModifyEmailDTO {
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "验证码")
    private String code;
}
