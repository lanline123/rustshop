package com.tigshop.bean.enums.o2o;

import lombok.Getter;

/**
 * 管理员消息分类
 *
 * @author tigshop
 * @create 2024-12-25
 */
@Getter
public enum ShopTypeEnum {
    SHOP(1, "店铺"),
    STORE(2, "门店"),
    PICKUP(3, "自提点"),;

    private final int code;
    private final String description;

    ShopTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

}
