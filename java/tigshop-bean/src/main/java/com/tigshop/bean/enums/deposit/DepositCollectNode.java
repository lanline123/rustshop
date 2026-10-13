package com.tigshop.bean.enums.deposit;

import lombok.Getter;

/**
 * 保证金收取节点枚举
 *
 * @author Tigshop团队
 * @create 2026-06-09
 */
@Getter
public enum DepositCollectNode {

    ON_SHELF(1, "上架时收"),
    FIRST_ORDER(2, "首单时收");

    private final Integer code;
    private final String description;

    DepositCollectNode(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        for (DepositCollectNode node : DepositCollectNode.values()) {
            if (node.getCode().equals(code)) {
                return node.getDescription();
            }
        }
        return "";
    }
}
