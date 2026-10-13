package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.NotifySettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * 通知配置新增参数
 *
 * @author kidd
 * @since 2025/4/3 16:27
 */
@Data
public class NotifySaveParam implements ConfigSaveParam {

    // *** 短信服务商 ***

    @ConfigItemField(SettingsEnum.SMS_PROVIDER)
    @Schema(description = "短信服务商；1-阿里云，2-腾讯云，3-华为云，4-火山引擎")
    private Integer smsProvider;

    // *** 阿里云短信 ***

    @ConfigItemField(SettingsEnum.SMS_KEY_ID)
    @Schema(description = "短信帐户用户名")
    private String smsKeyId;

    @ConfigItemField(SettingsEnum.SMS_KEY_SECRET)
    @Schema(description = "短信帐户密码")
    private String smsKeySecret;

    @ConfigItemField(SettingsEnum.SMS_SIGN_NAME)
    @Schema(description = "短信签名")
    private String smsSignName;

    @ConfigItemField(SettingsEnum.SMS_SHOP_MOBILE)
    @Schema(description = "商家短信号码")
    private String smsShopMobile;

    // *** 腾讯云短信 ***

    @ConfigItemField(SettingsEnum.SMS_TENCENT_SECRET_ID)
    @Schema(description = "腾讯云SecretId")
    private String smsTencentSecretId;

    @ConfigItemField(SettingsEnum.SMS_TENCENT_SECRET_KEY)
    @Schema(description = "腾讯云SecretKey")
    private String smsTencentSecretKey;

    @ConfigItemField(SettingsEnum.SMS_TENCENT_SDK_APP_ID)
    @Schema(description = "腾讯云SDK AppID")
    private String smsTencentSdkAppId;

    @ConfigItemField(SettingsEnum.SMS_TENCENT_SIGN_NAME)
    @Schema(description = "腾讯云短信签名")
    private String smsTencentSignName;

    @ConfigItemField(SettingsEnum.SMS_TENCENT_SHOP_MOBILE)
    @Schema(description = "腾讯云商家短信号码")
    private String smsTencentShopMobile;

    // *** 华为云短信 ***

    @ConfigItemField(SettingsEnum.SMS_HUAWEI_APP_KEY)
    @Schema(description = "华为云APP Key")
    private String smsHuaweiAppKey;

    @ConfigItemField(SettingsEnum.SMS_HUAWEI_APP_SECRET)
    @Schema(description = "华为云APP Secret")
    private String smsHuaweiAppSecret;

    @ConfigItemField(SettingsEnum.SMS_HUAWEI_CHANNEL)
    @Schema(description = "华为云短信通道号")
    private String smsHuaweiChannel;

    @ConfigItemField(SettingsEnum.SMS_HUAWEI_SIGN_NAME)
    @Schema(description = "华为云短信签名")
    private String smsHuaweiSignName;

    @ConfigItemField(SettingsEnum.SMS_HUAWEI_ENDPOINT)
    @Schema(description = "华为云接入地址")
    private String smsHuaweiEndpoint;

    @ConfigItemField(SettingsEnum.SMS_HUAWEI_SHOP_MOBILE)
    @Schema(description = "华为云商家短信号码")
    private String smsHuaweiShopMobile;

    // *** 火山引擎短信 ***

    @ConfigItemField(SettingsEnum.SMS_VOLCENGINE_ACCESS_KEY)
    @Schema(description = "火山引擎AccessKey ID")
    private String smsVolcengineAccessKey;

    @ConfigItemField(SettingsEnum.SMS_VOLCENGINE_SECRET_KEY)
    @Schema(description = "火山引擎SecretAccessKey")
    private String smsVolcengineSecretKey;

    @ConfigItemField(SettingsEnum.SMS_VOLCENGINE_SMS_ACCOUNT)
    @Schema(description = "火山引擎短信应用ID")
    private String smsVolcengineSmsAccount;

    @ConfigItemField(SettingsEnum.SMS_VOLCENGINE_SIGN_NAME)
    @Schema(description = "火山引擎短信签名")
    private String smsVolcengineSignName;

    @ConfigItemField(SettingsEnum.SMS_VOLCENGINE_SHOP_MOBILE)
    @Schema(description = "火山引擎商家短信号码")
    private String smsVolcengineShopMobile;

    // *** 阿里云短信通道开关（仅跨境版；1-+86国内 2-其他海外，英文逗号分隔多选） ***

    @ConfigItemField(SettingsEnum.SMS_ALIYUN_CHANNEL)
    @Schema(description = "阿里云短信通道；1-+86国内，2-其他海外，多选英文逗号分隔")
    private String smsAliyunChannel;

    @ConfigItemField(SettingsEnum.SMS_SENDER_ID)
    @Schema(description = "阿里云国际短信发送方标识SenderID（SendMessageToGlobe 的 From）")
    private String smsSenderId;

    // *** 邮箱通知 ***

    @ConfigItemField(SettingsEnum.SERVICE_EMAIL)
    @Schema(description = "客服收件地址")
    private String serviceEmail;

    @ConfigItemField(SettingsEnum.SEND_CONFIRM_EMAIL)
    @Schema(description = "提交订单发送邮件; 0-否，1-是")
    private Integer sendConfirmEmail;

    @ConfigItemField(SettingsEnum.ORDER_PAY_EMAIL)
    @Schema(description = "订单支付发送邮件; 0-否，1-是")
    private Integer orderPayEmail;

    @ConfigItemField(SettingsEnum.SEND_SERVICE_EMAIL)
    @Schema(description = "下单给客服发邮件; 0-否，1-是")
    private Integer sendServiceEmail;

    @ConfigItemField(SettingsEnum.SEND_SHIP_EMAIL)
    @Schema(description = "发货时发送邮件; 0-否，1-是")
    private Integer sendShipEmail;

    public void noUpdate(NotifySettingsVO settings) {
        // 阿里云
        if (Objects.equals(this.smsKeyId, settings.getSmsKeyId())) {
            this.smsKeyId = null;
        }
        if (Objects.equals(this.smsKeySecret, settings.getSmsKeySecret())) {
            this.smsKeySecret = null;
        }
        // 腾讯云
        if (Objects.equals(this.smsTencentSecretId, settings.getSmsTencentSecretId())) {
            this.smsTencentSecretId = null;
        }
        if (Objects.equals(this.smsTencentSecretKey, settings.getSmsTencentSecretKey())) {
            this.smsTencentSecretKey = null;
        }
        // 华为云
        if (Objects.equals(this.smsHuaweiAppKey, settings.getSmsHuaweiAppKey())) {
            this.smsHuaweiAppKey = null;
        }
        if (Objects.equals(this.smsHuaweiAppSecret, settings.getSmsHuaweiAppSecret())) {
            this.smsHuaweiAppSecret = null;
        }
        // 火山引擎
        if (Objects.equals(this.smsVolcengineAccessKey, settings.getSmsVolcengineAccessKey())) {
            this.smsVolcengineAccessKey = null;
        }
        if (Objects.equals(this.smsVolcengineSecretKey, settings.getSmsVolcengineSecretKey())) {
            this.smsVolcengineSecretKey = null;
        }
    }

}
