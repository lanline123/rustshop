package com.tigshop.bean.enums.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStyleActivityStatusEnum {

    NOT_STARTED(1, "活动未开始"),
    IN_PROGRESS(2, "活动进行中"),
    ENDED(3, "活动已结束");

    private final int code;
    private final String description;

    public static String getStatusName(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStyleActivityStatusEnum statusEnum : values()) {
            if (statusEnum.code == code) {
                return statusEnum.getDescription();
            }
        }
        return null;
    }

    /**
     * 根据开始时间和结束时间计算活动状态
     * @param startTime 开始时间（秒级时间戳）
     * @param endTime 结束时间（秒级时间戳）
     * @return 活动状态码，时间为空返回 null
     */
    public static Integer calcStatus(Long startTime, Long endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        long now = System.currentTimeMillis() / 1000;
        if (now < startTime) {
            return NOT_STARTED.code;
        } else if (now > endTime) {
            return ENDED.code;
        } else {
            return IN_PROGRESS.code;
        }
    }
}
