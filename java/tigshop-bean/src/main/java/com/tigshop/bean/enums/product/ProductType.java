// **---------------------------------------------------------------------+
// ** 文件
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
 * 商品类型
 *
 * @author Tigshop团队
 * @create 2024年11月27日 17:23
 */
@Getter
public enum ProductType {
    PRODUCT_TYPE_NORMAL(1,  "实物商品"),
    PRODUCT_TYPE_VIRTUAL(2,  "虚拟商品"),
    PRODUCT_TYPE_CARD(3,  "卡密商品"),
    PRODUCT_TYPE_PAID(4, "付费内容"),
    PRODUCT_TYPE_RESERVATION_STORE(5, "预约上门"),
    PRODUCT_TYPE_RESERVATION_HOME(6, "预约到店"),
    PRODUCT_TYPE_GIFT_CARD(7, "实体礼品卡"),
    PRODUCT_TYPE_GIFT_CARD_VIRTUAL(8, "电子礼品卡"),
    ;

    private final int code;
    private final String description;

    ProductType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProductType fromCode(int code) {
        for (ProductType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    // 判断是否为预约商品
    public static boolean isReservationProduct(int code) {
        return code == PRODUCT_TYPE_RESERVATION_STORE.code || code == PRODUCT_TYPE_RESERVATION_HOME.code;
    }
}
