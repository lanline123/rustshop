package com.tigshop.service.shop;

import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.shop.AdminUserShop;

import java.util.List;

/**
 * 店铺员工工作台候选资格策略。
 */
public interface ShopEmployeeWorkbenchCandidatePolicy {

    boolean isPosCandidate(AdminUserShop binding, AdminRole role);

    boolean isKefuCandidate(AdminUserShop binding, AdminRole role);

    List<Integer> posCandidateRoleIds();

    List<Integer> kefuCandidateRoleIds();
}
