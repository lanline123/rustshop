package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 结算账期类型枚举
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
@Getter
public enum SettlementTypeEnum {

    MONTHLY(1, "月结", 30),
    QUARTERLY(2, "季结", 90);

    private final Integer code;
    private final String description;
    private final Integer days;

    SettlementTypeEnum(Integer code, String description, Integer days) {
        this.code = code;
        this.description = description;
        this.days = days;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (SettlementTypeEnum type : SettlementTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type.getDescription();
            }
        }
        return "";
    }

    /**
     * 根据code获取账期天数
     */
    public static Integer getDaysByCode(Integer code) {
        for (SettlementTypeEnum type : SettlementTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type.getDays();
            }
        }
        return 30;
    }
}
