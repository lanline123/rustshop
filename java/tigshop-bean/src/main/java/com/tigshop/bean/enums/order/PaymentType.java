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
public enum PaymentType {
    /**
     * 在线支付（微信、支付宝等）
     */
    ONLINE(1, "在线支付"),
    /**
     * 货到付款（支持先发货，后付款）
     */
    COD(2, "货到付款"),
    /**
     * 线下支付（银行汇款）
     */
    OFFLINE(3, "线下支付"),
    /**
     * 现金支付
     */
    CASH(4, "现金支付"),
    ;

    private final int code;
    private final String description;

    PaymentType(int code, String description) {
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
        for (PaymentType status : values()) {
            if (status.code == code) {
                return true;
            }
        }
        return false;
    }

    public static String getStatusName(Integer status) {
        for (PaymentType paymentType : values()) {
            if (paymentType.code == status) {
                return paymentType.getDescription();
            }
        }
        return "";
    }

    public static PaymentType getStatus(Integer status) {
        for (PaymentType paymentType : values()) {
            if (paymentType.code == status) {
                return paymentType;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }
}
