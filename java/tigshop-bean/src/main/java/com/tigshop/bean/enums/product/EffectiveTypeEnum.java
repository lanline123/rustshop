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

@Getter
@AllArgsConstructor
public enum EffectiveTypeEnum {

    IMMEDIATE(1, "立即生效"),
    NEXT_DAY(2, "次日生效"),
    DELAY(3, "延迟生效");

    private final Integer code;
    private final String name;

    public static EffectiveTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EffectiveTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        EffectiveTypeEnum type = getByCode(code);
        return type != null ? type.getName() : "";
    }
}
