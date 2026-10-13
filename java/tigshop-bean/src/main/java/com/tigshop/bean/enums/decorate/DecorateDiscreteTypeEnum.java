package com.tigshop.bean.enums.decorate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 装修组件类型
 *
 * @author kidd
 * @since 2025/6/30 09:23
 */
@Getter
@AllArgsConstructor
public enum DecorateDiscreteTypeEnum {

    MEMBER_DECORATE("member_Decorate", "会员首页"),
    MOBILE_NAV("mobileNav", "主导航栏"),
    PC_USER_LOGIN("pcUserLogin", "其他页面"),
    INVESTMENT_PROMOTION_HOMEPAGE("investmentPromotionHomepage", "店铺招商首页"),
    INVESTMENT_PLAN("investmentPlan", "店铺招商计划"),
    INVESTMENT_CRITERIA("investmentCriteria", "店铺招商标准"),
    SETTLING_PROCESS("settlingProcess", "店铺入驻流程"),
    SHOP_HEAD("shopHead", "店铺头部"),
    OPEN_ADVERTISING("openAdvertising", "开屏广告"),
    PC_INDEX_TIPS("PcIndexTips", "跨境 PC 顶部提示"),
    ;

    private final String code;
    private final String description;

    public static DecorateDiscreteTypeEnum getByCode(String code) {
        for (DecorateDiscreteTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
