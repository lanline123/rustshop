package com.tigshop.bean.enums.o2o;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 门店配送类型
 *
 * @author Tigshop项目组
 * @create 2025年08月22日 14:51
 */
@Getter
@AllArgsConstructor
public enum ShopDeliveryTypeEnum {
    ALL_CHOOSE(1, "全部自提或全部配送"),
    PART_CHOOSE(2, "部分自提部分配送"),
    DELIVERY(3, "配送"),
    SELF_PICK_UP(4, "自提"),
    //同城
    CITY(5, "同城"),
    ;

    private final int code;
    private final String description;

}
