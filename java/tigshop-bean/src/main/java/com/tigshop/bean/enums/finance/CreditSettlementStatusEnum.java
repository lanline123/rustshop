package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 结算记录状态枚举
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Getter
public enum CreditSettlementStatusEnum {

    SETTLED(1, "已结算"),
    CANCELLED(2, "已撤销");

    private final Integer code;
    private final String description;

    CreditSettlementStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (CreditSettlementStatusEnum status : CreditSettlementStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getDescription();
            }
        }
        return "";
    }
}
