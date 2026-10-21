package com.tigshop.bean.enums.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * paypal 货币枚举
 *
 * @author kidd
 * @since 2025/4/7 09:24
 */
@Getter
@AllArgsConstructor
public enum PaypalCurrencyEnum {

    AUD("AUD", "澳元"),
    CAD("CAD", "加元"),
    EUR("EUR", "欧元"),
    GBP("GBP", "英镑"),
    JPY("JPY", "日元"),
    HKD("HKD", "港元"),
    USD("USD", "美元"),

    ;

    private final String code;

    private final String desc;

}
