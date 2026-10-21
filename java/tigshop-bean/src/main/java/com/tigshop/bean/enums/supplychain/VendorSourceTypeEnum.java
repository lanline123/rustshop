package com.tigshop.bean.enums.supplychain;

import lombok.Getter;

@Getter
public enum VendorSourceTypeEnum {
    ONBOARDED(1, "入驻供应商"),
    SELF_BUILT(2, "自建供应商");

    private final int code;
    private final String description;

    VendorSourceTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
