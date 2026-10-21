package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 分账状态
 *
 * @author Tigshop团队
 */
@Getter
public enum ProfitSharingStatus {
    PENDING(0, "待分账"),
    PROCESSING(1, "分账中"),
    SUCCESS(2, "分账成功"),
    FAILED(3, "分账失败"),
    /**
     * 已作废：从未向渠道发起过、也不会再发起的分账单（如无线上收款却误建的单）。
     * 与 FAILED 区分开，避免被失败重试任务反复捡起。
     */
    CANCELLED(4, "已作废");

    private final Integer code;
    private final String desc;

    ProfitSharingStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProfitSharingStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProfitSharingStatus s : values()) {
            if (s.getCode().equals(code)) {
                return s;
            }
        }
        return null;
    }
}
