package com.tigshop.service.pay;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 新增在线支付渠道分发注册表（局部工厂）。
 *
 * <p>
 * 仅登记「新渠道」：汇付（huifu）、Apple Pay（applepay）、拉卡拉（lakala）。
 * 微信/支付宝/paypal/stripe/antom/xendit 等既有渠道仍走各自的 if-else 分发，本类不介入，
 * 以保证既有生产渠道零改动、零风险。
 * </p>
 *
 * <p>
 * 通过 Spring 注入的 {@code Map<beanName, PayService>} 按 Bean 名建立 payType -&gt; 渠道映射；
 * 某渠道在特定构建未启用（Bean 缺失）时自动跳过，{@link #get(String)} 返回 {@code null}，
 * 调用点回落到原有分支语义，不会因缺 Bean 启动失败。
 * </p>
 *
 * @author Tigshop团队
 * @since 2026/07/16
 */
@Component
public class NewPayChannelRegistry {

    /** payType -> 渠道实现 */
    private final Map<String, PayService> byPayType = new HashMap<>();

    /**
     * @param payServices Spring 注入的所有 PayService 实现（key 为 Bean 名，如 "HuifuService"）
     */
    public NewPayChannelRegistry(Map<String, PayService> payServices) {
        // 汇付：huifu 为回调父级 key，huifuWechat/huifuAlipay 为下单/退款子码，均指向同一 Bean
        register(payServices, "HuifuService", "huifu", "huifuWechat", "huifuAlipay");
        register(payServices, "ApplePayService", "applepay");
        // 拉卡拉：lakala 为回调父级 key，lakalaWechat/lakalaAlipay 为下单/退款子码，均指向同一 Bean
        register(payServices, "LakalaService", "lakala", "lakalaWechat", "lakalaAlipay");
        // 易宝：yeepay 为回调父级 key，yeepayWechat/yeepayAlipay 为下单/退款子码，均指向同一 Bean
        register(payServices, "YeepayService", "yeepay", "yeepayWechat", "yeepayAlipay");
    }

    private void register(Map<String, PayService> beans, String beanName, String... payTypes) {
        PayService svc = beans.get(beanName);
        if (svc == null) {
            // 该渠道在当前构建未启用，跳过登记
            return;
        }
        for (String payType : payTypes) {
            byPayType.put(payType, svc);
        }
    }

    /**
     * 按 payType 取渠道实现；未登记（非本工厂管辖的渠道，或渠道未启用）返回 {@code null}。
     */
    public PayService get(String payType) {
        return payType == null ? null : byPayType.get(payType);
    }
}
