// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

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

import lombok.Getter;

/**
 * 商品属性类型
 *
 * @author Tigshop团队
 * @create 2024年11月27日 17:23
 */
@Getter
public enum AttrType {
    NORMAL(0, "normal", "普通属性"),
    SPEC(1, "spec", "规格属性"),
    EXTRA(2, "extra", "附加规格");

    private final int code;
    private final String attrName;
    private final String description;

    AttrType(int code, String attrName, String description) {
        this.code = code;
        this.attrName = attrName;
        this.description = description;
    }

    public static AttrType fromCode(int code) {
        for (AttrType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
