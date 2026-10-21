// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.login;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.FromTag;
import com.tigshop.bean.model.user.User;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Optional;

import static com.tigshop.common.constant.login.LoginConstants.PASSWORD_NOT_LOGIN;
import static com.tigshop.common.constant.user.UserConstants.*;

/**
 * 注册请求参数
 *
 * @author Tigshop团队
 * @create 2025年03月03日 15:53
 */
@Data
@Schema(description = "注册请求参数")
public class RegisterParam {

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "手机验证码")
    private String mobileCode;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "注册类型 为空或者为phone为手机注册, email 为邮箱注册")
    private String registType = "phone";

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "邮箱验证码")
    private String emailCode;

    @Schema(description = "推荐人（分销员）id")
    private Integer salesmanId;


    public void mobileValidate() {
        mobileValidate(false);
    }

    /**
     * 手机号注册验证
     *
     * @param skipPhoneFormatCheck 是否跳过手机号格式验证（跨境版本使用）
     */
    public void mobileValidate(boolean skipPhoneFormatCheck) {
        Assert.isFalse(StrUtil.isBlank(this.mobile), () -> new GlobalException(MOBILE_NOT_EMPTY));
        Assert.isFalse(StrUtil.isBlank(this.password), () -> new GlobalException(PASSWORD_NOT_LOGIN));
        Assert.isFalse(StrUtil.isBlank(this.mobileCode), () -> new GlobalException(MOBILE_CODE_NOT_EMPTY));
        if (!skipPhoneFormatCheck) {
            Assert.isTrue(PhoneUtil.isPhone(mobile), () -> new GlobalException("手机号格式不正确"));
        }
    }

    public static FromTag getFromTag() {
        String clientType = HeaderUtils.getClientType();
        switch (clientType) {
            // PC端
            case "pc" -> {
                return FromTag.PC;
            }
            // 公众号
            case "wechat" -> {
                return FromTag.WECHAT;
            }
            // h5
            case "h5" -> {
                return FromTag.H5;
            }
            // 小程序
            case "miniProgram" -> {
                return FromTag.MINI_PROGRAM;
            }
            // 安卓
            case "android" -> {
                return FromTag.ANDROID;
            }
            // ios
            case "ios" -> {
                return FromTag.IOS;
            }
            //app
            case "app" -> {
                return FromTag.APP;
            }
            default -> {
                return null;
            }
        }
    }

    public void emailValidate() {
        Assert.isFalse(StrUtil.isBlank(this.email), () -> new GlobalException(EMAIL_NOT_EMPTY));
        Assert.isFalse(StrUtil.isBlank(this.email), () -> new GlobalException(PASSWORD_NOT_LOGIN));
        Assert.isFalse(StrUtil.isBlank(this.emailCode), () -> new GlobalException(MOBILE_CODE_NOT_EMPTY));
        Assert.isTrue(Validator.isEmail(email), () -> new GlobalException("邮箱地址不正确"));
    }

    public User createMobileUser(String username, String userNamePrefix) {
        return User.builder()
                .fromTag(Optional.ofNullable(getFromTag()).map(FromTag::getCode).orElse(0))
                .username(username)
                .nickname(StrUtil.isNotBlank(userNamePrefix) ? userNamePrefix + username : StrUtil.format("user_{}", username))
                .mobile(this.mobile)
                .regTime(StringUtils.getCurrentTime())
                .mobileValidated(Constants.YES)
                .build();
    }

    public User createEmailUser(String username, String userNamePrefix) {
        return User.builder()
                .fromTag(Optional.ofNullable(getFromTag()).map(FromTag::getCode).orElse(0))
                .username(username)
                .nickname(StrUtil.isNotBlank(userNamePrefix) ? userNamePrefix + username : StrUtil.format("user_{}", username))
                .email(this.email)
                .regTime(StringUtils.getCurrentTime())
                .emailValidated(Constants.YES)
                .build();
    }

}