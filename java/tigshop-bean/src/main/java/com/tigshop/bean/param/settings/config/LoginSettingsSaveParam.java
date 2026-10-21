package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.LoginSettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * 系统设置-登录设置新增参数
 *
 * @author kidd
 * @since 2025/6/6 13:22
 */
@Data
public class LoginSettingsSaveParam implements ConfigSaveParam {

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
    @Schema(description = "PC微信扫码登录；0-否，1-是")
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

    public void noUpdate(LoginSettingsVO settings) {
        // 判断是否无需修改
        if (Objects.equals(this.wechatAppId, settings.getWechatAppId())) {
            this.wechatAppId = null;
        }
        if (Objects.equals(this.wechatAppSecret, settings.getWechatAppSecret())) {
            this.wechatAppSecret = null;
        }
        if (Objects.equals(this.wechatServerToken, settings.getWechatServerToken())) {
            this.wechatServerToken = null;
        }
        if (Objects.equals(this.wechatServerSecret, settings.getWechatServerSecret())) {
            this.wechatServerSecret = null;
        }

        if (Objects.equals(this.googleClientId, settings.getGoogleClientId())) {
            this.googleClientId = null;
        }
        if (Objects.equals(this.googleClientSecret, settings.getGoogleClientSecret())) {
            this.googleClientSecret = null;
        }

        if (Objects.equals(this.facebookClientId, settings.getFacebookClientId())) {
            this.facebookClientId = null;
        }
        if (Objects.equals(this.facebookClientSecret, settings.getFacebookClientSecret())) {
            this.facebookClientSecret = null;
        }

        if (Objects.equals(this.twitterClientId, settings.getTwitterClientId())) {
            this.twitterClientId = null;
        }
        if (Objects.equals(this.twitterClientSecret, settings.getTwitterClientSecret())) {
            this.twitterClientSecret = null;
        }
    }
}
