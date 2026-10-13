// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order.pay;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付创建返回参数
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "支付创建返回参数")
public class PayCreateVO {

    @Schema(description = "订单id")
    private Integer orderId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "支付参数")
    private PayInfo payInfo;

    @Schema(description = "code")
    private String code;


    @Data
    @Schema(description = "支付参数")
    public static class PayInfo {
        // paypal 使用
        @Schema(description = "url")
        private String url;

        // pc 微信扫码使用
        @Schema(description = "url")
        private String codeUrl;

        @Schema(description = "提供支付方:1.wxpay;2.alipay")
        private String provider;

        @Schema(description = "APP支付返回参数")
        private String orderInfo;

        // alipay 使用
        @Schema(description = "h5参数")
        private String html;

        @Schema(description = "用于微信支付(付款码支付)可能直接返回支付结果状态,true表示支付成功,为false前端需要轮训特殊处理")
        private Boolean codePaySuccess;

        @Schema(description = "用于微信支付(付款码支付),需要输入密码,显示正在支付中,需要前端轮训查询订单状态接口")
        private Boolean userPaying;

        private String appId;
        private String timeStamp;
        private String nonceStr;
        @JsonProperty("package")
        private String packageValue;
        private String signType;
        private String paySign;
        private String partnerId;
        private String prepayId;
        private String sign;

        // Apple Pay 使用：前端据此构造 ApplePaySession PaymentRequest
        @Schema(description = "Apple Pay 支付面板参数")
        private ApplePayParams applePay;

        public PayInfo() {
        }

        public PayInfo(String url) {
            this.url = url;
        }

        public PayInfo(String codeUrl, String html) {
            this.codeUrl = codeUrl;
            this.html = html;
        }

        public PayInfo(String provider, String codeUrl, String orderInfo) {
            this.provider = provider;
            this.codeUrl = codeUrl;
            this.orderInfo = orderInfo;
        }
    }

    @Data
    @Schema(description = "Apple Pay 支付面板参数")
    public static class ApplePayParams {

        @Schema(description = "支付流水号")
        private String paySn;

        @Schema(description = "国家/地区码（ISO 3166）")
        private String countryCode;

        @Schema(description = "货币码（ISO 4217）")
        private String currencyCode;

        @Schema(description = "商户展示名")
        private String merchantDisplayName;

        @Schema(description = "支持卡组织")
        private List<String> supportedNetworks;

        @Schema(description = "商户能力，如 supports3DS/supportsEMV")
        private List<String> merchantCapabilities;

        @Schema(description = "总额信息")
        private Total total;

        @Data
        @Schema(description = "Apple Pay 总额")
        public static class Total {
            @Schema(description = "展示标签（商户名）")
            private String label;
            @Schema(description = "金额字符串，如 128.00")
            private String amount;
        }
    }
}