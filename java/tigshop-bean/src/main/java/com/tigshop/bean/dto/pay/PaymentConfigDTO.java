// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "支付配置")
public class PaymentConfigDTO {

    @Schema(description = "是否使用微信支付")
    private Integer useWechat;

    @Schema(description = "是否使用支付宝")
    private int useAlipay;

    @Schema(description = "是否使用余额支付")
    private int useSurplus;

    @Schema(description = "是否使用线下支付")
    private int useOffline;

    @Schema(description = "是否使用雅邦支付")
    private int useYabanpay;

    @Schema(description = "是否使用雅邦微信支付")
    private int useYabanpayWechat;

    @Schema(description = "是否使用雅邦支付宝支付")
    private int useYabanpayAlipay;

    @Schema(description = "微信支付密钥")
    private String wechatPayKey;

    @Schema(description = "微信商户ID类型")
    private int wechatMchidType;

    @Schema(description = "雅邦支付货币")
    private String yabanpayCurrency;

    @Schema(description = "是否使用PayPal")
    private int usePaypal;

    @Schema(description = "PayPal客户端ID")
    private String paypalClientId;

    @Schema(description = "PayPal密钥")
    private String paypalSecret;

    @Schema(description = "PayPal货币")
    private String paypalCurrency;

    @Schema(description = "是否使用云支付")
    private int useYunpay;

    @Schema(description = "微信支付检查类型")
    private int wechatPayCheckType;

    @Schema(description = "微信支付公钥")
    private int wechatPayPublicKey;

    @Schema(description = "微信支付公钥ID")
    private String wechatPayPublicKeyId;

    @Schema(description = "线下支付银行信息")
    private String offlinePayBank;

    @Schema(description = "线下支付公司信息")
    private String offlinePayCompany;

    @Schema(description = "是否使用货到付款")
    private int useCod;

    @Schema(description = "是否使用积分支付")
    private int usePoints;

    @Schema(description = "是否使用优惠券支付")
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

    @Schema(description = "雅邦支付UID")
    private String yabandpayUid;

    @Schema(description = "雅邦支付密钥")
    private String yabandpaySecretKey;

    @Schema(description = "雅邦支付货币列表")
    private List<CurrencyItem> yabanpayCurrencyList;

    @Schema(description = "PayPal货币列表")
    private List<CurrencyItem> paypalCurrencyList;

    @Schema(description = "是否使用Stripe")
    private int useStripe;

    @Schema(description = "Stripe公钥")
    private String stripePublishableKey;

    @Schema(description = "Stripe货币")
    private String stripeCurrency;

    @Schema(description = "Stripe货币列表")
    private List<CurrencyItem> stripeCurrencyList;

    @Data
    @Schema(description = "货币项")
    public static class CurrencyItem {

        @Schema(description = "货币名称")
        private String name;

        @Schema(description = "货币代码")
        private String value;
    }
}
