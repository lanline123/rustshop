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
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;

/**
 * @author Tigshop
 */
@Getter
@AllArgsConstructor
public enum OrderSameCityEnum {
    /**
     * 待确认，待支付
     */
    PENDING(0, "待支付"),
    /**
     * 已确认，待发货（支付后同步为此状态）
     */
    CONFIRMED(1, "已确认，待发货"),
    /**
     * 处理中，已发货（发货后同步为此状态）
     */
    PROCESSING(2, "配送中"),
    /**
     * 已取消
     */
    CANCELLED(3, "已取消"),
    /**
     * 无效
     */
    INVALID(4, "无效"),
    /**
     * 已完成
     */
    COMPLETED(5, "已完成");

    private final int code;
    private final String description;

    /**
     * 判断一个值是否为有效的 code
     *
     * @param code 状态码
     * @return 如果 code 有效则返回 true；否则返回 false
     */
    public static boolean isValidCode(int code) {
        for (OrderSameCityEnum status : values()) {
            if (status.code == code) {
                return true;
            }
        }
        return false;
    }

    public static String getStatusName(Integer status) {
        for (OrderSameCityEnum orderStatusEnum : values()) {
            if (orderStatusEnum.code == status) {
                return orderStatusEnum.getDescription();
            }
        }
        return "";
    }

    public static OrderSameCityEnum getStatus(Integer status) {
        for (OrderSameCityEnum orderStatusEnum : values()) {
            if (orderStatusEnum.code == status) {
                return orderStatusEnum;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }

    public static OrderSameCityEnum getPreStatus(int code) {
        return switch (code) {
            // 待发货 -> 待支付
            case 1 -> CANCELLED;
            // 已发货 -> 待发货
            case 2 -> CONFIRMED;
            default -> null;
        };
    }
}
