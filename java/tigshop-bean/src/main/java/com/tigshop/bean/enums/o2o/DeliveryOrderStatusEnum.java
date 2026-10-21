// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.o2o;

import lombok.Getter;

/**
 * 派送订单状态枚举
 *
 * @author Tigshop团队
 * @create 2024-12-16
 */
@Getter
public enum DeliveryOrderStatusEnum {
    PENDING_DISPATCH(10, "待派发"),
    PENDING_PICKUP(20, "待取货/待服务"),
    IN_PROGRESS(30, "配送中/服务中"),
    COMPLETED(40, "已完成"),
    CANCELLED(50, "已取消");

    private final int code;
    private final String description;

    DeliveryOrderStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code code
     * @return DeliveryOrderStatusEnum
     */
    public static DeliveryOrderStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeliveryOrderStatusEnum statusEnum : values()) {
            if (statusEnum.getCode() == code) {
                return statusEnum;
            }
        }
        return null;
    }

    /**
     * 根据code获取描述
     *
     * @param code code
     * @return String
     */
    public static String getDescriptionByCode(Integer code) {
        DeliveryOrderStatusEnum statusEnum = getByCode(code);
        return statusEnum != null ? statusEnum.getDescription() : "";
    }

    /**
     * 根据订单类型获取对应的状态描述
     *
     * @param code     状态码
     * @param type     订单类型：1-配送订单，2-服务订单
     * @return String
     */
    public static String getDescriptionByCodeAndType(Integer code, Integer type) {
        DeliveryOrderStatusEnum statusEnum = getByCode(code);
        if (statusEnum == null) {
            return "";
        }
        // 根据订单类型返回对应的状态描述
        if (type != null && type == DeliveryOrderTypeEnum.SERVICE.getCode()) {
            // 服务订单
            return switch (statusEnum) {
                case PENDING_DISPATCH -> "待抢单";
                case PENDING_PICKUP -> "待服务";
                case IN_PROGRESS -> "服务中";
                case COMPLETED -> "已完成";
                case CANCELLED -> "已取消";
            };
        } else {
            // 配送订单
            return switch (statusEnum) {
                case PENDING_DISPATCH -> "待抢单";
                case PENDING_PICKUP -> "待取货";
                case IN_PROGRESS -> "配送中";
                case COMPLETED -> "已完成";
                case CANCELLED -> "已取消";
            };
        }
    }
}
