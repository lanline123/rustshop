// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.enums.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wzh
 */

@Getter
@AllArgsConstructor
public enum EffectiveDelayUnitEnum {

    DAY(1, "天"),
    HOUR(2, "小时");

    private final Integer code;
    private final String name;

    public static EffectiveDelayUnitEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EffectiveDelayUnitEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        EffectiveDelayUnitEnum type = getByCode(code);
        return type != null ? type.getName() : "";
    }
}
