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
 * 商品操作类型枚举
 *
 * @author Tigshop团队
 */
@Getter
@AllArgsConstructor
public enum ProductOperationTypeEnum {

    /**
     * 下架
     */
    OFFLINE(0, "下架"),

    /**
     * 上架
     */
    ONLINE(1, "上架");

    private final Integer code;
    private final String description;

    public static ProductOperationTypeEnum fromCode(Integer code) {
        for (ProductOperationTypeEnum value : ProductOperationTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}

