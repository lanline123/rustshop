package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.bean.enums.setting.SettingsEnum.WECHAT_PAY_PUBLIC_KEY;

/**
 * 系统设置-微信支付设置
 *
 * @author kidd
 * @since 2025/6/7 10:59
 */
@Data
public class WechatPaySettingsVO {

    // *** 微信支付设置 ***

    @ConfigItemField(USE_WECHAT)
    @Schema(description = "是否启用微信支付；0-关闭，1-开启")
    private Integer useWechat;

    @ConfigItemField(WECHAT_MCHID_TYPE)
    @Schema(description = "微信商户号类型；1-普通商户模式，2-服务商模式")
    private Integer wechatMchidType;

    @ConfigItemField(WECHAT_PAY_MCHID)
    @Schema(description = "微信商户号")
    private String wechatPayMchid;

    @ConfigItemField(WECHAT_PAY_SUB_MCHID)
    @Schema(description = "微信子商户号")
    private Integer wechatPaySubMchid;

    @ConfigItemField(WECHAT_PAY_KEY)
    @Schema(description = "商户API密钥")
    private String wechatPayKey;

    @ConfigItemField(WECHAT_PAY_V2_KEY)
    @Schema(description = "商户APIV2密钥")
    private String wechatPayV2Key;

    @ConfigItemField(WECHAT_PAY_SERIAL_NO)
    @Schema(description = "商户证书序列号")
    private String wechatPaySerialNo;

    @ConfigItemField(WECHAT_PAY_CERTIFICATE)
    @Schema(description = "商户API证书")
    private Integer wechatPayCertificate;

    @ConfigItemField(WECHAT_PAY_PRIVATE_KEY)
    @Schema(description = "商户API证书密钥")
    private Integer wechatPayPrivateKey;

    @ConfigItemField(WECHAT_PAY_CHECK_TYPE)
    @Schema(description = "验证微信支付方式；1-平台证书，2-微信支付公钥")
    private Integer wechatPayCheckType;

    @ConfigItemField(WECHAT_PAY_PLATFORM_CERTIFICATE)
    @Schema(description = "平台证书")
    private Integer wechatPayPlatformCertificate;

    @ConfigItemField(WECHAT_PAY_PUBLIC_KEY_ID)
    @Schema(description = "微信支付公钥ID")
    private String wechatPayPublicKeyId;

    @ConfigItemField(WECHAT_PAY_PUBLIC_KEY)
    @Schema(description = "微信支付公钥文件")
    private Integer wechatPayPublicKey;

    // *** 微信小程序支付 ***

    @ConfigItemField(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID)
    @Schema(description = "微信小程序AppID")
    private String wechatMiniProgramAppId;

    @ConfigItemField(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET)
    @Schema(description = "微信小程序AppSecret")
    private String wechatMiniProgramSecret;

    // *** 微信 APP 支付 ***

    @ConfigItemField(SettingsEnum.WECHAT_PAY_APP_ID)
    @Schema(description = "应用APP ID")
    private String wechatPayAppId;

    @ConfigItemField(SettingsEnum.WECHAT_PAY_APP_SECRET)
    @Schema(description = "应用APP SECRET")
    private String wechatPayAppSecret;

    public void encryptData() {
        this.wechatPayMchid = StringUtils.maskMiddleHalf(this.wechatPayMchid);

        this.wechatPayKey = StringUtils.maskMiddleHalf(this.wechatPayKey);
        this.wechatPayV2Key = StringUtils.maskMiddleHalf(this.wechatPayV2Key);
        this.wechatPaySerialNo = StringUtils.maskMiddleHalf(this.wechatPaySerialNo);

        this.wechatMiniProgramAppId = StringUtils.maskMiddleHalf(this.wechatMiniProgramAppId);
        this.wechatMiniProgramSecret = StringUtils.maskMiddleHalf(this.wechatMiniProgramSecret);

        this.wechatPayAppId = StringUtils.maskMiddleHalf(this.wechatPayAppId);
        this.wechatPayAppSecret = StringUtils.maskMiddleHalf(this.wechatPayAppSecret);
    }

}
