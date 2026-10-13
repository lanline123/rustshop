package com.tigshop.bean.enums.deposit;

import lombok.Getter;

/**
 * 保证金收款账户类型枚举
 *
 * @author Tigshop团队
 */
@Getter
public enum DepositPayeeAccountType {

    BANK_CARD(1, "银行卡"),
    ALIPAY(2, "支付宝"),
    WECHAT(3, "微信");

    private final Integer code;
    private final String description;

    DepositPayeeAccountType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (DepositPayeeAccountType type : DepositPayeeAccountType.values()) {
            if (type.getCode().equals(code)) {
                return type.getDescription();
            }
        }
        return "";
    }
}
