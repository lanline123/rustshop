package com.tigshop.bean.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前台提交的修改密码信息
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "前台提交的修改密码信息")
public class CheckModifyPasswordDTO {
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "验证码")
    private String code;

    @Schema(description = "新密码")
    private String password;
}
