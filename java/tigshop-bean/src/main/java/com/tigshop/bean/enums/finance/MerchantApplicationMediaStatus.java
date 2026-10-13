package com.tigshop.bean.enums.finance;

import lombok.Getter;

/**
 * 商户进件媒体状态
 *
 * @author Tigshop团队
 */
@Getter
public enum MerchantApplicationMediaStatus {
    UPLOADING(0),
    TEMP(1),
    BOUND(2),
    DELETE_PENDING(3),
    DELETED(4);

    private final Integer code;

    MerchantApplicationMediaStatus(Integer code) {
        this.code = code;
    }
}
