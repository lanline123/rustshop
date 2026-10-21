package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 提现方式
 *
 * @author Tigshop团队
 */
@Getter
public enum WithdrawType {
    OFFLINE(1, "线下提现"),
    ONLINE(2, "线上提现");

    private final Integer code;
    private final String desc;

    WithdrawType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WithdrawType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WithdrawType t : values()) {
            if (t.getCode().equals(code)) {
                return t;
            }
        }
        return null;
    }
}
