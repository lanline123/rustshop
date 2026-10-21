package com.tigshop.bean.enums.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wzh
 */

@Getter
@AllArgsConstructor
public enum BrandStatusEnum {
    /**
     * 待审核
     */
    PENDING(0, "待审核"),
    /**
     * 审核通过
     */
    APPROVED(1, "审核通过"),
    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝");

    private final int code;
    private final String description;


    public static String getStatusName(Integer status) {
        for (BrandStatusEnum statusEnum : values()) {
            if (statusEnum.code == status) {
                return statusEnum.getDescription();
            }
        }
        return null;
    }
}