package com.tigshop.service.common.impl;

import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.common.provider.CurrencyContext;
import com.tigshop.common.provider.CurrencyNameProvider;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Tigshop团队
 */
@Service
@RequiredArgsConstructor
public class DbCurrencyNameProvider implements CurrencyNameProvider {

    private final ConfigService configService;

    @PostConstruct
    public void init() {
        // 启动时注入
        CurrencyContext.setProvider(this);
    }

    @Override
    public String getCurrencyName() {
        // 读取数据库配置
        return configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
    }
}
