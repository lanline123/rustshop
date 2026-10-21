package com.tigshop.bean.enums.product;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ProductStockAllocateStateEnum {

    //1=待审核，2待调出，3=待调入,4=已完成，5=已驳回，6=已作废

    AUDIT_WAIT(1, "待审核"),
    STOCK_OUT_WAIT(2, "待调出"),
    STOCK_IN_WAIT(3, "待调入"),
    COMPLETE(4, "已完成"),
    CANCEL(6, "已作废"),
    AUDIT_REJECT(5, "已驳回")
    ;

    private final Integer code;
    private final String name;

    ProductStockAllocateStateEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        return Arrays.stream(ProductStockAllocateStateEnum.values())
                .filter(type -> type.getCode().equals(code))
                .findFirst()
                .map(ProductStockAllocateStateEnum::getName)
                .orElse(null);
    }
}
