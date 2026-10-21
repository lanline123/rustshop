package com.tigshop.bean.enums.order;

import lombok.Getter;

/**
 * 支付状态枚举
 * @author tigshop团队
 */
@Getter
public enum PayStatusEnum {
    /**
     * 待支付
     */
    UNPAID(0, "待支付"),
    
    /**
     * 已支付
     */
    PAID(1, "已支付"),
    
    /**
     * 支付失败
     */
    FAILED(2, "支付失败");

    private final Integer code;
    private final String description;

    PayStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}