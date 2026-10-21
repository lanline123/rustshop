package com.tigshop.bean.enums.finance;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 发票抬头
 *
 * @author  baishang
 * @create  2024-12-25
 */
@Getter
public enum OrderInvoiceTitle {
    PERSONAL(1, "个人"),
    COMPANY(2, "企业");

    private final int code;
    private final String description;

    OrderInvoiceTitle(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static boolean isValidCode(int code) {
        for (OrderInvoiceTitle type : OrderInvoiceTitle.values()) {
            if (type.getCode() == code) {
                return true;
            }
        }
        return false;
   }

   public static String getTitleName(int code) {
       for (OrderInvoiceTitle type : OrderInvoiceTitle.values()) {
           if (type.getCode() == code) {
               return type.getDescription();
           }
       }
       throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
   }
}
