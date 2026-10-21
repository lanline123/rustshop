// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.cart;

import lombok.Getter;

import java.util.Objects;

/**
 * cart类型
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:27
 */
@Getter
public enum CartTypeEnum {
    TYPE_NORMAL(1, "普通商品"),
    TYPE_PIN(2, "拼团商品"),
    TYPE_EXCHANGE(3, "兑换商品"),
    TYPE_GIFT(4, "赠品"),
    TYPE_BARGAIN(5, "砍一砍商品"),
    TYPE_VIRTUAL(6, "虚拟商品"),
    TYPE_PAID(7, "付费商品"),
    TYPE_CARD(8, "卡密商品"),
    TYPE_RESERVATION_STORE(9, "预约到店"),
    TYPE_RESERVATION_HOME(10, "预约上门"),
    TYPE_GIFT_CARD(11, "礼品卡商品"),
    TYPE_GIFT_EXCHANGE(12, "兑换商品"),
    TYPE_RESERVATION_PRODUCT(13, "预售商品"),
    TYPE_LOTTERY_PRODUCT(14, "抽奖商品"),
    TYPE_ADMIN_SHOPPING(15, "代客下单"),
    ;

    private final Integer code;
    private final String name;

    CartTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据类型编码获取类型
     *
     * @param code 类型编码
     * @return 类型
     */
    public static CartTypeEnum fromTypeCode(Integer code) {
        for (CartTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    // 是否是预约类型
    public static boolean isReservationType(Integer type) {
        return Objects.equals(type, TYPE_RESERVATION_STORE.getCode()) || Objects.equals(type, TYPE_RESERVATION_HOME.getCode());
    }
}
