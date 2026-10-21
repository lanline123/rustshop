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
 * 打印机状态枚举
 *
 * @author Tigshop团队
 * @since 2025/1/15
 */
@Getter
@AllArgsConstructor
public enum PrintStatusEnum {

    ENABLED(1, "启用"),
    DISABLED(2, "停用");

    private final Integer code;
    private final String desc;

    /**
     * 根据状态码获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知";
        }
        for (PrintStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知";
    }

    /**
     * 根据状态码获取枚举
     */
    public static PrintStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PrintStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}