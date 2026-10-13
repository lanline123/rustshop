package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 商户进件媒体角色
 *
 * @author Tigshop团队
 */
@Getter
public enum MerchantApplicationMediaRole {
    UNBOUND(0),
    BUSINESS_LICENSE(1),
    ID_CARD_PORTRAIT(2),
    ID_CARD_NATIONAL(3);

    private final Integer code;

    MerchantApplicationMediaRole(Integer code) {
        this.code = code;
    }
}
