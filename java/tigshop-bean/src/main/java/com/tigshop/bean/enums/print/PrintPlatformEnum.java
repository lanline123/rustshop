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
 * 打印机平台枚举
 *
 * @author Tigshop团队
 * @since 2025/1/15
 */
@Getter
@AllArgsConstructor
public enum PrintPlatformEnum {

    FEIEYUN(1, "飞鹅云"),
    SUNMI(2, "商米云");

    private final Integer code;
    private final String desc;

    /**
     * 根据平台码获取描述
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知平台";
        }
        for (PrintPlatformEnum platform : values()) {
            if (platform.getCode().equals(code)) {
                return platform.getDesc();
            }
        }
        return "未知平台";
    }

    /**
     * 根据平台码获取枚举
     */
    public static PrintPlatformEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PrintPlatformEnum platform : values()) {
            if (platform.getCode().equals(code)) {
                return platform;
            }
        }
        return null;
    }
}