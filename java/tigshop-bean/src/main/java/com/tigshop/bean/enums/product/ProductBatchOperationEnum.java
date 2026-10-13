package com.tigshop.bean.enums.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 批量操作枚举
 *
 * @author kidd
 * @since 2025/3/24 17:40
 */
@Getter
@AllArgsConstructor
public enum ProductBatchOperationEnum {

    UP("up", "上架"),
    DOWN("down", "下架"),
    RECYCLE("recycle", "回收"),
    RESTORE("restore", "还原"),
    DEL("del", "删除"),
    AUDIT("audit", "审核"),;

    private final String code;

    private final String desc;
}
