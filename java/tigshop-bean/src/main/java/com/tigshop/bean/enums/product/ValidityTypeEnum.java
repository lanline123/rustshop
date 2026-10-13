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
public enum ValidityTypeEnum {

    PERMANENT(1, "长期有效"),
    SAME_DAY(2, "当天有效"),
    N_DAYS(3, "N天内有效"),
    FIXED_RANGE(4, "固定日期区间");

    private final Integer code;
    private final String name;

    public static ValidityTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ValidityTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static String getNameByCode(Integer code) {
        ValidityTypeEnum type = getByCode(code);
        return type != null ? type.getName() : "";
    }
}
