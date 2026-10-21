package com.tigshop.bean.enums.salesman;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

/**
 * 微商模式下级招募方式
 *
 * @author Tigshop团队
 * @since 2026/6/12
 */
@Getter
@AllArgsConstructor
public enum RecruitMethodEnum {

    /** 消费自动开通：客户经分销员推广下单后自动成为其下级分销员 */
    CONSUME_AUTO(1, "消费自动开通"),

    /** 名片邀请开通：扫分销名片二维码一键成为下级分销员 */
    CARD_INVITE(2, "名片邀请开通");

    private final Integer code;
    private final String desc;

    public static boolean isValidCode(Integer code) {
        for (RecruitMethodEnum method : values()) {
            if (method.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(Collection<Integer> methods, RecruitMethodEnum target) {
        return methods != null && methods.contains(target.getCode());
    }
}
