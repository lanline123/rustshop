package com.tigshop.common.provider.impl;

import com.tigshop.common.provider.CurrencyNameProvider;

/**
 * @author Tigshop团队
 */
public class DefaultCurrencyNameProvider implements CurrencyNameProvider {
    @Override
    public String getCurrencyName() {
        // 默认值
        return "积分";
    }
}
