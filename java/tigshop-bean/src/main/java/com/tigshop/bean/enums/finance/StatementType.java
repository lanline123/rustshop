package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对账单类型枚举
 * @author Tigshop项目组
 */
@Getter
@AllArgsConstructor
public enum StatementType {
    HANDLING_FEE(1, "手续费"),
    SERVICE_FEE(2, "服务费"),
    ORDER(3, "订单收支"),
    SHOP_WITHDRAWAL(4, "店铺提现收支"),
    STORE_WITHDRAWAL(5, "门店提现收支"),
    SUPPLIER_WITHDRAWAL(6, "供应商提现收支"),
    // 订单退款
    ORDER_REFUND(7, "订单退款"),
    DISTRIBUTION_COMMISSION(8, "分销佣金"),
    ;
    
    private final Integer code;
    private final String description;

    public static StatementType fromCode(Integer code) {
        for (StatementType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
