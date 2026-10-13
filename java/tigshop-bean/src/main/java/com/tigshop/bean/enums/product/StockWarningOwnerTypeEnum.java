package com.tigshop.bean.enums.product;

import lombok.Getter;

/**
 * 库存预警列表的归属主体类型。
 * 自营不是 shop 表记录，而是约定 shopId=0 的虚拟主体；店铺与门店由 shop.shop_type 区分。
 *
 * @author Tigshop团队
 */
@Getter
public enum StockWarningOwnerTypeEnum {

    SELF_OPERATED(1, "自营"),
    SHOP(2, "店铺"),
    STORE(3, "门店");

    private final Integer code;
    private final String name;

    StockWarningOwnerTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
