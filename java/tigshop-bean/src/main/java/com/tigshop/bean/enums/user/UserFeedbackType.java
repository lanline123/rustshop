package com.tigshop.bean.enums.user;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 反馈类型
 *
 * @author 企业认证实体类
 * @create  2024-12-25
 */
@Getter
public enum UserFeedbackType {
    //建议：0
    SUGGESTION(0, "建议"),
    //投诉：1
    COMPLAINT(1, "投诉"),
   //商品：2
    PRODUCT(2, "商品"),
    //其他：3
    OTHER(3, "其他"),
    //店铺投诉
    SHOP_COMPLAINT(4, "店铺投诉"),
    //订单问题
    ORDER_PROBLEM(5, "订单问题"),
   //订单咨询
    ORDER_CONSULTATION(6, "订单咨询");

    private final Integer code;
    private final String description;

    UserFeedbackType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getTypeName(int code) {
        for (UserFeedbackType type : UserFeedbackType.values()) {
            if (type.getCode() == code) {
                return type.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }

}
