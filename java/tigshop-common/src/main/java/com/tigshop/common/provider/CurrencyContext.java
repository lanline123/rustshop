package com.tigshop.common.provider;

import com.tigshop.common.provider.impl.DefaultCurrencyNameProvider;
import lombok.Setter;
import org.springframework.stereotype.Component;


/**
 * @author Tigshop团队
 */
@Component
public class CurrencyContext {
    @Setter
    private static CurrencyNameProvider provider = new DefaultCurrencyNameProvider();

    public static String getCurrencyName() {
        return provider.getCurrencyName();
    }
}
