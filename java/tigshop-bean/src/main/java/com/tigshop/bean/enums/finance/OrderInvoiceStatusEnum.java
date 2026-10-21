package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Arrays;

/**
 * @author yangqiang
 */

@Getter
@AllArgsConstructor
public enum OrderInvoiceStatusEnum {
    STATUS_WAIT(0, "待处理"),
    STATUS_PASS(1, "已开"),
    STATUS_REFUSE(2, "失败/作废");

    private final int code;
    private final String description;

    public static boolean isValidCode(int code) {
        return Arrays.stream(values())
                .anyMatch(status -> status.code == code);
    }

    public static String getStatusName(Integer status) {
        return Arrays.stream(values())
                .filter(orderInvoiceStatusEnum -> orderInvoiceStatusEnum.code == status)
                .map(OrderInvoiceStatusEnum::getDescription)
                .findFirst()
                .orElse("");
    }

}
