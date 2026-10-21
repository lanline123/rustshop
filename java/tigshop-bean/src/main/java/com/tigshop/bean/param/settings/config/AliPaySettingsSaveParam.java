package com.tigshop.bean.param.settings.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.AliPaySettingsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.bean.enums.setting.SettingsEnum.ALIPAY_RSA_PUBLIC_KEY;

/**
 * 系统设置-支付宝支付设置新增参数
 *
 * @author kidd
 * @since 2025/6/7 11:13
 */
@Data
public class AliPaySettingsSaveParam implements ConfigSaveParam {

    @ConfigItemField(USE_ALIPAY)
    @Schema(description = "是否启用支付宝支付；0-关闭，1-开启")
    private Integer useAlipay;

    @ConfigItemField(ALIPAY_APPID)
    @Schema(description = "支付宝APPID")
    private String alipayAppid;

    @ConfigItemField(ALIPAY_MOBILE_APPID)
    @Schema(description = "支付宝移动端APPID")
    private String alipayMobileAppid;

    @ConfigItemField(ALIPAY_RSA_PRIVATE_KEY)
    @Schema(description = "应用私钥")
    private String alipayRsaPrivateKey;

    @ConfigItemField(ALIPAY_RSA_PUBLIC_KEY)
    @Schema(description = "支付宝公钥")
    private String alipayRsaPublicKey;

    @ConfigItemField(ALIPAY_PROFIT_SHARING_ENABLED)
    @Schema(description = "是否启用支付宝分账；0-关闭，1-开启")
    private Integer alipayProfitSharingEnabled;

    @ConfigItemField(ALIPAY_DELAY_ACCT_ENABLED)
    @Schema(description = "支付宝是否启用延时冻结；0-否，1-是")
    private Integer alipayDelayAcctEnabled;

    @ConfigItemField(ALIPAY_PROFIT_SHARING_NOTIFY_URL)
    @Schema(description = "支付宝分账回调地址")
    private String alipayProfitSharingNotifyUrl;

    @ConfigItemField(ALIPAY_PROFIT_SHARING_MAX_RATIO)
    @Schema(description = "支付宝默认最大分账比例(%)")
    private String alipayProfitSharingMaxRatio;

    @ConfigItemField(ALIPAY_PROFIT_SHARING_RULE_ORIGIN)
    @Schema(description = "支付宝分账规则来源；01-接口发起，02-控台配置")
    private String alipayProfitSharingRuleOrigin;

    @ConfigItemField(ALIPAY_PROFIT_SHARING_PLATFORM_ID)
    @Schema(description = "支付宝平台分账收款账户(PID/UID)")
    private String alipayProfitSharingPlatformId;

    @ConfigItemField(ALIPAY_APP_AUTH_TOKEN)
    @Schema(description = "支付宝服务商代调授权令牌(app_auth_token)")
    private String alipayAppAuthToken;

    public void noUpdate(AliPaySettingsVO settings) {
        if (Objects.equals(this.alipayAppid, settings.getAlipayAppid())) {
            this.alipayAppid = null;
        }
        if (Objects.equals(this.alipayMobileAppid, settings.getAlipayMobileAppid())) {
            this.alipayMobileAppid = null;
        }
        if (Objects.equals(this.alipayRsaPrivateKey, settings.getAlipayRsaPrivateKey())) {
            this.alipayRsaPrivateKey = null;
        }
        if (Objects.equals(this.alipayRsaPublicKey, settings.getAlipayRsaPublicKey())) {
            this.alipayRsaPublicKey = null;
        }
    }
}
