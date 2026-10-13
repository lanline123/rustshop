// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 支付设置
 *
 * @author Tigshop团队
 * @create 2024年12月30日 11:23
 */
@Data
@Schema(description = "支付设置")
public class PaymentConfigDTO {

    @Schema(description = "支付代码")
    private String code;

    @Schema(description = "是否使用微信支付")
    private int useWechat;

    @Schema(description = "是否使用支付宝支付")
    private int useAlipay;

    @Schema(description = "是否使用余额支付")
    private int useSurplus;

    @Schema(description = "是否使用线下支付")
    private int useOffline;

    @Schema(description = "是否使用Yabanpay支付")
    private int useYabanpay;

    @Schema(description = "是否使用Yabanpay微信支付")
    private int useYabanpayWechat;

    @Schema(description = "是否使用Yabanpay支付宝支付")
    private int useYabanpayAlipay;

    @Schema(description = "微信支付密钥")
    private String wechatPayKey;

    @Schema(description = "微信商户ID类型")
    private int wechatMchidType;

    @Schema(description = "Yabanpay货币类型")
    private String yabanpayCurrency;

    @Schema(description = "是否使用Paypal支付")
    private int usePaypal;

    @Schema(description = "Paypal客户端ID")
    private String paypalClientId;

    @Schema(description = "Paypal密钥")
    private String paypalSecret;

    @Schema(description = "Paypal货币类型")
    private String paypalCurrency;

    @Schema(description = "是否使用云支付")
    private int useYunpay;

    @Schema(description = "微信支付检查类型")
    private int wechatPayCheckType;

    @Schema(description = "微信支付公钥")
    private int wechatPayPublicKey;

    @Schema(description = "微信支付公钥ID")
    private String wechatPayPublicKeyId;

    @Schema(description = "线下银行支付信息")
    private String offlinePayBank;

    @Schema(description = "线下公司支付信息")
    private String offlinePayCompany;

    @Schema(description = "是否使用货到付款")
    private int useCod;

    @Schema(description = "是否使用积分")
    private int usePoints;

    @Schema(description = "是否使用优惠券")
    private int useCoupon;

    @Schema(description = "微信商户ID")
    private String wechatPayMchid;

    @Schema(description = "微信子商户ID")
    private int wechatPaySubMchid;

    @Schema(description = "微信支付序列号")
    private String wechatPaySerialNo;

    @Schema(description = "微信支付私钥")
    private int wechatPayPrivateKey;

    @Schema(description = "微信支付证书")
    private int wechatPayCertificate;

    @Schema(description = "微信支付平台证书")
    private int wechatPayPlatformCertificate;

    @Schema(description = "支付宝应用ID")
    private String alipayAppid;

    @Schema(description = "支付宝RSA私钥")
    private String alipayRsaPrivateKey;

    @Schema(description = "支付宝RSA公钥")
    private String alipayRsaPublicKey;

    @Schema(description = "支付宝RSA签名类型")
    private int alipayRsaSignType;

    @Schema(description = "支付宝RSA签名类型值")
    private String alipayRsaSignTypeValue;

    @Schema(description = "支付宝RSA签名类型值列表")
    private String alipayRsaSignTypeValueList;

    @Schema(description = "云支付UID")
    private String yunpayUid;

    @Schema(description = "云支付密钥")
    private String yunpaySecretKey;

    @Schema(description = "Yabanpay UID")
    private String yabandpayUid;

    @Schema(description = "Yabanpay密钥")
    private String yabandpaySecretKey;

    @Schema(description = "Yabanpay货币类型列表")
    private List<YabanCurrency> yabanpayCurrencyList;

    @Schema(description = "Paypal货币类型列表")
    private List<YabanCurrency> paypalCurrencyList;

    @Schema(description = "是否使用Stripe支付")
    private int useStripe;

    @Schema(description = "Stripe公钥")
    private String stripePublishableKey;

    @Schema(description = "Stripe货币类型")
    private String stripeCurrency;

    @Schema(description = "Stripe货币类型列表")
    private List<YabanCurrency> stripeCurrencyList;

    @Data
    @Schema(description = "货币类型")
    public static class YabanCurrency {
        @Schema(description = "货币名称")
        private String name;

        @Schema(description = "货币代码")
        private String value;
    }
}
