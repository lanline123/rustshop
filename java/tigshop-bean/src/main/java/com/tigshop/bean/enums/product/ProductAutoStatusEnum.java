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
 * 商品自动上下架状态枚举
 *
 * @author Tigshop团队
 */
@Getter
@AllArgsConstructor
public enum ProductAutoStatusEnum {

    /**
     * 待执行
     */
    PENDING(0, "待执行"),

    /**
     * 执行成功
     */
    SUCCESS(1, "执行成功"),

    /**
     * 执行失败
     */
    FAILED(2, "执行失败");

    private final Integer code;
    private final String description;

    public static ProductAutoStatusEnum fromCode(Integer code) {
        for (ProductAutoStatusEnum value : ProductAutoStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}

