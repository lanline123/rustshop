package com.tigshop.bean.enums.salesman;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分销设置类型
 *
 * @author kidd
 * @since 2025/6/20 17:54
 */
@Getter
@AllArgsConstructor
public enum SalesmanConfigTypeEnum {

    SALESMAN_CONFIG("salesmanConfig"),
    SALESMAN_SETTLEMENT("salesmanSettlement");

    private String code;

}
