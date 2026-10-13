package com.tigshop.bean.enums.merchant;

import com.tigshop.common.exception.GlobalException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

@Getter
@AllArgsConstructor
public enum MerchantAccountType {
    //1.银行卡
    BANK_CARD(1, "银行卡"),
    //2.支付宝
    ALIPAY(2, "支付宝"),
    //3.微信
    WECHAT(3, "微信");

    private final Integer code;
    private final String description;

    public static String getTypeName(int code) {
        for (MerchantAccountType type : MerchantAccountType.values()) {
            if (type.getCode() == code) {
                return type.getDescription();
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR, SERVICE_DATA_ERROR);
    }

    public static Map<Integer, String> getTypeList() {
        Map<Integer, String> typeList = new HashMap<>();
        for (MerchantAccountType type : MerchantAccountType.values()) {
            typeList.put(type.getCode(), type.getDescription());
        }
        return typeList;
    }

}
