package com.tigshop.bean.enums.vendor;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 供应商商品规格库存操作
 *
 * @author kidd
 * @since 2025/7/14 10:34
 */
@Getter
@AllArgsConstructor
public enum VendorProductSkuStockOperationEnum {

    ADD(1, "增加"),
    SUBTRACT(2, "减少"),
    ;

    private final int code;
    private final String desc;
}
