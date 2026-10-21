package com.tigshop.bean.enums.order;

import com.tigshop.bean.enums.vendor.VendorProductSkuStockBizEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品组织类型枚举
 *
 * @author kidd
 * @since 2025/8/21 14:54
 */
@Getter
@AllArgsConstructor
public enum OrderDeliveryTypeEnum {
    LOGISTICS(1, "物流配送"),
    PICKUP(2, "自提"),
    SAME_CITY(3, "同城配送"),
    ;

    private final Integer code;
    private final String desc;
    

    //根据code获得desc的方法
    public static String getDesc(Integer code) {
        for (OrderDeliveryTypeEnum orderDeliveryTypeEnum : values()) {
            if (orderDeliveryTypeEnum.code == code) {
                return orderDeliveryTypeEnum.desc;
            }
        }
        return "";
    }
}
