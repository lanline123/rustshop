package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 分账触发方式
 *
 * @author Tigshop团队
 */
@Getter
public enum ProfitSharingTriggerType {
    MANUAL(1, "手动"),
    AUTO(2, "自动");

    private final Integer code;
    private final String desc;

    ProfitSharingTriggerType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProfitSharingTriggerType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProfitSharingTriggerType s : values()) {
            if (s.getCode().equals(code)) {
                return s;
            }
        }
        return null;
    }
}
