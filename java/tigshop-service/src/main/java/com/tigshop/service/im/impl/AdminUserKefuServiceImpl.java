package com.tigshop.service.im.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.im.AdminUserKefuCreateDTO;
import com.tigshop.bean.dto.im.AdminUserKefuUpdateDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.MerchantUser;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.query.im.AdminUserKefuPageQuery;
import com.tigshop.bean.vo.im.AdminUserKefuVO;
import com.tigshop.bean.vo.im.KefuRoleSimpleVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.authority.AdminRoleMapper;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.im.AdminUserKefuService;
import com.tigshop.service.merchant.MerchantUserService;
import com.tigshop.service.shop.ShopEmployeeRoleLifecycle;
import com.tigshop.service.shop.ShopEmployeeWorkbenchCandidatePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.authority.AdminUserConstants.*;
import static com.tigshop.common.constant.shop.AdminUserShopConstants.ADMIN_USER_SHOP_NOT_DELETE_SELF;
import static com.tigshop.common.constant.shop.AdminUserShopConstants.ADMIN_USER_SHOP_NOT_EXIST;
import static com.tigshop.common.constant.ResultTextConstants.INVALID_FIELD_VALUE;

/**
 * 客服人员管理：底层表为 admin_user_shop，按 kefu_agent 角色过滤，负责客服子集的 CRUD，并联动 im_servant。
 *
 * @author Tigshop团队
 */
@Service
@RequiredArgsConstructor
public class AdminUserKefuServiceImpl extends BaseServiceImpl<AdminUserShopMapper, AdminUserShop> implements AdminUserKefuService {

    /** 客服角色稳定标识 */
    private static final String KEFU_AGENT_ROLE_CODE = "kefu_agent";
    private final ShopMapper shopMapper;
    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final MerchantUserService merchantUserService;
    private final AdminLogService adminLogService;
    private final PasswordEncoder passwordEncoder;
    private final ShopEmployeeRoleLifecycle shopEmployeeRoleLifecycle;
    private final ShopEmployeeWorkbenchCandidatePolicy workbenchCandidatePolicy;

    @Override
    public Page<AdminUserKefuVO> listUser(AdminUserKefuPageQuery pageQuery, Integer currentShopId) {
        if (ObjectUtil.isNotEmpty(pageQuery.getSearchStartTime())) {
            DateTime startDate = DateUtil.parse(pageQuery.getSearchStartTime(), "yyyy-MM-dd");
            pageQuery.setStartCreateTime(DateUtil.beginOfDay(startDate).getTime() / 1000);
        }
        if (ObjectUtil.isNotEmpty(pageQuery.getSearchEndTime())) {
            DateTime endDate = DateUtil.parse(pageQuery.getSearchEndTime(), "yyyy-MM-dd");
            pageQuery.setEndCreateTime(DateUtil.endOfDay(endDate).getTime() / 1000);
        }
        List<Integer> candidateRoleIds = workbenchCandidatePolicy.kefuCandidateRoleIds();
        List<Integer> bindPhoneAdminUserIds = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(pageQuery.getKeyword())) {
            bindPhoneAdminUserIds = adminUserMapper.selectList(new LambdaQueryWrapper<AdminUser>()
                            .like(AdminUser::getUsername, pageQuery.getKeyword())
                            .or()
                            .like(AdminUser::getMobile, pageQuery.getKeyword()))
                    .stream()
                    .map(AdminUser::getAdminId)
                    .toList();
        }

        Page<AdminUserShop> page = buildSortOrder(pageQuery);
        List<Integer> finalBindPhoneAdminUserIds = bindPhoneAdminUserIds;
        Page<AdminUserShop> kefuPage = this.lambdaQuery()
                .eq(AdminUserShop::getShopId, currentShopId)
                .and(query -> query.eq(AdminUserShop::getIsAdmin, 1)
                        .or(!candidateRoleIds.isEmpty())
                        .in(!candidateRoleIds.isEmpty(), AdminUserShop::getRoleId, candidateRoleIds))
                .eq(pageQuery.getIsUsing() != null, AdminUserShop::getIsUsing, pageQuery.getIsUsing())
                .and(StrUtil.isNotBlank(pageQuery.getKeyword()), qw -> {
                    qw.like(StrUtil.isNotBlank(pageQuery.getKeyword()), AdminUserShop::getUsername, pageQuery.getKeyword());
                    qw.or();
                    qw.in(ObjectUtil.isNotEmpty(finalBindPhoneAdminUserIds), AdminUserShop::getAdminId, finalBindPhoneAdminUserIds);
                })
                .gt(ObjectUtil.isNotNull(pageQuery.getStartCreateTime()), AdminUserShop::getAddTime, pageQuery.getStartCreateTime())
                .lt(ObjectUtil.isNotNull(pageQuery.getEndCreateTime()), AdminUserShop::getAddTime, pageQuery.getEndCreateTime())
                .page(page);

        List<AdminUserShop> records = kefuPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Integer> adminIds = records.stream().map(AdminUserShop::getAdminId).toList();
        List<AdminUser> adminUsers = adminUserMapper.selectBatchIds(adminIds);
        Map<Integer, AdminUser> adminUserMap = adminUsers.stream()
                .collect(Collectors.toMap(AdminUser::getAdminId, Function.identity()));

        List<Integer> roleIdList = records.stream()
                .map(AdminUserShop::getRoleId)
                .filter(Objects::nonNull)
                .toList();
        Map<Integer, AdminRole> roleMap = roleIdList.isEmpty() ? new HashMap<>() :
                adminRoleMapper.selectBatchIds(roleIdList).stream()
                        .collect(Collectors.toMap(AdminRole::getRoleId, Function.identity()));

        List<AdminUserKefuVO> voList = records.stream()
                .map(adminUserShop -> {
                    AdminUserKefuVO vo = new AdminUserKefuVO(adminUserShop);
                    AdminUser adminUser = adminUserMap.get(adminUserShop.getAdminId());
                    AdminRole adminRole = roleMap.get(adminUserShop.getRoleId());
                    vo.assembleData(null, adminUser, adminRole);
                    return vo;
                })
                .toList();
        return PageUtil.convertPage(kefuPage, voList);
    }

    @Override
    public AdminUserKefuVO detail(Integer id, Integer currentShopId) {
        AdminUserShop adminUserShop = findCandidateBinding(id, currentShopId);
        if (adminUserShop == null) {
            return null;
        }
        AdminUser adminUser = adminUserMapper.selectById(adminUserShop.getAdminId());
        AdminUserKefuVO vo = handleRes(adminUserShop);
        vo.setUser(null);
        return getAdminUserItem(adminUser, vo);
    }

    @Override
    public AdminUserKefuVO info(Integer id, Integer shopId) {
        AdminUserShop adminUserShop = findCandidateBindingByAdminId(id, shopId);
        if (adminUserShop == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        AdminUser adminUser = adminUserMapper.selectById(adminUserShop.getAdminId());
        AdminUserKefuVO vo = handleRes(adminUserShop);
        return getAdminUserItem(adminUser, vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAdminUserKefu(AdminUserKefuCreateDTO createDTO, Integer currentShopId) {
        createDTO.setShopId(currentShopId);
        createDTO.setAdminId(null);
        AdminRole kefuRole = requireKefuAgentRole(currentShopId);
        Integer trustedMerchantId = resolveTrustedMerchantId(currentShopId);
        Integer trustedUserId = 0;
        AdminUser adminUserByMobile = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getMobile, createDTO.getMobile()));
        if (adminUserByMobile != null) {
            createDTO.setAdminId(adminUserByMobile.getAdminId());
            trustedUserId = Objects.requireNonNullElse(adminUserByMobile.getUserId(), 0);
        }

        long usernameCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, createDTO.getUsername())
                .ne(createDTO.getAdminId() != null, AdminUser::getAdminId, createDTO.getAdminId()));
        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        if (createDTO.getAvatar() == null) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            createDTO.setAvatar(StrUtil.format("../assets/avatar/{}.jpeg", randomNumber));
        }

        if (adminUserByMobile == null) {
            AdminUser adminUserSave = new AdminUser();
            BeanUtils.copyProperties(createDTO, adminUserSave);
            adminUserSave.setAdminType(Objects.equals(currentShopId, 0)
                    ? AdminTypeEnum.ADMIN.getCode()
                    : AdminTypeEnum.SHOP.getCode());
            adminUserSave.setMerchantId(trustedMerchantId);
            adminUserSave.setUserId(0);
            adminUserSave.setAddTime(StringUtils.getCurrentTime());
            if (ObjectUtil.isEmpty(adminUserSave.getPassword())) {
                adminUserSave.setPassword(passwordEncoder.encode(PASSWORD));
            }
            adminUserSave.setAuthList("[]");
            adminUserSave.setRoleId(0);
            int isSave = adminUserMapper.insert(adminUserSave);
            if (isSave > 0) {
                createDTO.setAdminId(adminUserSave.getAdminId());
                MerchantUser merchantUser = new MerchantUser();
                BeanUtils.copyProperties(createDTO, merchantUser);
                merchantUser.setMerchantId(trustedMerchantId);
                merchantUser.setUserId(0);
                merchantUser.setIsAdmin(0);
                merchantUserService.updateMerchantUser(merchantUser, adminUserSave.getAdminId());
            }
        }

        // 撞行保护：同一账号在同一门店已存在
        long hasExist = this.count(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getShopId, createDTO.getShopId())
                .eq(AdminUserShop::getAdminId, createDTO.getAdminId()));
        if (hasExist > 0) {
            throw new GlobalException("该账号在本门店已存在");
        }

        AdminUserShop adminUserShop = new AdminUserShop();
        BeanUtils.copyProperties(createDTO, adminUserShop);
        adminUserShop.setShopId(currentShopId);
        adminUserShop.setUserId(trustedUserId);
        adminUserShop.setRoleId(kefuRole.getRoleId());
        adminUserShop.setAuthList("[]");
        adminUserShop.setIsAdmin(0);
        if (createDTO.getUsername() == null) {
            AdminUser adminUserInfo = adminUserMapper.selectById(createDTO.getAdminId());
            adminUserShop.setUsername(adminUserInfo.getUsername());
        }
        adminUserShop.setAddTime(StringUtils.getCurrentTime());
        this.save(adminUserShop);

        // 联动 im_servant：确保存在一条客服在线状态记录
        shopEmployeeRoleLifecycle.ensureKefu(createDTO.getAdminId(), currentShopId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminUserKefu(AdminUserKefuUpdateDTO updateDTO, Integer currentShopId) {
        AdminUserShop adminUserShop = requireDedicatedBinding(updateDTO.getId(), currentShopId);
        updateDTO.setShopId(currentShopId);
        updateDTO.setAdminId(adminUserShop.getAdminId());

        long usernameCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, updateDTO.getUsername())
                .ne(updateDTO.getAdminId() != null, AdminUser::getAdminId, updateDTO.getAdminId()));
        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        long mobileCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getMobile, updateDTO.getMobile())
                .ne(updateDTO.getAdminId() != null, AdminUser::getAdminId, updateDTO.getAdminId()));
        if (mobileCount > 0) {
            throw new GlobalException(MOBILE_IS_EXIST);
        }

        AdminUser adminUser = adminUserMapper.selectById(adminUserShop.getAdminId());
        if (adminUser != null && !Objects.equals(updateDTO.getMobile(), adminUser.getMobile())) {
            rejectSharedAccountMobileChange(adminUserShop.getAdminId(), currentShopId);
            adminUser.setMobile(updateDTO.getMobile());
            adminUser.setEmail(updateDTO.getEmail());
            adminUserMapper.updateById(adminUser);
        }

        Integer expectedRoleId = adminUserShop.getRoleId();
        BeanUtils.copyProperties(updateDTO, adminUserShop);
        // 客服角色保持为 kefu_agent，不因更新丢失
        adminUserShop.setRoleId(expectedRoleId);
        adminUserShop.setAuthList("[]");
        this.updateById(adminUserShop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delKefu(Integer id, Integer adminId, Integer currentShopId) {
        AdminUserShop adminUserShop = requireDedicatedBinding(id, currentShopId);
        if (Objects.equals(adminId, adminUserShop.getAdminId())) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_DELETE_SELF);
        }
        adminLogService.createByLogInfo("删除客服人员信息：" + adminUserShop.getUsername());
        this.removeById(id);
        // 联动 im_servant：移除该客服在本店铺的在线状态记录
        shopEmployeeRoleLifecycle.removeKefu(adminUserShop.getAdminId(), currentShopId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO, Integer currentShopId, Integer currentAdminId) {
        if (!"del".equals(batchDTO.getType())) {
            return false;
        }
        List<AdminUserShop> bindings = batchDTO.getIds().stream()
                .map(id -> requireDedicatedBinding(id, currentShopId))
                .toList();
        if (bindings.stream().anyMatch(binding -> Objects.equals(currentAdminId, binding.getAdminId()))) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_DELETE_SELF);
        }
        boolean removed = this.removeByIds(batchDTO.getIds());
        if (removed) {
            bindings.forEach(binding -> shopEmployeeRoleLifecycle.removeKefu(binding.getAdminId(), currentShopId));
        }
        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUsing(UpdateFieldDTO updateFieldDTO, Integer currentShopId) {
        validateUsingField(updateFieldDTO.getField());
        validateUsingValue(updateFieldDTO.getVal());
        AdminUserShop binding = requireCandidateBinding(updateFieldDTO.getId(), currentShopId);
        LambdaUpdateWrapper<AdminUserShop> wrapper = new LambdaUpdateWrapper<AdminUserShop>()
                .eq(AdminUserShop::getId, updateFieldDTO.getId())
                .eq(AdminUserShop::getShopId, currentShopId)
                .set(AdminUserShop::getIsUsing, updateFieldDTO.getVal());
        if (binding.getRoleId() == null) {
            wrapper.isNull(AdminUserShop::getRoleId);
        } else {
            wrapper.eq(AdminUserShop::getRoleId, binding.getRoleId());
        }
        boolean updated = baseMapper.update(null, wrapper) > 0;
        if (!updated) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        if ("1".equals(updateFieldDTO.getVal())) {
            shopEmployeeRoleLifecycle.disableKefu(binding.getAdminId(), currentShopId);
        }
        return true;
    }

    @Override
    public List<KefuRoleSimpleVO> roles(Integer shopId) {
        AdminRole role = findKefuAgentRole(shopId);
        List<KefuRoleSimpleVO> list = new ArrayList<>();
        if (role != null) {
            list.add(new KefuRoleSimpleVO(role));
        }
        return list;
    }

    private AdminRole findKefuAgentRole(Integer shopId) {
        return adminRoleMapper.selectOne(new LambdaQueryWrapper<AdminRole>()
                .eq(AdminRole::getRoleCode, KEFU_AGENT_ROLE_CODE)
                .eq(AdminRole::getShopId, shopId)
                .eq(AdminRole::getAdminType, AdminTypeEnum.SHOP.getCode())
                .eq(AdminRole::getIsSystem, 1));
    }

    private AdminRole requireKefuAgentRole(Integer shopId) {
        AdminRole role = findKefuAgentRole(shopId);
        if (role == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        return role;
    }

    private Integer resolveTrustedMerchantId(Integer shopId) {
        if (Objects.equals(shopId, 0)) {
            return 0;
        }
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new GlobalException("店铺不存在");
        }
        return Objects.requireNonNullElse(shop.getMerchantId(), 0);
    }

    private void rejectSharedAccountMobileChange(Integer adminId, Integer currentShopId) {
        long otherShopBindings = this.count(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, adminId)
                .ne(AdminUserShop::getShopId, currentShopId));
        if (otherShopBindings > 0) {
            throw new GlobalException("该账号已绑定其他门店，不能修改手机号");
        }
    }

    private AdminUserShop findDedicatedBinding(Integer id, Integer shopId) {
        AdminRole role = findKefuAgentRole(shopId);
        if (role == null) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getId, id)
                .eq(AdminUserShop::getShopId, shopId)
                .eq(AdminUserShop::getRoleId, role.getRoleId()));
    }

    private AdminUserShop requireDedicatedBinding(Integer id, Integer shopId) {
        AdminUserShop binding = findDedicatedBinding(id, shopId);
        if (binding == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        return binding;
    }

    private AdminUserShop findCandidateBinding(Integer id, Integer shopId) {
        AdminUserShop binding = this.getOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getId, id)
                .eq(AdminUserShop::getShopId, shopId));
        return isKefuCandidate(binding) ? binding : null;
    }

    private AdminUserShop findCandidateBindingByAdminId(Integer adminId, Integer shopId) {
        AdminUserShop binding = this.getOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, adminId)
                .eq(AdminUserShop::getShopId, shopId));
        return isKefuCandidate(binding) ? binding : null;
    }

    private AdminUserShop requireCandidateBinding(Integer id, Integer shopId) {
        AdminUserShop binding = findCandidateBinding(id, shopId);
        if (binding == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        return binding;
    }

    private boolean isKefuCandidate(AdminUserShop binding) {
        if (binding == null) {
            return false;
        }
        AdminRole role = binding.getRoleId() == null || binding.getRoleId() <= 0
                ? null : adminRoleMapper.selectById(binding.getRoleId());
        return workbenchCandidatePolicy.isKefuCandidate(binding, role);
    }

    private void validateUsingField(String field) {
        if (!"isUsing".equals(field) && !"is_using".equals(field)) {
            throw new GlobalException(INVALID_FIELD_VALUE);
        }
    }

    private void validateUsingValue(String value) {
        if (!"0".equals(value) && !"1".equals(value)) {
            throw new GlobalException(INVALID_FIELD_VALUE);
        }
    }

    private AdminUserKefuVO getAdminUserItem(AdminUser adminUser, AdminUserKefuVO vo) {
        if (adminUser != null) {
            vo.setAdminUser(new AdminUserKefuVO.AdminUserVO(adminUser));
        } else {
            vo.setAdminUser(null);
        }
        return vo;
    }

    private AdminUserKefuVO handleRes(AdminUserShop adminUserShop) {
        AdminUserKefuVO vo = new AdminUserKefuVO();
        BeanUtils.copyProperties(adminUserShop, vo);
        vo.setAddTime(TigUtils.handelTime(adminUserShop.getAddTime()));
        if (adminUserShop.getRoleId() != null && adminUserShop.getRoleId() > 0) {
            AdminRole role = adminRoleMapper.selectById(adminUserShop.getRoleId());
            if (role != null && JSONUtil.isTypeJSONArray(role.getAuthorityList())) {
                vo.setAuthList(JSONUtil.parseArray(role.getAuthorityList()));
            } else {
                vo.setAuthList(new JSONArray());
            }
        } else if (adminUserShop.getAuthList() != null && !"[]".equals(adminUserShop.getAuthList())) {
            vo.setAuthList(JsonUtil.fromJson(adminUserShop.getAuthList(), JSONArray.class));
        } else {
            vo.setAuthList(new JSONArray());
        }
        return vo;
    }
}
