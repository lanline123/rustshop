package com.tigshop.bean.vo.setting.config;

import com.alibaba.fastjson.JSONArray;
import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-Apple Pay 苹果支付设置
 *
 * @author Tigshop团队
 */
@Data
public class ApplePaySettingsVO {

    @ConfigItemField(USE_APPLE_PAY)
    @Schema(description = "是否启用Apple Pay支付；0-关闭，1-开启")
    private Integer useApplePay;

    @ConfigItemField(APPLE_PAY_MERCHANT_ID)
    @Schema(description = "Apple商户标识（Merchant ID）")
    private String applePayMerchantId;

    @ConfigItemField(APPLE_PAY_DISPLAY_NAME)
    @Schema(description = "商户展示名")
    private String applePayDisplayName;

    @ConfigItemField(APPLE_PAY_COUNTRY_CODE)
    @Schema(description = "国家/地区（ISO 3166 两位码）")
    private String applePayCountryCode;

    @ConfigItemField(APPLE_PAY_CURRENCY)
    @Schema(description = "货币（ISO 4217 三位码）")
    private String applePayCurrency;

    @ConfigItemField(APPLE_PAY_SUPPORTED_NETWORKS)
    @Schema(description = "支持卡组织（逗号分隔）")
    private String applePaySupportedNetworks;

    @ConfigItemField(APPLE_PAY_MERCHANT_IDENTITY_CERT)
    @Schema(description = "商户身份证书已上传状态；0-未上传，1-已上传")
    private Integer applePayMerchantIdentityCert;

    @ConfigItemField(APPLE_PAY_MERCHANT_IDENTITY_KEY)
    @Schema(description = "商户身份证书私钥已上传状态；0-未上传，1-已上传")
    private Integer applePayMerchantIdentityKey;

    @ConfigItemField(APPLE_PAY_PROCESSING_PRIVATE_KEY)
    @Schema(description = "支付处理证书私钥已上传状态；0-未上传，1-已上传")
    private Integer applePayProcessingPrivateKey;

    @ConfigItemField(APPLE_PAY_ACQUIRER_PROVIDER)
    @Schema(description = "收单机构：ums/stripe/adyen/generic")
    private String applePayAcquirerProvider;

    @ConfigItemField(APPLE_PAY_UMS_SIGN_TYPE)
    @Schema(description = "银联商务签名算法：SHA256/SM3")
    private String applePayUmsSignType;

    @ConfigItemField(APPLE_PAY_ACQUIRER_GATEWAY)
    @Schema(description = "收单网关地址")
    private String applePayAcquirerGateway;

    @ConfigItemField(APPLE_PAY_ACQUIRER_MERCHANT_NO)
    @Schema(description = "收单商户号")
    private String applePayAcquirerMerchantNo;

    @ConfigItemField(APPLE_PAY_ACQUIRER_KEY)
    @Schema(description = "收单密钥")
    private String applePayAcquirerKey;

    @ConfigItemField(APPLE_PAY_COUNTRY_LIST)
    @Schema(description = "Apple Pay国家/地区列表原始JSON串")
    private String applePayCountryListStr;

    @ConfigItemField(APPLE_PAY_CURRENCY_LIST)
    @Schema(description = "Apple Pay货币列表原始JSON串")
    private String applePayCurrencyListStr;

    // *** Other ***

    @Schema(description = "Apple Pay国家/地区列表")
    private JSONArray applePayCountryList;

    @Schema(description = "Apple Pay货币列表")
    private JSONArray applePayCurrencyList;

    /**
     * 密钥类字段中段掩码（证书状态为 0/1 标记，不掩码；收单商户号/密钥做掩码）。
     */
    public void encryptData() {
        this.applePayAcquirerMerchantNo = StringUtils.maskMiddleHalf(this.applePayAcquirerMerchantNo);
        this.applePayAcquirerKey = StringUtils.maskMiddleHalf(this.applePayAcquirerKey);
    }
}
