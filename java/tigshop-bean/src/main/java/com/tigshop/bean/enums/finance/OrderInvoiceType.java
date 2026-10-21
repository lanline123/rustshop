package com.tigshop.bean.enums.finance;

import lombok.Getter;

import java.util.Arrays;

/**
 * @author yangqiang
 */

@Getter

public enum OrderInvoiceType {
    INVOICE_TYPE_ORDINARY(1, "普通发票"),
    INVOICE_TYPE_SPECIAL(2, "增值税专用发票");

    private final int code;
    private final String description;

    OrderInvoiceType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static boolean isValidCode(int code) {
        return Arrays.stream(values())
                .anyMatch(status -> status.code == code);
    }

    public static String getTypeName(Integer type) {
        return Arrays.stream(values())
                .filter(orderInvoiceStatus -> orderInvoiceStatus.code == type)
                .map(OrderInvoiceType::getDescription)
                .findFirst()
                .orElse("");
    }

}
