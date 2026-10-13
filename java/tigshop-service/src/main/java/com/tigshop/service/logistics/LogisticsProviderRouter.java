package com.tigshop.service.logistics;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.enums.order.LogisticsProviderType;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.common.constant.setting.Kuaidi100CodeMapping;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.setting.ConfigService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按配置 {@code logisticsProvider} 路由到当前激活的物流服务商。
 * <p>1=快递鸟（默认），2=快递100。</p>
 */
@Component
public class LogisticsProviderRouter {

    private final ConfigService configService;
    private final Map<Integer, LogisticsProvider> providersByType;

    public LogisticsProviderRouter(ConfigService configService, List<LogisticsProvider> providers) {
        this.configService = configService;
        this.providersByType = providers.stream()
                .collect(Collectors.toMap(LogisticsProvider::providerType, Function.identity(), (a, b) -> a));
    }

    public LogisticsProvider getActive() {
        int type = resolveProviderType();
        LogisticsProvider provider = providersByType.get(type);
        if (provider == null) {
            throw new GlobalException("未找到物流服务商实现，provider=" + type);
        }
        return provider;
    }

    /**
     * 按订单快照解析唯一的历史查询服务商。
     * 未知或非法快照只尝试当前激活服务商，编码或凭证不兼容时返回 {@code null}。
     */
    public LogisticsProvider resolveForQuery(int providerSnapshot, LogisticsCompany company) {
        LogisticsProviderType snapshot = LogisticsProviderType.fromCode(providerSnapshot);
        int providerType = snapshot == LogisticsProviderType.UNKNOWN
                ? resolveProviderType()
                : snapshot.getCode();
        LogisticsProvider provider = providersByType.get(providerType);
        if (provider == null || !provider.isConfigured() || !supportsCompany(providerType, company)) {
            return null;
        }
        return provider;
    }

    private boolean supportsCompany(int providerType, LogisticsCompany company) {
        if (company == null) {
            return false;
        }
        if (providerType == LogisticsProviderType.KDNIAO.getCode()) {
            return StrUtil.isNotBlank(company.getLogisticsCode());
        }
        if (providerType == LogisticsProviderType.KUAIDI100.getCode()) {
            return StrUtil.isNotBlank(company.getKuaidi100Code())
                    || StrUtil.isNotBlank(Kuaidi100CodeMapping.resolve(company.getLogisticsCode()));
        }
        return false;
    }

    public int resolveProviderType() {
        String val = configService.getConfigVal(SettingsEnum.LOGISTICS_PROVIDER);
        if (StrUtil.isBlank(val)) {
            return LogisticsProviderType.KDNIAO.getCode();
        }
        try {
            int type = Integer.parseInt(val.trim());
            if (type == LogisticsProviderType.KUAIDI100.getCode()) {
                return LogisticsProviderType.KUAIDI100.getCode();
            }
            return LogisticsProviderType.KDNIAO.getCode();
        } catch (NumberFormatException e) {
            return LogisticsProviderType.KDNIAO.getCode();
        }
    }
}
