package com.tigshop.service.finance.profitsharing;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.model.finance.Paylog;

import java.util.Locale;

/**
 * 支付渠道 → 线上分账渠道 纯映射工具（CR-20260731-150400）。
 *
 * <p>收单侧与结算侧共用同一口径，避免两处渠道判定漂移。仅做字符串映射，
 * 无任何汇付/支付宝 Bean 依赖，属常驻基础代码，不随 VORT 剥离。</p>
 *
 * <p>映射口径（按 {@code paylog.payCode} 前缀）：
 * <ul>
 *   <li>{@code huifu*}（如 huifuWechat / huifuAlipay）→ {@link #CHANNEL_HUIFU}</li>
 *   <li>{@code lakala*}（拉卡拉一期平台统一收款，无二级户线上分账）→ {@code null}</li>
 *   <li>{@code alipay*}（支付宝直付通）→ {@link #CHANNEL_ALIPAY}</li>
 *   <li>{@code wechat*}（微信服务商）→ {@link #CHANNEL_WECHAT}</li>
 *   <li>其余 / 空（余额、未知）→ {@code null}</li>
 * </ul>
 * 返回 {@code null} 表示该支付不属于任何「资金进店铺二级户」的线上分账渠道。</p>
 */
public final class ProfitSharingChannelResolver {

    public static final String CHANNEL_WECHAT = "WECHAT";
    public static final String CHANNEL_HUIFU = "HUIFU";
    public static final String CHANNEL_ALIPAY = "ALIPAY";

    private ProfitSharingChannelResolver() {
    }

    /**
     * 解析 paylog 对应的线上分账渠道；非二级户分账渠道返回 {@code null}。
     */
    public static String resolvePayChannel(Paylog paylog) {
        return paylog == null ? null : resolvePayChannel(paylog.getPayCode());
    }

    /**
     * 解析 payCode 对应的线上分账渠道；非二级户分账渠道返回 {@code null}。
     */
    public static String resolvePayChannel(String payCode) {
        if (StrUtil.isBlank(payCode)) {
            return null;
        }
        String code = payCode.trim().toLowerCase(Locale.ROOT);
        if (code.startsWith("huifu")) {
            return CHANNEL_HUIFU;
        }
        if (code.startsWith("lakala")) {
            return null;
        }
        if (code.startsWith("alipay")) {
            return CHANNEL_ALIPAY;
        }
        if (code.startsWith("wechat")) {
            return CHANNEL_WECHAT;
        }
        return null;
    }
}
