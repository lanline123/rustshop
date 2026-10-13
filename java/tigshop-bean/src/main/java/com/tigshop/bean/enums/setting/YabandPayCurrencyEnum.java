package com.tigshop.bean.enums.setting;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 雅邦支付货币枚举
 *
 * @author kidd
 * @since 2025/4/7 09:22
 */
@Getter
@AllArgsConstructor
public enum YabandPayCurrencyEnum {

    EUR("EUR", "欧元"),
    CNY("CNY", "人民币"),
    HKD("HKD", "港币"),
    CHF("CHF", "瑞士法郎"),
    DKK("DKK", "丹麦克朗"),
    SEK("SEK", "瑞典克朗"),
    PLN("PLN", "波兰兹罗提"),
    NOK("NOK", "挪威克朗"),
    USD("USD", "美元"),
    HUF("HUF", "匈牙利福林"),
    CZK("CZK", "捷克克朗"),

    ;

    private final String code;

    private final String desc;
}
