package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统设置-登录设置
 *
 * @author kidd
 * @since 2025/6/6 10:00
 */
@Data
public class LoginSettingsVO {

    // *** 微信登录 ***

    @ConfigItemField(SettingsEnum.OPEN_WECHAT_OAUTH)
    @Schema(description = "开启公众号授权登录；0-关闭，1-开启")
    private Integer openWechatOauth;

    @ConfigItemField(SettingsEnum.WECHAT_APP_ID)
    @Schema(description = "微信APPID")
    private String wechatAppId;

    @ConfigItemField(SettingsEnum.WECHAT_APP_SECRET)
    @Schema(description = "微信AppSecret")
    private String wechatAppSecret;

    @ConfigItemField(SettingsEnum.WECHAT_SERVER_TOKEN)
    @Schema(description = "微信服务器Token")
    private String wechatServerToken;

    @ConfigItemField(SettingsEnum.WECHAT_SERVER_SECRET)
    @Schema(description = "微信服务器消息加密密钥")
    private String wechatServerSecret;

    @ConfigItemField(SettingsEnum.OPEN_WECHAT_REGISTER)
    @Schema(description = "是否开启小程序快捷登录；0-否，1-是")
    private Integer openWechatRegister;

    @ConfigItemField(SettingsEnum.WECHAT_SHARE_NEED_LOGIN)
    @Schema(description = "微信小程序分享是否需要登录；0-否，1-是")
    private Integer wechatShareNeedLogin;

    @ConfigItemField(SettingsEnum.OPEN_WECHAT_PC_LOGIN)
    @Schema(description = "是否开启微信注册；0-否，1-是")
    private Integer openWechatPcLogin;

    @ConfigItemField(SettingsEnum.WECHAT_REGISTER_BIND_PHONE)
    @Schema(description = "是否需要绑定手机号；0-否，1-是")
    private Integer wechatRegisterBindPhone;

    // *** Facebook 登录 ***

    @ConfigItemField(SettingsEnum.FACEBOOK_LOGIN_ON)
    @Schema(description = "是否开启Facebook登陆；0-否，1-是")
    private Integer facebookLoginOn;

    @ConfigItemField(SettingsEnum.FACEBOOK_CLIENT_ID)
    @Schema(description = "应用APP ID")
    private String facebookClientId;

    @ConfigItemField(SettingsEnum.FACEBOOK_CLIENT_SECRET)
    @Schema(description = "应用APP SECRET")
    private String facebookClientSecret;

    // *** Google 登录 ***

    @ConfigItemField(SettingsEnum.GOOGLE_LOGIN_ON)
    @Schema(description = "是否开启Google登陆；0-否，1-是")
    private Integer googleLoginOn;

    @ConfigItemField(SettingsEnum.GOOGLE_CLIENT_ID)
    @Schema(description = "应用APP ID")
    private String googleClientId;

    @ConfigItemField(SettingsEnum.GOOGLE_CLIENT_SECRET)
    @Schema(description = "应用APP SECRET")
    private String googleClientSecret;

    // *** X / Twitter 登录 ***

    @ConfigItemField(SettingsEnum.TWITTER_LOGIN_ON)
    @Schema(description = "是否开启X/Twitter登陆；0-否，1-是")
    private Integer twitterLoginOn;

    @ConfigItemField(SettingsEnum.TWITTER_CLIENT_ID)
    @Schema(description = "应用Client ID")
    private String twitterClientId;

    @ConfigItemField(SettingsEnum.TWITTER_CLIENT_SECRET)
    @Schema(description = "应用Client Secret")
    private String twitterClientSecret;

    // *** 基础设置 ***

    @ConfigItemField(SettingsEnum.IS_OPEN_MOBILE_AREA_CODE)
    @Schema(description = "是否开启手机区号选择；0-关闭，1-开启")
    private Integer isOpenMobileAreaCode;

    @ConfigItemField(SettingsEnum.USERNAME_PREFIX)
    @Schema(description = "自动注册用户名前缀")
    private String usernamePrefix;

    @ConfigItemField(SettingsEnum.OPEN_EMAIL_REGISTER)
    @Schema(description = "开启邮箱注册；0-否，1-是")
    private Integer openEmailRegister;

    public void encryptData() {
        this.wechatAppId = StringUtils.maskMiddleHalf(this.wechatAppId);
        this.wechatAppSecret = StringUtils.maskMiddleHalf(this.wechatAppSecret);
        this.wechatServerToken = StringUtils.maskMiddleHalf(this.wechatServerToken);
        this.wechatServerSecret = StringUtils.maskMiddleHalf(this.wechatServerSecret);

        this.googleClientId = StringUtils.maskMiddleHalf(this.googleClientId);
        this.googleClientSecret = StringUtils.maskMiddleHalf(this.googleClientSecret);

        this.facebookClientId = StringUtils.maskMiddleHalf(this.facebookClientId);
        this.facebookClientSecret = StringUtils.maskMiddleHalf(this.facebookClientSecret);

        this.twitterClientId = StringUtils.maskMiddleHalf(this.twitterClientId);
        this.twitterClientSecret = StringUtils.maskMiddleHalf(this.twitterClientSecret);
    }

}
