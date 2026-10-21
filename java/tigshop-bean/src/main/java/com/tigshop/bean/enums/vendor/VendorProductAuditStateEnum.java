package com.tigshop.bean.enums.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 供应商商品审核状态
 *
 * @author kidd
 * @since 2025/7/10 13:32
 */
@Getter
@AllArgsConstructor
public enum VendorProductAuditStateEnum {

    WAIT_AUDIT(0, "待审核"),

    PASS_AUDIT(1, "审核通过"),

    REJECT_AUDIT(2, "审核失败"),
    ;

    private final int code;
    private final String desc;
}
