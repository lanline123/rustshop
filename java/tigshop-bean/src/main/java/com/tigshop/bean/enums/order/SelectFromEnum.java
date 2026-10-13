package com.tigshop.bean.enums.order;

import lombok.Getter;

/**
 * 跳转类型(从哪个页面跳转)
 *
 * @author Tigshop项目组
 * @create 2025年07月08日 15:40
 */
@Getter
public enum SelectFromEnum {
    // 首次加载
    FIRST_LOAD(1, "首次加载"),
    CHECK_INDEX(2, "结算页面"),
    CHECK_UPDATE(3, "更新优惠券"),
    CHECK_SUBMIT(4, "提交订单");

    private final int value;
    private final String desc;

    SelectFromEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
