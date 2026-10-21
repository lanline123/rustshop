package com.tigshop.bean.enums.finance;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 发票状态
 *
 * @author  baishang
 * @create  2024-12-25
 */
@Getter
public enum UserInvoiceStatus {
    //审核通过
    AUDIT_PASS(1, "审核通过"),
    //审核不通过
    AUDIT_FAIL(2, "待审核"),
    //待审核
    AUDIT_WAIT(3, "审核不通过");

    private final int code;
    private final String description;

    UserInvoiceStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescription(int code) {
        for (UserInvoiceStatus status : UserInvoiceStatus.values()) {
            if (status.getCode() == code) {
                return status.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }

}
