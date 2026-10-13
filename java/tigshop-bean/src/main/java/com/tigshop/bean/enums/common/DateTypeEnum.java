package com.tigshop.bean.enums.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Tigshop项目组
 * @create 2025年10月11日 15:21
 */
@Getter
@AllArgsConstructor
public enum DateTypeEnum {
    ALL(0, "全部")
    , THREE_MONTH(1, "3个月")
    , SIX_MONTH(2, "6个月")
    , TWELVE_MONTH(3, "12个月")
    ;
    private final Integer code;
    private final String name;

    /**
     * 根据code获取DateTypeEnum枚举
     * @param code 枚举code
     * @return 对应的DateTypeEnum枚举
     */
    public static DateTypeEnum getByCode(Integer code) {
        for (DateTypeEnum type : DateTypeEnum.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return ALL;
    }

    /**
     * 根据枚举code获取时间戳范围
     * @param code 枚举code
     * @return 包含开始时间和结束时间的时间戳数组
     */
    public static long[] getTimestampRangeByCode(Integer code) {
        DateTypeEnum dateType = getByCode(code);
        return dateType.getTimestampRange();
    }

    /**
     * 获取时间戳范围
     * @return 包含开始时间和结束时间的时间戳数组
     */
    public long[] getTimestampRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;

        switch (this) {
            case THREE_MONTH:
                start = now.minusMonths(3);
                break;
            case SIX_MONTH:
                start = now.minusMonths(6);
                break;
            case TWELVE_MONTH:
                start = now.minusMonths(12);
                break;
            case ALL:
            default:
                // 对于"全部"，返回一个较早的时间点
                return new long[]{0L, now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()};
        }

        long startTime = start.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long endTime = now.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

        return new long[]{startTime, endTime};
    }
}
