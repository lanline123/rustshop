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

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.enums.order;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;

/**
 * @author Tigshop
 */
@Getter
public enum ShippingStatusEnum {
    /**
     * 待发货
     */
    PENDING(0, "待发货"),
    /**
     * 已发货
     */
    SENT(1, "已发货"),
    /**
     * 已收货
     */
    SHIPPED(2, "已收货"),
    /**
     * 配送失败
     */
    FAILED(3, "配送失败"),
    /**
     * 配送中(同城配送) => 已被抢单
     */
    IN_TRANSIT(4, "配送中"),
    /**
     * 已发货,待配送(同城配送)
     */
    SENT_WAITING_DELIVERY(5, "已发货,待配送");

    private final int code;
    private final String description;

    ShippingStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 判断一个值是否为有效的 code
     *
     * @param code 状态码
     * @return 如果 code 有效则返回 true；否则返回 false
     */
    public static boolean isValidCode(int code) {
        for (ShippingStatusEnum status : values()) {
            if (status.code == code) {
                return true;
            }
        }
        return false;
    }

    public static String getStatusName(Integer status) {
        for (ShippingStatusEnum shippingStatusEnum : values()) {
            if (shippingStatusEnum.code == status) {
                return shippingStatusEnum.getDescription();
            }
        }
        return "";
    }

    public static ShippingStatusEnum getStatus(Integer status) {
        for (ShippingStatusEnum shippingStatusEnum : values()) {
            if (shippingStatusEnum.code == status) {
                return shippingStatusEnum;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }
}
