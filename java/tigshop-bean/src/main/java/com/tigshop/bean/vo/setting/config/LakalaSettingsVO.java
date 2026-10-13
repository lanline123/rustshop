// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 系统设置-拉卡拉支付设置
 *
 * <p>
 * 一期定位：平台统一收款 + 退款，不涉及分账 / 多商户结算。
 * 覆盖 PC 收银台 / H5 / 微信公众号 JSAPI / 小程序 / APP / 被扫付款码。
 * 使用拉卡拉开放平台官方 Java SDK（1.0.8，随源码内置于 repository/）。
 * </p>
 *
 * @author Tigshop团队
 * @since 2026/07
 */
@Data
public class LakalaSettingsVO {

    @ConfigItemField(USE_LAKALA)
    @Schema(description = "是否启用拉卡拉支付；0-关闭，1-开启")
    private Integer useLakala;

    @ConfigItemField(LAKALA_APP_ID)
    @Schema(description = "拉卡拉开放平台 AppId")
    private String lakalaAppId;

    @ConfigItemField(LAKALA_MERCHANT_NO)
    @Schema(description = "拉卡拉商户号（merchantNo）")
    private String lakalaMerchantNo;

    @ConfigItemField(LAKALA_TERM_NO)
    @Schema(description = "拉卡拉终端号（termNo）")
    private String lakalaTermNo;

    @ConfigItemField(LAKALA_SERIAL_NO)
    @Schema(description = "拉卡拉商户证书序列号（serialNo）")
    private String lakalaSerialNo;

    @ConfigItemField(LAKALA_MERCHANT_PRIVATE_KEY)
    @Schema(description = "拉卡拉商户私钥（字符串，PKCS8）")
    private String lakalaMerchantPrivateKey;

    @ConfigItemField(LAKALA_PLATFORM_CERT)
    @Schema(description = "拉卡拉开放平台证书（字符串，.cer 内容）")
    private String lakalaPlatformCert;

    @ConfigItemField(LAKALA_SM4_KEY)
    @Schema(description = "拉卡拉报文对称加密 SM4 密钥（可选）")
    private String lakalaSm4Key;

    @ConfigItemField(LAKALA_GATEWAY_URL)
    @Schema(description = "拉卡拉网关地址")
    private String lakalaGatewayUrl;

    @ConfigItemField(LAKALA_SANDBOX)
    @Schema(description = "拉卡拉是否启用沙箱/测试环境；0-生产，1-沙箱")
    private Integer lakalaSandbox;

    @ConfigItemField(LAKALA_WX_SUB_APPID)
    @Schema(description = "拉卡拉微信子 AppId（公众号 JSAPI）")
    private String lakalaWxSubAppid;

    @ConfigItemField(LAKALA_WX_MINI_APPID)
    @Schema(description = "拉卡拉微信小程序 AppId")
    private String lakalaWxMiniAppid;

    /**
     * 对敏感字段做掩码（中间半段打码），避免下发到前端时泄露私钥 / 证书 / 密钥原文。
     */
    public void encryptData() {
        this.lakalaMerchantPrivateKey = StringUtils.maskMiddleHalf(this.lakalaMerchantPrivateKey);
        this.lakalaPlatformCert = StringUtils.maskMiddleHalf(this.lakalaPlatformCert);
        this.lakalaSm4Key = StringUtils.maskMiddleHalf(this.lakalaSm4Key);
    }
}
