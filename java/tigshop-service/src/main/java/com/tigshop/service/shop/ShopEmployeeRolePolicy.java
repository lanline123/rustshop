package com.tigshop.service.shop;

import com.tigshop.bean.model.authority.AdminRole;

/**
 * 店铺员工角色归属策略。
 */
public interface ShopEmployeeRolePolicy {

    /**
     * 校验角色是否可绑定到当前店铺。
     *
     * @param roleId 角色ID
     * @param currentShopId 当前店铺ID
     * @return 已校验角色
     */
    AdminRole requireAssignableRole(Integer roleId, Integer currentShopId);

    /**
     * 解析角色稳定标识。
     *
     * @param roleId 角色ID
     * @return 稳定标识；普通自定义角色为 null
     */
    String resolveRoleCode(Integer roleId);
}
