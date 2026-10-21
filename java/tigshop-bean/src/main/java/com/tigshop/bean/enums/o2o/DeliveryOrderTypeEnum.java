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
 * 派送订单类型枚举
 *
 * @author Tigshop团队
 * @create 2024-12-16
 */
@Getter
public enum DeliveryOrderTypeEnum {
    DELIVERY(1, "配送订单"),
    SERVICE(2, "服务订单");

    private final int code;
    private final String description;

    DeliveryOrderTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code code
     * @return DeliveryOrderTypeEnum
     */
    public static DeliveryOrderTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeliveryOrderTypeEnum typeEnum : values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum;
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
        DeliveryOrderTypeEnum typeEnum = getByCode(code);
        return typeEnum != null ? typeEnum.getDescription() : "";
    }
}
