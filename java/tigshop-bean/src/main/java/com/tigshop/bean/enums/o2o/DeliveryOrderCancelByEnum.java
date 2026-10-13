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
 * 派送订单取消方枚举
 *
 * @author Tigshop团队
 * @create 2024-12-16
 */
@Getter
public enum DeliveryOrderCancelByEnum {
    STAFF(1, "配送员/服务员"),
    ADMIN(2, "后台管理员"),
    SYSTEM(3, "系统");

    private final int code;
    private final String description;

    DeliveryOrderCancelByEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code code
     * @return DeliveryOrderCancelByEnum
     */
    public static DeliveryOrderCancelByEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeliveryOrderCancelByEnum cancelByEnum : values()) {
            if (cancelByEnum.getCode() == code) {
                return cancelByEnum;
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
        DeliveryOrderCancelByEnum cancelByEnum = getByCode(code);
        return cancelByEnum != null ? cancelByEnum.getDescription() : "";
    }
}
