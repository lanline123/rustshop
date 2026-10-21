package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 二级商户状态
 *
 * @author Tigshop团队
 */
@Getter
public enum SubMerchantStatus {
    INACTIVE(0, "待激活"),
    ACTIVE(1, "已激活"),
    FROZEN(2, "已冻结");

    private final Integer code;
    private final String desc;

    SubMerchantStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SubMerchantStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SubMerchantStatus s : values()) {
            if (s.getCode().equals(code)) {
                return s;
            }
        }
        return null;
    }
}
