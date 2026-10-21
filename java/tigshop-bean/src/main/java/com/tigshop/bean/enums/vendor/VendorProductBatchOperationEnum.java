package com.tigshop.bean.enums.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 供应商商品批量操作枚举
 */
@Getter
@AllArgsConstructor
public enum VendorProductBatchOperationEnum {

    ONSALE("onsale", "起售"),
    OUTAGE("outage", "断供"),
    RECYCLE("recycle", "回收"),
    RESTORE("restore", "还原"),
    DEL("del", "删除");

    private final String code;

    private final String desc;
}
