const WECHAT_CHANNEL_EXTRA = "需要开通微信收付通，订单资金由微信直接分账到子商户账户";

export function getOnlineChannelExtraKey(channel: string | undefined, isO2o: boolean): string {
    const shopLabel = isO2o ? "门店" : "店铺";
    const map: Record<string, string> = {
        wechat: WECHAT_CHANNEL_EXTRA,
        huifu: `需要${shopLabel}先完成汇付进件，订单资金才能由汇付线上分账到商户账户`,
        alipay: `需要${shopLabel}先完成支付宝进件，订单资金才能由支付宝线上分账到商户账户`
    };

    return map[channel || "wechat"] || WECHAT_CHANNEL_EXTRA;
}
