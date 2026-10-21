package com.tigshop.service.pay.applepay;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.service.setting.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 收单机构路由器：按配置 {@code applePayAcquirerProvider} 选中对应 {@link AcquirerProvider} 并委派。
 *
 * <p>上层（ApplePayServiceImpl）注入本类作为 {@link AcquirerClient} 门面，新增/切换收单机构只需增删
 * {@link AcquirerProvider} 实现 + 改配置，无需改上层。默认 generic。</p>
 *
 * @author Tigshop团队
 */
@Slf4j
@Primary
@Component
public class AcquirerClientRouter implements AcquirerClient {

    private static final String DEFAULT_PROVIDER = "generic";

    private final ConfigService configService;
    private final Map<String, AcquirerProvider> providerMap;

    public AcquirerClientRouter(ConfigService configService, List<AcquirerProvider> providers) {
        this.configService = configService;
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(AcquirerProvider::providerCode, Function.identity()));
    }

    private AcquirerProvider resolve() {
        String code = configService.getConfigVal(SettingsEnum.APPLE_PAY_ACQUIRER_PROVIDER);
        if (StrUtil.isBlank(code)) {
            code = DEFAULT_PROVIDER;
        }
        AcquirerProvider provider = providerMap.get(code);
        if (provider == null) {
            log.warn("Apple Pay 未知收单机构配置 {}，回退 {}", code, DEFAULT_PROVIDER);
            provider = providerMap.get(DEFAULT_PROVIDER);
        }
        return provider;
    }

    @Override
    public boolean requiresDecryption() {
        return resolve().requiresDecryption();
    }

    @Override
    public boolean isAsyncSettlement() {
        return resolve().isAsyncSettlement();
    }

    @Override
    public String submit(ApplePayAcquirerContext ctx) {
        return resolve().submit(ctx);
    }

    @Override
    public boolean refund(String transactionId, String refundSn, BigDecimal amount, ApplePayAcquirerConfig config) {
        return resolve().refund(transactionId, refundSn, amount, config);
    }

    @Override
    public String query(String transactionId, ApplePayAcquirerConfig config) {
        return resolve().query(transactionId, config);
    }

    @Override
    public boolean supportsPaymentMethodConfirm() {
        return resolve().supportsPaymentMethodConfirm();
    }

    @Override
    public String submitByPaymentMethod(String paySn, BigDecimal amount, String paymentMethodId,
                                        ApplePayAcquirerConfig config) {
        return resolve().submitByPaymentMethod(paySn, amount, paymentMethodId, config);
    }

    @Override
    public AcquirerNotifyResult parseNotify(java.util.Map<String, Object> params, ApplePayAcquirerConfig config) {
        return resolve().parseNotify(params, config);
    }
}
