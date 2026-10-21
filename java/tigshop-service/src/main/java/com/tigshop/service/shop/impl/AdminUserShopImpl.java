// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.shop.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.shop.AdminUserShopCreateDTO;
import com.tigshop.bean.query.shop.AdminUserShopPageQuery;
import com.tigshop.bean.dto.shop.AdminUserShopModifyDTO;
import com.tigshop.bean.dto.shop.AdminUserShopUpdateDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.MerchantUser;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.shop.AdminUserShopVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.authority.AdminRoleMapper;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.merchant.MerchantUserService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.AdminUserShopService;
import com.tigshop.service.shop.ShopEmployeeRoleLifecycle;
import com.tigshop.service.shop.ShopEmployeeRolePolicy;
import com.tigshop.service.shop.ShopEmployeeWorkbenchCandidatePolicy;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.authority.AdminUserConstants.*;
import static com.tigshop.common.constant.shop.AdminUserShopConstants.ADMIN_USER_SHOP_ALREADY_EXIST;
import static com.tigshop.common.constant.shop.AdminUserShopConstants.ADMIN_USER_SHOP_NOT_DELETE_SELF;
import static com.tigshop.common.constant.shop.AdminUserShopConstants.ADMIN_USER_SHOP_NOT_EXIST;

/**
 * 店铺表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class AdminUserShopImpl extends BaseServiceImpl<AdminUserShopMapper, AdminUserShop> implements AdminUserShopService {

    @Resource
    private ConfigService configService;
    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AdminRoleMapper adminRoleMapper;
    @Resource
    private ShopMapper shopMapper;
    @Resource
    private MerchantUserService merchantUserService;
    @Resource
    private AdminLogService adminLogService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Resource
    private ShopEmployeeRolePolicy rolePolicy;
    @Resource
    private ShopEmployeeRoleLifecycle roleLifecycle;
    @Resource
    private ShopEmployeeWorkbenchCandidatePolicy workbenchCandidatePolicy;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAdminUserShop(AdminUserShopCreateDTO createDTO) {
        Integer trustedInternalAdminId = Integer.valueOf(1).equals(createDTO.getIsAdmin())
                && StrUtil.isBlank(createDTO.getMobile())
                && createDTO.getAdminId() != null && createDTO.getAdminId() > 0
                ? createDTO.getAdminId() : null;
        // 普通员工请求中的 adminId 不可信，必须在任何查询或校验前丢弃。
        createDTO.setAdminId(null);
        AdminRole assignableRole = rolePolicy.requireAssignableRole(createDTO.getRoleId(), createDTO.getShopId());
        Integer trustedMerchantId = resolveTrustedMerchantId(createDTO.getShopId());
        createDTO.setMerchantId(trustedMerchantId);
        createDTO.setUserId(0);
        if (trustedInternalAdminId != null) {
            AdminUser internalAdmin = adminUserMapper.selectById(trustedInternalAdminId);
            if (internalAdmin == null) {
                throw new GlobalException(USER_NOT_EXIST);
            }
            createDTO.setMobile(internalAdmin.getMobile());
            if (StrUtil.isBlank(createDTO.getUsername())) {
                createDTO.setUsername(internalAdmin.getUsername());
            }
        }
        // 判断用户名是否重复
        long usernameCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, createDTO.getUsername())
                .ne(AdminUser::getMobile, createDTO.getMobile())
        );

        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        if (createDTO.getShopId() > 0) {
            List<Integer> cashierRoleIds = posCashierRoleIds();
            long count = this.count(new LambdaQueryWrapper<AdminUserShop>()
                    .eq(AdminUserShop::getShopId, createDTO.getShopId())
                    .eq(AdminUserShop::getIsAdmin, 0)
                    .notIn(CollUtil.isNotEmpty(cashierRoleIds), AdminUserShop::getRoleId, cashierRoleIds));
            ConfigPO maxSubAdministratorConfig = configService.getConfigByCode(SettingsEnum.MAX_SUB_ADMINISTRATOR.getBizCode());
            long maxAdminUserShopCount = StrUtil.isNotBlank(maxSubAdministratorConfig.getBizVal()) ? Long.parseLong(maxSubAdministratorConfig.getBizVal()) : 0L;
            if (count >= maxAdminUserShopCount) {
                throw new GlobalException("店铺员工数已达上限,上限为" + maxAdminUserShopCount + "个,如需修改,请前往配置");
            }
        }

        if (createDTO.getAvatar() == null) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            createDTO.setAvatar(format);
        }

        // adminId 不接受客户端选择；统一由服务端按手机号解析账号。
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUser::getMobile, createDTO.getMobile());
        AdminUser adminUser = adminUserMapper.selectOne(queryWrapper);
        if (adminUser == null) {
            AdminUser adminUserSave = new AdminUser();
            BeanUtils.copyProperties(createDTO, adminUserSave);
            adminUserSave.setAdminType(resolveAdminTypeByShopId(createDTO.getShopId()));
            adminUserSave.setAddTime(StringUtils.getCurrentTime());
            if (ObjectUtil.isEmpty(adminUserSave.getPassword())) {
                adminUserSave.setPassword(passwordEncoder.encode(PASSWORD));
            }
            adminUserSave.setMerchantId(trustedMerchantId);
            adminUserSave.setUserId(0);
            adminUserSave.setRoleId(0);
            adminUserSave.setAuthList("[]");
            int isSave = adminUserMapper.insert(adminUserSave);
            if (isSave > 0) {
                createDTO.setAdminId(adminUserSave.getAdminId());
                MerchantUser merchantUser = new MerchantUser();
                BeanUtils.copyProperties(createDTO, merchantUser);
                merchantUser.setMerchantId(trustedMerchantId);
                merchantUser.setUserId(0);
                merchantUserService.updateMerchantUser(merchantUser, adminUserSave.getAdminId());
            }
        } else {
            createDTO.setAdminId(adminUser.getAdminId());
            createDTO.setUserId(adminUser.getUserId());
        }

        long hasExist = this.count(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getShopId, createDTO.getShopId())
                .eq(AdminUserShop::getAdminId, createDTO.getAdminId()));
        if (hasExist > 0) {
            throw new GlobalException(ADMIN_USER_SHOP_ALREADY_EXIST);
        }

        AdminUserShop adminUsershop = new AdminUserShop();
        BeanUtils.copyProperties(createDTO, adminUsershop);
        adminUsershop.setUserId(createDTO.getUserId());
        // RBAC: 只存 role_id，不再复制权限到 auth_list
        adminUsershop.setRoleId(assignableRole.getRoleId());
        adminUsershop.setAuthList("[]");
        //获取用户名
        if (createDTO.getUsername() == null) {
            AdminUser adminUserInfo = adminUserMapper.selectById(createDTO.getAdminId());
            adminUsershop.setUsername(adminUserInfo.getUsername());
        }
        adminUsershop.setAddTime(StringUtils.getCurrentTime());
        this.save(adminUsershop);
        if (workbenchCandidatePolicy.isKefuCandidate(adminUsershop, assignableRole)) {
            roleLifecycle.ensureKefu(adminUsershop.getAdminId(), adminUsershop.getShopId());
        }
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminUserShop(AdminUserShopUpdateDTO updateDTO, Integer currentShopId) {
        AdminUserShop adminUserShop = requireCurrentShopBinding(updateDTO.getId(), currentShopId);
        AdminRole assignableRole = rolePolicy.requireAssignableRole(updateDTO.getRoleId(), currentShopId);
        AdminRole oldRole = findExistingRole(adminUserShop.getRoleId());
        boolean wasKefu = workbenchCandidatePolicy.isKefuCandidate(adminUserShop, oldRole);
        Integer trustedAdminId = adminUserShop.getAdminId();
        Integer trustedShopId = adminUserShop.getShopId();
        Integer trustedIsAdmin = adminUserShop.getIsAdmin();

        // 判断用户名是否重复
        long usernameCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, updateDTO.getUsername())
                .ne(AdminUser::getAdminId, trustedAdminId)
        );

        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        long mobileCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getMobile, updateDTO.getMobile())
                .ne(AdminUser::getAdminId, trustedAdminId)
        );

        if (mobileCount > 0) {
            throw new GlobalException(MOBILE_IS_EXIST);
        }
        AdminUser adminUser = adminUserMapper.selectById(trustedAdminId);
        if (adminUser == null) {
            throw new GlobalException(USER_NOT_EXIST);
        }
        if (!Objects.equals(updateDTO.getMobile(), adminUser.getMobile())) {
            long otherShopBindingCount = this.count(new LambdaQueryWrapper<AdminUserShop>()
                    .eq(AdminUserShop::getAdminId, trustedAdminId)
                    .ne(AdminUserShop::getShopId, trustedShopId));
            if (otherShopBindingCount > 0) {
                throw new GlobalException("该账号已绑定其他店铺，不能修改登录手机号");
            }
            AdminUser adminUserUpdate = new AdminUser();
            adminUserUpdate.setAdminId(trustedAdminId);
            adminUserUpdate.setMobile(updateDTO.getMobile());
            adminUserUpdate.setEmail(updateDTO.getEmail());
            adminUserMapper.updateById(adminUserUpdate);
        }

        if (updateDTO.getAvatar() == null) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            updateDTO.setAvatar(format);
        }

        // 更新 admin_user_shop
        BeanUtils.copyProperties(updateDTO, adminUserShop);
        adminUserShop.setId(updateDTO.getId());
        adminUserShop.setAdminId(trustedAdminId);
        adminUserShop.setShopId(trustedShopId);
        adminUserShop.setIsAdmin(trustedIsAdmin);
        adminUserShop.setRoleId(assignableRole.getRoleId());
        adminUserShop.setAuthList("[]");
        this.updateById(adminUserShop);

        boolean isKefu = workbenchCandidatePolicy.isKefuCandidate(adminUserShop, assignableRole);
        if (!wasKefu && isKefu) {
            roleLifecycle.ensureKefu(trustedAdminId, trustedShopId);
        } else if (wasKefu && !isKefu) {
            roleLifecycle.removeKefu(trustedAdminId, trustedShopId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delShop(Integer id, Integer adminId, Integer currentShopId) {
        AdminUserShop adminUserShop = requireCurrentShopBinding(id, currentShopId);
        if (Objects.equals(adminId, adminUserShop.getAdminId())) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_DELETE_SELF);
        }
        boolean wasKefu = isKefuCandidate(adminUserShop);
        adminLogService.createByLogInfo("删除员工信息：" + adminUserShop.getUsername());
        this.removeById(id);
        if (wasKefu) {
            roleLifecycle.removeKefu(adminUserShop.getAdminId(), adminUserShop.getShopId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO, Integer currentShopId) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        if ("del".equals(batchDTO.getType())) {
            List<AdminUserShop> bindings = batchDTO.getIds().stream()
                    .map(id -> requireCurrentShopBinding(id, currentShopId))
                    .toList();
            Map<Integer, Boolean> kefuCandidates = new HashMap<>();
            bindings.forEach(binding -> {
                if (Objects.equals(adminId, binding.getAdminId())) {
                    throw new GlobalException(ADMIN_USER_SHOP_NOT_DELETE_SELF);
                }
                kefuCandidates.put(binding.getId(), isKefuCandidate(binding));
            });
            bindings.forEach(binding -> {
                adminLogService.createByLogInfo("删除员工信息：" + binding.getUsername());
                this.removeById(binding.getId());
                if (Boolean.TRUE.equals(kefuCandidates.get(binding.getId()))) {
                    roleLifecycle.removeKefu(binding.getAdminId(), binding.getShopId());
                }
            });
        }
        return false;
    }

    @Override
    public void modifyUser(AdminUserShopModifyDTO modifyDTO, Integer currentShopId) {
        AdminUserShop adminUserShop = requireCurrentShopBinding(modifyDTO.getId(), currentShopId);
        if (ObjectUtil.isEmpty(modifyDTO.getAvatar())) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            modifyDTO.setAvatar(format);
        }
        adminUserShop.setAvatar(modifyDTO.getAvatar());
        adminUserShop.setEmail(modifyDTO.getEmail());
        this.updateById(adminUserShop);
    }

    @Override
    public boolean hasShop(Integer userId) {
        LambdaQueryWrapper<AdminUserShop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUserShop::getUserId, userId);
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper) != null;
    }

    @Override
    public Page<AdminUserShopVO> list(AdminUserShopPageQuery pageQuery, Integer shopId) {
        if (shopId == null) {
            return new Page<>();
        }
        Page<AdminUserShop> page = buildSortOrder(pageQuery);
        Page<AdminUserShop> shopPage = this.lambdaQuery()
                .eq(AdminUserShop::getShopId, shopId)
                .like(StrUtil.isNotBlank(pageQuery.getKeyword()), AdminUserShop::getUsername, pageQuery.getKeyword())
                .page(page);

        List<AdminUserShop> adminShopRecords = shopPage.getRecords();
        if (CollUtil.isEmpty(adminShopRecords)) {
            return new Page<>();
        }
        List<Integer> userIds = adminShopRecords.stream().map(AdminUserShop::getUserId).toList();


        // 查出用户信息
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 查出admin_user信息
        List<Integer> adminIds = adminShopRecords.stream().map(AdminUserShop::getAdminId).toList();
        List<AdminUser> adminUsers = adminUserMapper.selectBatchIds(adminIds);
        Map<Integer, AdminUser> adminUserMap = adminUsers.stream().collect(Collectors.toMap(AdminUser::getAdminId, Function.identity()));

        // 查出角色
        List<Integer> roleIds = adminShopRecords.stream().map(AdminUserShop::getRoleId).toList();
        List<AdminRole> roles = adminRoleMapper.selectBatchIds(roleIds);
        Map<Integer, AdminRole> roleMap = roles.stream().collect(Collectors.toMap(AdminRole::getRoleId, Function.identity()));

        // 转换为VO
        List<AdminUserShopVO> adminUserShopVOList = adminShopRecords.stream()
                .map(adminUserShop -> {
                    AdminUserShopVO adminUserShopVO = new AdminUserShopVO(adminUserShop);

                    User user = userMap.get(adminUserShop.getUserId());
                    AdminUser adminUser = adminUserMap.get(adminUserShop.getAdminId());
                    AdminRole adminRole = roleMap.get(adminUserShop.getRoleId());
                    adminUserShopVO.assembleData(user, adminUser, adminRole);

                    return adminUserShopVO;
                })
                .toList();
        return PageUtil.convertPage(shopPage, adminUserShopVOList);
    }

    @Override
    public AdminUserShopVO detail(Integer id, Integer currentShopId) {
        AdminUserShop adminUserShop = requireCurrentShopBinding(id, currentShopId);
        User user = userMapper.selectById(adminUserShop.getUserId());
        AdminUser adminUser = adminUserMapper.selectById(adminUserShop.getAdminId());
        AdminUserShopVO adminUserShopVO = handleRes(adminUserShop);
        if (user != null) {
            AdminUserShopVO.UserVO userVO = new AdminUserShopVO.UserVO();
            BeanUtils.copyProperties(user, userVO);
            adminUserShopVO.setUser(userVO);
        } else {
            adminUserShopVO.setUser(null);
        }
        return getAdminUserShopItem(adminUser, adminUserShopVO);
    }

    @Override
    public AdminUserShopVO info(Integer id, Integer shopId) {
        LambdaQueryWrapper<AdminUserShop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUserShop::getAdminId, id)
                .eq(AdminUserShop::getShopId, shopId);
        AdminUserShop adminUserShop = this.getOne(queryWrapper);
        if (adminUserShop == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        AdminUser adminUser = adminUserMapper.selectById(adminUserShop.getAdminId());
        AdminUserShopVO adminUserShopVO = handleRes(adminUserShop);
        return getAdminUserShopItem(adminUser, adminUserShopVO);
    }

    /**
     * 获取adminUserShopVOItemVO
     *
     * @param adminUser       adminUser
     * @param adminUserShopVO 店铺员工管理VO
     * @return AdminUserShopVO
     */
    private AdminUserShopVO getAdminUserShopItem(AdminUser adminUser, AdminUserShopVO adminUserShopVO) {
        if (adminUser != null) {
            AdminUserShopVO.AdminUserVO adminUserVO = new AdminUserShopVO.AdminUserVO();
            BeanUtils.copyProperties(adminUser, adminUserVO);
            adminUserShopVO.setAdminUser(adminUserVO);
        } else {
            adminUserShopVO.setAdminUser(null);
        }
        return adminUserShopVO;
    }

    /**
     * 处理返回结果
     *
     * @param adminUserShop 店铺员工管理
     * @return AdminUserShopVO
     */
    private AdminUserShopVO handleRes(AdminUserShop adminUserShop) {
        AdminUserShopVO adminUserShopVO = new AdminUserShopVO();
        BeanUtils.copyProperties(adminUserShop, adminUserShopVO);
        adminUserShopVO.setAddTime(TigUtils.handelTime(adminUserShop.getAddTime()));
        // RBAC: 有角色时从角色动态解析权限列表
        if (adminUserShop.getRoleId() != null && adminUserShop.getRoleId() > 0) {
            AdminRole role = adminRoleMapper.selectById(adminUserShop.getRoleId());
            if (role != null) {
                adminUserShopVO.setAuthList(JSONUtil.parseArray(role.getAuthorityList()));
            } else {
                adminUserShopVO.setAuthList(new JSONArray());
            }
        } else if (adminUserShop.getAuthList() != null && !"[]".equals(adminUserShop.getAuthList())) {
            adminUserShopVO.setAuthList(JsonUtil.fromJson(adminUserShop.getAuthList(), JSONArray.class));
        } else {
            adminUserShopVO.setAuthList(new JSONArray());
        }
        return adminUserShopVO;
    }

    @Override
    public List<Integer> getShopIds(Integer adminId) {
        // 仅返回启用中的绑定（is_using=0）：被店铺禁用的员工绑定不应出现在可登录组织列表中
        List<Integer> shopIds = this.list(new LambdaQueryWrapper<AdminUserShop>()
                        .eq(AdminUserShop::getAdminId, adminId)
                        .eq(AdminUserShop::getIsUsing, 0)
                        .select(AdminUserShop::getShopId))
                .stream()
                .map(AdminUserShop::getShopId)
                .toList();
        if (shopIds.isEmpty()) {
            return new ArrayList<>();
        }
        return shopIds;

    }

    @Override
    public void setPosLoginDefault(Integer shopId, Integer defaultPosLogin) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        AdminUserShop adminUserShop = this.getOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, adminId)
                .eq(AdminUserShop::getShopId, shopId)
                .last("limit 1"));
        if (adminUserShop == null) {
            throw new GlobalException("当前账号不属于该门店，无法设置默认进收银台");
        }
        adminUserShop.setDefaultPosLogin(defaultPosLogin != null && defaultPosLogin == 1 ? 1 : 0);
        this.updateById(adminUserShop);
    }

    @Override
    public void recordLoginSystem(Integer shopId, String lastLoginSystem) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        AdminUserShop adminUserShop = this.getOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, adminId)
                .eq(AdminUserShop::getShopId, shopId)
                .last("limit 1"));
        if (adminUserShop == null) {
            return;
        }
        adminUserShop.setLastLoginSystem(lastLoginSystem == null ? "" : lastLoginSystem);
        this.updateById(adminUserShop);
    }

    @Override
    public Integer getDefaultPosShopId(Integer adminId) {
        AdminUserShop adminUserShop = this.getOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, adminId)
                .eq(AdminUserShop::getDefaultPosLogin, 1)
                .eq(AdminUserShop::getIsUsing, 0)
                .last("limit 1"));
        return adminUserShop == null ? null : adminUserShop.getShopId();
    }

    @Override
    public String getLastLoginSystem(Integer adminId) {
        AdminUserShop adminUserShop = this.getOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, adminId)
                .ne(AdminUserShop::getLastLoginSystem, "")
                .orderByDesc(AdminUserShop::getAddTime)
                .last("limit 1"));
        return adminUserShop == null || adminUserShop.getLastLoginSystem() == null ? "" : adminUserShop.getLastLoginSystem();
    }

    private AdminUserShop requireCurrentShopBinding(Integer id, Integer currentShopId) {
        if (id == null || currentShopId == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        AdminUserShop binding = this.getOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getId, id)
                .eq(AdminUserShop::getShopId, currentShopId));
        if (binding == null) {
            throw new GlobalException(ADMIN_USER_SHOP_NOT_EXIST);
        }
        return binding;
    }

    private AdminRole findExistingRole(Integer roleId) {
        return roleId == null || roleId <= 0 ? null : adminRoleMapper.selectById(roleId);
    }

    private boolean isKefuCandidate(AdminUserShop binding) {
        return workbenchCandidatePolicy.isKefuCandidate(binding, findExistingRole(binding.getRoleId()));
    }

    private List<Integer> posCashierRoleIds() {
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
                        .eq(AdminRole::getRoleCode, "pos_cashier")
                        .select(AdminRole::getRoleId))
                .stream()
                .map(AdminRole::getRoleId)
                .toList();
    }

    private String resolveAdminTypeByShopId(Integer shopId) {
        if (shopId == null || shopId <= 0) {
            return AdminTypeEnum.SHOP.getCode();
        }
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            return AdminTypeEnum.SHOP.getCode();
        }
        return AdminTypeEnum.resolveCodeByShopType(shop.getShopType());
    }

}
