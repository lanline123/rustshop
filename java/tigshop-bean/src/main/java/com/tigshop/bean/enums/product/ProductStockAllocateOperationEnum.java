package com.tigshop.bean.enums.product;

import lombok.Getter;

@Getter
public enum ProductStockAllocateOperationEnum {

    ALLOCATE_SUBMIT(1, "提交审核"),
    ALLOCATE_CANCEL(2, "调拨作废"),
    ALLOCATE_AUDIT_PASS(3, "审核通过"),
    ALLOCATE_AUDIT_REJECT(4, "审核驳回"),
    ALLOCATE_OUT_SUBMIT(5, "调拨出库"),
    ALLOCATE_IN_SUBMIT(6, "调拨入库"),
    ALLOCATE_OUT_COMPLETE(7, "调拨出库完成");

    private final Integer code;
    private final String name;

    ProductStockAllocateOperationEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
