package com.tigshop.bean.enums.product;

import lombok.Getter;

import java.util.Arrays;

/**
 * 库存预警状态
 *
 * @author Tigshop团队
 */
@Getter
public enum StockWarningStatusEnum {

    NORMAL(0, "正常库存"),
    LOW(1, "低库存"),
    HIGH(2, "高库存"),
    OUT_OF_STOCK(3, "无货"),
    /** 未配置有效规则，仅用于列表展示与筛选，不作为已发送状态落库 */
    UNCONFIGURED(9, "未配置");

    private final Integer code;
    private final String name;

    StockWarningStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        return Arrays.stream(StockWarningStatusEnum.values())
                .filter(status -> status.getCode().equals(code))
                .findFirst()
                .map(StockWarningStatusEnum::getName)
                .orElse(null);
    }
}
