// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.print;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 打印配置类型枚举
 *
 * @author Tigshop团队
 * @since 2025/7/22
 */
@Getter
@AllArgsConstructor
public enum PrintConfigTypeEnum {

    RECEIPT(1, "购物小票");

    private final Integer code;
    private final String desc;

    /**
     * 根据类型码获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知类型";
        }
        for (PrintConfigTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type.getDesc();
            }
        }
        return "未知类型";
    }

    /**
     * 根据类型码获取枚举
     */
    public static PrintConfigTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PrintConfigTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}