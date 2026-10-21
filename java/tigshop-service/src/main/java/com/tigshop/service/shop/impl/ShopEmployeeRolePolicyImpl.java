package com.tigshop.service.shop.impl;

import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.authority.AdminRoleMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.shop.ShopEmployeeRolePolicy;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * 店铺员工角色归属策略实现。
 */
@Service
public class ShopEmployeeRolePolicyImpl implements ShopEmployeeRolePolicy {

    private static final Set<String> EMPLOYEE_SYSTEM_ROLE_CODES = Set.of("pos_cashier", "kefu_agent");

    private final AdminRoleMapper adminRoleMapper;
    private final ShopMapper shopMapper;

    public ShopEmployeeRolePolicyImpl(AdminRoleMapper adminRoleMapper, ShopMapper shopMapper) {
        this.adminRoleMapper = adminRoleMapper;
        this.shopMapper = shopMapper;
    }

    @Override
    public AdminRole requireAssignableRole(Integer roleId, Integer currentShopId) {
        if (roleId == null || roleId <= 0) {
            throw new GlobalException("请选择员工角色");
        }
        AdminRole role = requireRole(roleId);
        if (Integer.valueOf(0).equals(currentShopId)) {
            return requireSelfOperatedRole(role);
        }
        Shop shop = shopMapper.selectById(currentShopId);
        if (shop == null) {
            throw new GlobalException("店铺不存在");
        }
        String roleCode = role.getRoleCode();
        if (roleCode != null) {
            if (!EMPLOYEE_SYSTEM_ROLE_CODES.contains(roleCode)) {
                throw new GlobalException("角色不可绑定");
            }
            if (!Objects.equals(role.getShopId(), currentShopId)
                    || !AdminTypeEnum.SHOP.getCode().equals(role.getAdminType())
                    || !Integer.valueOf(1).equals(role.getIsSystem())) {
                throw new GlobalException("角色不可绑定");
            }
            return role;
        }

        if (!Objects.equals(role.getShopId(), currentShopId)) {
            throw new GlobalException("角色不可绑定");
        }
        return role;
    }

    private AdminRole requireSelfOperatedRole(AdminRole role) {
        if (!Integer.valueOf(0).equals(role.getShopId())) {
            throw new GlobalException("角色不可绑定");
        }
        String roleCode = role.getRoleCode();
        if (roleCode != null) {
            if (!EMPLOYEE_SYSTEM_ROLE_CODES.contains(roleCode)
                    || !AdminTypeEnum.SHOP.getCode().equals(role.getAdminType())
                    || !Integer.valueOf(1).equals(role.getIsSystem())) {
                throw new GlobalException("角色不可绑定");
            }
            return role;
        }
        if (!AdminTypeEnum.ADMIN.getCode().equals(role.getAdminType())
                || !(role.getVendorId() == null || Integer.valueOf(0).equals(role.getVendorId()))) {
            throw new GlobalException("角色不可绑定");
        }
        return role;
    }

    @Override
    public String resolveRoleCode(Integer roleId) {
        if (roleId == null || roleId <= 0) {
            throw new GlobalException("请选择员工角色");
        }
        return requireRole(roleId).getRoleCode();
    }

    private AdminRole requireRole(Integer roleId) {
        AdminRole role = adminRoleMapper.selectById(roleId);
        if (role == null) {
            throw new GlobalException("角色不存在");
        }
        return role;
    }
}
