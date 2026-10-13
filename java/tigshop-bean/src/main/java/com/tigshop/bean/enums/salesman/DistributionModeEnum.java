package com.tigshop.bean.enums.salesman;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分销模式
 *
 * @author Tigshop团队
 * @since 2026/6/12
 */
@Getter
@AllArgsConstructor
public enum DistributionModeEnum {

    /** 推广员等级模式（默认） */
    PROMOTER(1, "推广员等级模式"),

    /** 微商模式 */
    WECHAT_BIZ(2, "微商模式");

    private final Integer code;
    private final String desc;

    /**
     * 老数据无该字段时一律按推广员等级模式处理
     */
    public static DistributionModeEnum fromCode(Integer code) {
        if (code != null && WECHAT_BIZ.getCode().equals(code)) {
            return WECHAT_BIZ;
        }
        return PROMOTER;
    }

    public static boolean isWechatBiz(Integer code) {
        return WECHAT_BIZ.getCode().equals(fromCode(code).getCode());
    }
}
