package com.tigshop.bean.enums.finance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对账单时间类型枚举
 * @author Tigshop项目组
 */
@Getter
@AllArgsConstructor
public enum StatementTimeType {
    
    /**
     * 入账（结算）时间
     */
    SETTLEMENT_TIME(1, "入账时间"),
    
    /**
     * 下单时间
     */
    RECORD_TIME(2, "下单时间");
    
    private final Integer code;
    private final String description;

    /**
     * 根据code获取枚举
     */
    public static StatementTimeType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatementTimeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching StatementTimeType for code: " + code);
    }
}
