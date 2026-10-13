package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 账期订单结算状态枚举
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Getter
public enum CreditOrderSettlementStatusEnum {

    UNSETTLED(0, "待结算"),
    SETTLED(1, "已结算"),
    /**
     * 订单已退款/作废，不再参与结算
     */
    CANCELLED(2, "已取消"),
    /**
     * 售后/退款处理中，暂不允许结算
     */
    REFUNDING(3, "退款中");

    private final Integer code;
    private final String description;

    CreditOrderSettlementStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (CreditOrderSettlementStatusEnum status : CreditOrderSettlementStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getDescription();
            }
        }
        return "";
    }
}
