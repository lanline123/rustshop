package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 分账完结状态
 *
 * @author Tigshop团队
 */
@Getter
public enum ProfitSharingFinishStatus {
    UNFINISHED(0, "未完结"),
    FINISHED(1, "已完结");

    private final Integer code;
    private final String desc;

    ProfitSharingFinishStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProfitSharingFinishStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProfitSharingFinishStatus s : values()) {
            if (s.getCode().equals(code)) {
                return s;
            }
        }
        return null;
    }
}
