package com.tigshop.service.sms;

import com.tigshop.bean.enums.setting.SmsProviderEnum;
import com.tigshop.common.exception.GlobalException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SmsProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(SmsProviderFactory.class);

    @Resource
    private List<SmsProvider> providers;

    private final Map<SmsProviderEnum, SmsProvider> providerMap = new EnumMap<>(SmsProviderEnum.class);

    @PostConstruct
    public void init() {
        for (SmsProvider provider : providers) {
            providerMap.put(provider.getProviderType(), provider);
            log.info("注册短信服务商：{}", provider.getProviderType().getName());
        }
    }

    public SmsProvider getProvider(Integer providerCode) {
        SmsProviderEnum providerEnum = SmsProviderEnum.fromCode(providerCode);
        return getProvider(providerEnum);
    }

    public SmsProvider getProvider(SmsProviderEnum providerEnum) {
        SmsProvider provider = providerMap.get(providerEnum);
        if (provider == null) {
            throw new GlobalException("不支持的短信服务商：" + providerEnum.getName());
        }
        return provider;
    }
}
