package com.tigshop.service.shop;

/**
 * 店铺员工特殊角色生命周期。
 */
public interface ShopEmployeeRoleLifecycle {

    void ensureKefu(Integer adminId, Integer shopId);

    void removeKefu(Integer adminId, Integer shopId);

    void disableKefu(Integer adminId, Integer shopId);
}
