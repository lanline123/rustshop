package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 结算状态枚举
 * @author Tigshop项目组
 */
@Getter
public enum SettlementStatus {
    PENDING(1, "待入账"),
    SETTLED(2, "已入账");
    
    private final Integer code;
    private final String description;
    
    SettlementStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SettlementStatus fromCode(Integer code) {
        for (SettlementStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return code + ":" + description;
    }
}
