package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 商户进件状态
 *
 * @author Tigshop团队
 */
@Getter
public enum MerchantApplicationStatus {
    DRAFT("DRAFT", "草稿"),
    PENDING_REVIEW("PENDING_REVIEW", "待审核"),
    SUBMITTED("SUBMITTED", "已提交"),
    CHECKING("CHECKING", "资料校验中"),
    AUDITING("AUDITING", "审核中"),
    ACCOUNT_NEED_VERIFY("ACCOUNT_NEED_VERIFY", "待账户验证"),
    NEED_SIGN("NEED_SIGN", "待签约"),
    FINISH("FINISH", "完成"),
    REJECTED("REJECTED", "已驳回");

    private final String code;
    private final String desc;

    MerchantApplicationStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MerchantApplicationStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        // 兼容微信原始申请单状态
        String normalized = code.trim().toUpperCase();
        for (MerchantApplicationStatus s : values()) {
            if (s.getCode().equals(normalized)) {
                return s;
            }
        }
        return switch (normalized) {
            case "APPLYMENT_STATE_FINISHED" -> FINISH;
            case "APPLYMENT_STATE_TO_BE_SIGNED" -> NEED_SIGN;
            case "APPLYMENT_STATE_REJECTED" -> REJECTED;
            case "APPLYMENT_STATE_AUDITING" -> AUDITING;
            case "APPLYMENT_STATE_TO_BE_CONFIRMED", "ACCOUNT_NEED_VERIFY" -> ACCOUNT_NEED_VERIFY;
            case "CHECKING" -> CHECKING;
            default -> null;
        };
    }
}
