package com.tigshop.bean.enums.content;

import lombok.Getter;

@Getter
public enum WechatLiveAuditStatus {
    NOT_AUDITED(0, "未审核"),
    AUDITING(1, "审核中"),
    AUDIT_PASSED(2, "审核通过"),
    AUDIT_REJECTED(3, "审核驳回");

    private final int code;
    private final String description;

    WechatLiveAuditStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getStatusName(Integer status) {
        if (status == null) {
            return "";
        }
        for (WechatLiveAuditStatus auditStatus : values()) {
            if (auditStatus.code == status) {
                return auditStatus.getDescription();
            }
        }
        return "";
    }
}
