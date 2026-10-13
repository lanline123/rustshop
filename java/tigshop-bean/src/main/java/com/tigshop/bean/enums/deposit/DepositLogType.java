package com.tigshop.bean.enums.deposit;

import lombok.Getter;

/**
 * 保证金流水类型枚举
 *
 * @author Tigshop团队
 * @create 2026-06-09
 */
@Getter
public enum DepositLogType {

    RECHARGE(1, "充值入账"),
    WITHDRAW(2, "提现出账"),
    /** 暂不使用，与 WITHDRAW 合并；存量流水仍可能为 3 */
    REFUND(3, "退还出账"),
    DEDUCT(4, "平台扣除"),
    ADJUST(5, "平台调整");

    private final Integer code;
    private final String description;

    DepositLogType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        if (REFUND.getCode().equals(code)) {
            return WITHDRAW.getDescription();
        }
        for (DepositLogType type : DepositLogType.values()) {
            if (type.getCode().equals(code)) {
                return type.getDescription();
            }
        }
        return "";
    }

    /** 提现/退还出账筛选：2 与 3 暂合并为同一类（新流水仅写 2）。 */
    public static boolean isWithdrawCategory(Integer code) {
        return WITHDRAW.getCode().equals(code) || REFUND.getCode().equals(code);
    }

    /** 对外展示/写入统一为 2；存量 type=3 归并到 2。 */
    public static Integer normalizeCode(Integer code) {
        return REFUND.getCode().equals(code) ? WITHDRAW.getCode() : code;
    }
}
