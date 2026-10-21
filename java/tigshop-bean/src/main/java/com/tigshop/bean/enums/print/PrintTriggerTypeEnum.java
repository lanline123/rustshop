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
 * 打印任务触发方式枚举
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Getter
@AllArgsConstructor
public enum PrintTriggerTypeEnum {

    AUTO(1, "自动"),
    MANUAL(2, "手动补打");

    private final Integer code;
    private final String desc;

    /**
     * 根据触发方式码获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知";
        }
        for (PrintTriggerTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type.getDesc();
            }
        }
        return "未知";
    }

    /**
     * 根据触发方式码获取枚举
     */
    public static PrintTriggerTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PrintTriggerTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
