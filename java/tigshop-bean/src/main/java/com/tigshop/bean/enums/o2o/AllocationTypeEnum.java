package com.tigshop.bean.enums.o2o;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Tigshop团队
 */
@Getter
@AllArgsConstructor
public enum AllocationTypeEnum {
    // 全部分配
    ALL,
    // 区域分配
    AREA,
    // 指定门店分配
    SHOPS
     ;
}