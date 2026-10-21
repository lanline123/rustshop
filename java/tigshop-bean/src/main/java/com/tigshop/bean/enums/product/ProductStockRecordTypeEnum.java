package com.tigshop.bean.enums.product;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ProductStockRecordTypeEnum {

    //11=采购入库，12=退货入库，10=其他入库，21=过期出库，22=订单出库，23=其他出库


    OUT(2, "出库"),
    EXPIRED_OUT(21, "过期出库"),
    ORDER_OUT(22, "订单出库"),
    OTHER_OUT(23, "其他出库"),
    ALLOCATE_OUT(24, "调拨出库"),
    PURCHASE_RETURN_OUT(25, "采购退货出库"),

    IN(1, "入库"),
    PURCHASE_IN(11, "采购入库"),
    RETURN_IN(12, "退货入库"),
    ALLOCATE_IN(13, "调拨入库"),
    OTHER_IN(10, "其他入库");


    private final Integer code;
    private final String name;

    ProductStockRecordTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    public static String getNameByCode(Integer code) {
        return Arrays.stream(ProductStockRecordTypeEnum.values())
                .filter(type -> type.getCode().equals(code))
                .findFirst()
                .map(ProductStockRecordTypeEnum::getName)
                .orElse(null); // 或抛出异常
    }
}
