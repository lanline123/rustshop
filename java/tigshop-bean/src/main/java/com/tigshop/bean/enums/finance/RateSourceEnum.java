package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单结算费率命中来源（店铺侧 / 供应商侧分别使用）
 */
@Getter
@AllArgsConstructor
public enum RateSourceEnum {
    RULE_SHOP_LAYER1("RULE_SHOP_LAYER1"),
    RULE_SHOP_LAYER2("RULE_SHOP_LAYER2"),
    RULE_SHOP_LAYER3("RULE_SHOP_LAYER3"),
    RULE_VENDOR_LAYER1("RULE_VENDOR_LAYER1"),
    RULE_VENDOR_LAYER2("RULE_VENDOR_LAYER2"),
    RULE_VENDOR_LAYER3("RULE_VENDOR_LAYER3"),
    GLOBAL_DEFAULT("GLOBAL_DEFAULT");

    private final String value;
}
