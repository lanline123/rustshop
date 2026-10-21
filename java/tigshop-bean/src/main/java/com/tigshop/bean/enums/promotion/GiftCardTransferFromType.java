package com.tigshop.bean.enums.promotion;

import lombok.Getter;

@Getter
public enum GiftCardTransferFromType {
    H5(1, "H5"),
    PC(2, "PC");

    private final Integer code;
    private final String name;

    GiftCardTransferFromType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
