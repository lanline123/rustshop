package com.tigshop.bean.enums.pay;

import lombok.Getter;

/**
 * 微信支付下单形态
 * <p>决定调用哪一族微信 V3 接口：合单支付、普通服务商支付还是直连商户支付。
 * 三者在回调地址、退款参数和查单接口上均不通用，必须在下单时一次性判定。
 *
 * @author Tigshop团队
 */
@Getter
public enum WechatPayMode {

    /**
     * 合单支付 {@code /v3/combine-transactions/*}
     * <p>跨店订单且全部子单店铺均已完成二级商户进件时使用，一笔支付覆盖多个二级商户
     */
    COMBINE("combine", "合单支付"),

    /**
     * 普通服务商支付 {@code /v3/pay/partner/transactions/*}
     * <p>单店订单且该店铺已完成二级商户进件时使用
     */
    PARTNER("partner", "服务商支付"),

    /**
     * 直连商户支付 {@code /v3/pay/transactions/*}
     * <p>未开启线上分账、任一相关店铺未进件、无店铺归属（平台自营 / 余额充值）时使用，该单不参与分账
     */
    DIRECT("direct", "直连支付");

    private final String code;
    private final String description;

    WechatPayMode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static WechatPayMode fromCode(String code) {
        for (WechatPayMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return null;
    }
}
