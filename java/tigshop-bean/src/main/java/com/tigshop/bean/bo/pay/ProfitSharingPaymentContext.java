package com.tigshop.bean.bo.pay;

import java.util.Set;

/** 单次支付请求的分账渠道快照上下文。 */
public final class ProfitSharingPaymentContext {

    private static final Set<String> CHANNELS = Set.of("WECHAT", "HUIFU", "ALIPAY");
    private static final ProfitSharingPaymentContext NONE = new ProfitSharingPaymentContext(null);

    private final String channel;

    private ProfitSharingPaymentContext(String channel) {
        this.channel = channel;
    }

    public static ProfitSharingPaymentContext none() {
        return NONE;
    }

    public static ProfitSharingPaymentContext of(String channel) {
        if (channel == null || !CHANNELS.contains(channel)) {
            throw new IllegalArgumentException("Unsupported profit sharing payment channel");
        }
        return new ProfitSharingPaymentContext(channel);
    }

    public String getChannel() {
        return channel;
    }
}
