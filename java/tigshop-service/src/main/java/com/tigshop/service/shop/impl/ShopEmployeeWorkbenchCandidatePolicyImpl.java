package com.tigshop.service.shop.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.mapper.authority.AdminRoleMapper;
import com.tigshop.service.shop.ShopEmployeeWorkbenchCandidatePolicy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Vortmall 工作台候选语义在 Tigshop 店铺员工模型上的统一实现。
 */
@Service
public class ShopEmployeeWorkbenchCandidatePolicyImpl implements ShopEmployeeWorkbenchCandidatePolicy {

    public static final String POS_CASHIER_ROLE_CODE = "pos_cashier";
    public static final String KEFU_AGENT_ROLE_CODE = "kefu_agent";
    public static final String POS_WORKBENCH_AUTH_SN = "posWorkbench";
    public static final String KEFU_WORKBENCH_AUTH_SN = "kefuWorkbench";

    private final AdminRoleMapper adminRoleMapper;

    public ShopEmployeeWorkbenchCandidatePolicyImpl(AdminRoleMapper adminRoleMapper) {
        this.adminRoleMapper = adminRoleMapper;
    }

    @Override
    public boolean isPosCandidate(AdminUserShop binding, AdminRole role) {
        if (binding == null || role == null || role.getRoleId() == null
                || !Objects.equals(binding.getRoleId(), role.getRoleId())
                || binding.getShopId() == null || !Objects.equals(binding.getShopId(), role.getShopId())
                || !List.of("admin", "shop", "store", "pickup", "pos").contains(Objects.toString(role.getAdminType(), ""))
                || (role.getVendorId() != null && role.getVendorId() != 0)
                || ("admin".equals(role.getAdminType()) && role.getShopId() != 0)) {
            return false;
        }
        // all 免勾选仅适用于同主体的平台、门店内置管理员。
        return containsExactAuthority(role.getAuthorityList(), POS_WORKBENCH_AUTH_SN)
                || (Integer.valueOf(1).equals(role.getIsSystem())
                && (POS_CASHIER_ROLE_CODE.equals(role.getRoleCode())
                || (List.of("admin", "store").contains(role.getAdminType())
                && containsExactAuthority(role.getAuthorityList(), "all"))));
    }

    @Override
    public boolean isKefuCandidate(AdminUserShop binding, AdminRole role) {
        return isCandidate(binding, role, KEFU_AGENT_ROLE_CODE, KEFU_WORKBENCH_AUTH_SN);
    }

    @Override
    public List<Integer> posCandidateRoleIds() {
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
                        .in(AdminRole::getAdminType, List.of("admin", "shop", "store", "pickup", "pos"))
                        .apply("COALESCE(vendor_id, 0) = 0")
                        .and(query -> query.ne(AdminRole::getAdminType, "admin").or().eq(AdminRole::getShopId, 0))
                        .and(query -> query
                                .apply("JSON_CONTAINS(CASE WHEN JSON_VALID(authority_list) THEN authority_list ELSE '[]' END, {0})", "[\"posWorkbench\"]")
                                .or(system -> system.eq(AdminRole::getIsSystem, 1)
                                        .and(auth -> auth.eq(AdminRole::getRoleCode, POS_CASHIER_ROLE_CODE)
                                                .or(admin -> admin.in(AdminRole::getAdminType, List.of("admin", "store"))
                                                        .apply("JSON_CONTAINS(CASE WHEN JSON_VALID(authority_list) THEN authority_list ELSE '[]' END, {0})", "[\"all\"]")))))
                        .select(AdminRole::getRoleId))
                .stream().map(AdminRole::getRoleId).toList();
    }

    @Override
    public List<Integer> kefuCandidateRoleIds() {
        return candidateRoleIds(KEFU_AGENT_ROLE_CODE, KEFU_WORKBENCH_AUTH_SN);
    }

    private boolean isCandidate(AdminUserShop binding, AdminRole role, String roleCode, String authoritySn) {
        if (binding == null) {
            return false;
        }
        if (Integer.valueOf(1).equals(binding.getIsAdmin())) {
            return true;
        }
        return role != null
                && (roleCode.equals(role.getRoleCode()) || containsExactAuthority(role.getAuthorityList(), authoritySn));
    }

    private List<Integer> candidateRoleIds(String roleCode, String authoritySn) {
        String authorityJson = "\"" + authoritySn + "\"";
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
                        .and(query -> query.eq(AdminRole::getRoleCode, roleCode)
                                .or()
                                .apply("JSON_VALID(authority_list) AND JSON_CONTAINS(authority_list, {0})", authorityJson))
                        .select(AdminRole::getRoleId))
                .stream()
                .map(AdminRole::getRoleId)
                .toList();
    }

    private boolean containsExactAuthority(String authorityList, String authoritySn) {
        if (!JSONUtil.isTypeJSONArray(authorityList)) {
            return false;
        }
        try {
            JSONArray authorities = JSONUtil.parseArray(authorityList);
            return authorities.stream().anyMatch(authoritySn::equals);
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
