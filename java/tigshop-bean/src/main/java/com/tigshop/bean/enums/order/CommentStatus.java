package com.tigshop.bean.enums.order;

import lombok.Getter;

/**
 * @author Tigshop
 */
@Getter
public enum CommentStatus {
    /**
     * 待评价
     */
    PENDING(0, "待评价"),
    /**
     * 已评价
     */
    COMPLETED(1, "已评价");

    private final int code;
    private final String description;

    CommentStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 判断一个值是否为有效的 code
     *
     * @param code 状态码
     * @return 如果 code 有效则返回 true；否则返回 false
     */
    public static boolean isValidCode(int code) {
        for (CommentStatus status : values()) {
            if (status.code == code) {
                return true;
            }
        }
        return false;
    }
}
