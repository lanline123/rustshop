package com.tigshop.bean.enums.salesman;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分销商品分销类型
 *
 * @author kidd
 * @since 2025/6/20 18:08
 */
@Getter
@AllArgsConstructor
public enum SalesmanProductCommissionTypeEnum {

    DEFAULT_RATE(1, "默认"),

    CUSTOM_RATE(2, "自定义比例"),

    CUSTOM_PRICE(3, "自定义金额"),
    ;

    private final int code;

    private final String description;
}
