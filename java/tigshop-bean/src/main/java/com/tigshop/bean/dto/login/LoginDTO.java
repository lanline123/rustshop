package com.tigshop.bean.dto.login;

//**---------------------------------------------------------------------+
//** 登录实体类
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.login.LoginConstants.*;

/**
 * @author Jayce
 * @create 2024/9/26 13:26
 */
@Schema(description = "登录实体类")
@Data
public class LoginDTO {
    @Schema(description = "登录类型")
    @NotNull(message = LOGIN_TYPE_NOT_NULL)
    private String loginType;

    @Schema(description = "验证码唯一标识")
    private String captchaUid;

    @Schema(description = "用户名")
    @NotNull(message = ADMIN_USER_NOT_NAME)
    private String username;

    @Schema(description = "密码")
    @NotNull(message = PASSWORD_NOT_LOGIN)
    private String password;

    @Schema(description = "是否记住我")
    Boolean remember;

    @Schema(description = "验证码")
    private String verifyToken;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "手机验证码")
    private String mobileCode;
}
