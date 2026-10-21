// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.authority.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.tigshop.bean.dto.authority.AdminUserDTO;
import com.tigshop.bean.dto.authority.AdminUserModifyManageAccountsDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.login.RegisterSmsDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.query.authority.adminuser.AdminUserPageQuery;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.shop.ShopEmployeeRoleLifecycle;
import com.tigshop.service.shop.ShopEmployeeWorkbenchCandidatePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.ADMIN_USER;
import static com.tigshop.common.constant.authority.AdminUserConstants.*;
import static com.tigshop.common.constant.login.LoginConstants.*;

/**
 * 后台用户
 *
 * @author Jayce
 * @create 2024-09-27 10:34:44
 */
@RequiredArgsConstructor
@Service
public class AdminUserServiceImpl extends BaseServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    @Resource
    private RedisTemplate<String, AdminUser> redisTemplate;

    private final RedisCache redisCache;

    private final StringRedisTemplate stringRedisTemplate;

    private final PasswordEncoder passwordEncoder;

    private final AdminRoleService adminRoleService;

    private final ConfigService configService;


    private final ShopMapper shopMapper;

    private final MerchantMapper merchantMapper;

    private final AdminUserShopMapper adminUserShopMapper;

    private final ShopEmployeeWorkbenchCandidatePolicy workbenchCandidatePolicy;

    private final ShopEmployeeRoleLifecycle roleLifecycle;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(AdminUserDTO adminUserDTO) {
        // 检查新添加的用户名是否已被其他未删除的用户使用
        long usernameCount = this.count(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, adminUserDTO.getUsername())
                .and(wrapper -> wrapper.isNull(AdminUser::getIsDeleted).or().eq(AdminUser::getIsDeleted, 0)));
        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        // 查询是否存在已软删除的同手机号用户
        AdminUser deletedUser = this.getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getMobile, adminUserDTO.getMobile())
                .eq(AdminUser::getIsDeleted, 1));
        if (deletedUser != null) {
            // 复用已软删除的账号，更新相关信息
            deletedUser.setIsDeleted(0); // 恢复账号
            deletedUser.setUsername(adminUserDTO.getUsername());
            deletedUser.setAdminType(AdminTypeEnum.ADMIN.getCode());
            deletedUser.setRoleId(Optional.ofNullable(adminUserDTO.getRoleId()).orElse(0));
            deletedUser.setEmail(adminUserDTO.getEmail());
            // 设置头像
            if (ObjectUtil.isEmpty(adminUserDTO.getAvatar())) {
                int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
                deletedUser.setAvatar(StrUtil.format("../assets/avatar/{}.jpeg", randomNumber));
            } else {
                deletedUser.setAvatar(adminUserDTO.getAvatar());
            }
            // 设置密码
            if (StrUtil.isNotEmpty(adminUserDTO.getPassword())) {
                deletedUser.setPassword(passwordEncoder.encode(adminUserDTO.getPassword()));
            } else if (StrUtil.isNotEmpty(adminUserDTO.getInitialPassword())) {
                deletedUser.setPassword(passwordEncoder.encode(adminUserDTO.getInitialPassword()));
            }
            // RBAC: 只存 role_id，不再复制权限到 auth_list
            deletedUser.setRoleId(Optional.ofNullable(adminUserDTO.getRoleId()).orElse(0));
            deletedUser.setAuthList("[]");
            deletedUser.setAddTime(StringUtils.getCurrentTime());
            boolean updated = this.updateById(deletedUser);
            if (updated) {
                syncPlatformWorkbenchBinding(deletedUser);
            }
            return updated;
        }

        // 如果手机号原来的账号不为管理员类型，则将其改为管理员类型，并保留原来的店铺和供应商
        AdminUser adminUser = this.getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getMobile, adminUserDTO.getMobile())
                .and(wrapper -> wrapper.isNull(AdminUser::getIsDeleted).or().eq(AdminUser::getIsDeleted, 0)));
        if (adminUser != null) {
            if (AdminTypeEnum.fromCode(adminUser.getAdminType()) == AdminTypeEnum.ADMIN) {
                throw new GlobalException(MOBILE_IS_EXIST);
            } else {
                adminUser.setAdminType(AdminTypeEnum.ADMIN.getCode());
                // RBAC: 只存 role_id，不再复制权限到 auth_list
                adminUser.setRoleId(Optional.ofNullable(adminUserDTO.getRoleId()).orElse(0));
                adminUser.setAuthList("[]");
                boolean updated = this.updateById(adminUser);
                if (updated) {
                    syncPlatformWorkbenchBinding(adminUser);
                }
                return updated;
            }
        }

        AdminUser adminUserSave = buildAdminUser(adminUserDTO);
        boolean saved = this.save(adminUserSave);
        if (saved) {
            syncPlatformWorkbenchBinding(adminUserSave);
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUser providerCreate(AdminUserDTO adminUserDTO) {
        AdminUser adminUser = buildAdminUser(adminUserDTO);
        boolean save = this.save(adminUser);
        if (!save) {
            return null;
        }
        return adminUser;
    }

    @Override
    public boolean modifyManageAccounts(AdminUserModifyManageAccountsDTO adminUserModifyManageAccountsDTO) {
        Integer currentAdminId = SecurityUtils.getCurrentAdminId();
        AdminUser adminUser = this.getById(currentAdminId);

        AdminUser adminUserUpdate = new AdminUser();
        adminUserUpdate.setAdminId(currentAdminId);
        switch (adminUserModifyManageAccountsDTO.getModifyType()) {
            case 1 -> {
                //修改个人信息
                adminUserUpdate.setAvatar(adminUserModifyManageAccountsDTO.getAvatar());
                adminUserUpdate.setEmail(adminUserModifyManageAccountsDTO.getEmail());
            }
            case 2 -> {
                //修改密码
                if (adminUserModifyManageAccountsDTO.getOldPassword() != null) {
                    if (!passwordEncoder.matches(adminUserModifyManageAccountsDTO.getOldPassword(), adminUser.getPassword())) {
                        throw new GlobalException(OLD_PASSWORD_ERROR);
                    }
                }
                if (adminUserModifyManageAccountsDTO.getPassword() != null) {
                    adminUserUpdate.setPassword(passwordEncoder.encode(adminUserModifyManageAccountsDTO.getPassword()));
                    // 清空 redis 中 admin_user 信息
                    redisTemplate.delete(StrUtil.format("{}{}", ADMIN_USER, adminUser.getUsername()));
                    // 删除该用户所有token，使用模糊匹配
                    Set<String> keys = redisTemplate.keys(Constants.getAllLoginTokenFormatByUserName(adminUser.getUsername()));
                    if (keys != null && !keys.isEmpty()) {
                        redisTemplate.delete(keys);
                    }
                }
                if (!Objects.equals(adminUserModifyManageAccountsDTO.getPassword(), adminUserModifyManageAccountsDTO.getPwdConfirm())) {
                    throw new GlobalException(PASSWORD_NOT_CONSISTENT);
                }
                redisCache.deleteObject(Constants.PASSWORD_TOO_SIMPLE + currentAdminId);
            }
            case 3 -> {
                // 修改手机号
                if (adminUser != null && Objects.equals(adminUser.getMobile(), adminUserModifyManageAccountsDTO.getMobile())) {
                    throw new GlobalException("新手机号不能与当前手机号相同");
                }
                if (adminUser != null && StrUtil.isNotEmpty(adminUser.getMobile())
                        && !"1".equals(stringRedisTemplate.opsForValue().get(verifiedMobileKey(currentAdminId)))) {
                    throw new GlobalException("请先验证当前绑定的手机号");
                }
                if (StrUtil.isEmpty(adminUserModifyManageAccountsDTO.getCode())) {
                    throw new GlobalException(USER_NOT_CODE);
                }
                if (this.count(new LambdaQueryWrapper<AdminUser>()
                        .ne(AdminUser::getAdminId, currentAdminId)
                        .eq(AdminUser::getMobile, adminUserModifyManageAccountsDTO.getMobile())) > 0) {
                    // 判断手机号是否被其他人使用
                    throw new GlobalException(MOBILE_USED);
                }
                adminUserUpdate.setMobile(adminUserModifyManageAccountsDTO.getMobile());
                String mobileCode = adminUserModifyManageAccountsDTO.getCode();
                String redisCode = stringRedisTemplate.opsForValue().get("login" + adminUserModifyManageAccountsDTO.getMobile());
                if (!mobileCode.equals(redisCode)) {
                    throw new GlobalException("短信验证码错误或已过期，请重试");
                }
                stringRedisTemplate.delete("login" + adminUserModifyManageAccountsDTO.getMobile());
                stringRedisTemplate.delete(verifiedMobileKey(currentAdminId));
            }
            case 4 -> {
                if (adminUser == null || StrUtil.isEmpty(adminUser.getMobile())) {
                    throw new GlobalException("当前账号未绑定手机号");
                }
                String codeKey = "login" + adminUser.getMobile();
                String redisCode = stringRedisTemplate.opsForValue().get(codeKey);
                if (StrUtil.isEmpty(adminUserModifyManageAccountsDTO.getCode())
                        || !adminUserModifyManageAccountsDTO.getCode().equals(redisCode)) {
                    throw new GlobalException("短信验证码错误或已过期，请重试");
                }
                stringRedisTemplate.opsForValue().set(verifiedMobileKey(currentAdminId), "1", 10, TimeUnit.MINUTES);
                stringRedisTemplate.delete(codeKey);
                return true;
            }
        }
        return updateById(adminUserUpdate);
    }

    @Override
    public Integer getMerchantId(Integer adminId) {
        AdminUser adminUser = this.getById(adminId);

        if (adminUser != null) {
            return adminUser.getMerchantId();
        } else {
            throw new GlobalException("未登录");
        }
    }

    @Override
    public List<AdminRole> getConfig(String adminType) {
        AdminTypeEnum adminTypeEnum = AdminTypeEnum.fromCode(getAdminType());
        switch (adminTypeEnum) {
            case AdminTypeEnum.ADMIN:
                return listOrdinaryRoles(adminRoleService.lambdaQuery()
                        .eq(AdminRole::getShopId, getShopId())
                        .eq(AdminRole::getMerchantId, 0)
                        .eq(AdminRole::getVendorId, 0));
            case AdminTypeEnum.SHOP:
            case AdminTypeEnum.STORE:
            case AdminTypeEnum.PICKUP:
                Shop shop = shopMapper.selectById(getShopId());
                if (shop == null) {
                    throw new GlobalException("店铺不存在");
                }

                Merchant merchant = merchantMapper.selectById(shop.getMerchantId());
                if (merchant != null) {
                    return listOrdinaryRoles(adminRoleService.lambdaQuery()
                            .and(qw -> qw
                                    .and(inner -> inner
                                            .eq(AdminRole::getMerchantId, merchant.getMerchantId())
                                            .eq(AdminRole::getShopId, 0)
                                    )
                                    .or()
                                    .eq(AdminRole::getShopId, getShopId())
                            ));
                }
                return listOrdinaryRoles(adminRoleService.lambdaQuery()
                        .eq(AdminRole::getShopId, getShopId()));
            case AdminTypeEnum.VENDOR:
                return listOrdinaryRoles(adminRoleService.lambdaQuery()
                        .eq(AdminRole::getVendorId, getVendorId()));
            default:
                throw new GlobalException("管理员类型错误");
        }
    }

    private List<AdminRole> listOrdinaryRoles(LambdaQueryChainWrapper<AdminRole> query) {
        return query.and(qw -> qw
                        .isNull(AdminRole::getRoleCode)
                        .or()
                        .notIn(AdminRole::getRoleCode, List.of("pos_cashier", "kefu_agent")))
                .list();
    }

    @Override
    public void getCode(String type, String mobile, String verifyToken) {
        AdminUser currentAdmin = this.getById(SecurityUtils.getCurrentAdminId());
        String currentMobile = currentAdmin != null ? currentAdmin.getMobile() : null;
        if (StrUtil.isNotEmpty(mobile) && StrUtil.isNotEmpty(currentMobile)
                && !mobile.equals(currentMobile)
                && !"1".equals(stringRedisTemplate.opsForValue().get(verifiedMobileKey(SecurityUtils.getCurrentAdminId())))) {
            throw new GlobalException("请先验证当前绑定的手机号");
        }
        if (StrUtil.isEmpty(mobile)) {
            if (currentAdmin == null || StrUtil.isEmpty(currentMobile)) {
                throw new GlobalException("手机号不能为空");
            }
            mobile = currentMobile;
        }
        RegisterSmsDTO dto = new RegisterSmsDTO();
        dto.setEvent("login");
        dto.setMobile(mobile);
        dto.setVerifyToken(verifyToken);
        configService.sendSms(dto);
    }

    private String verifiedMobileKey(Integer adminId) {
        return "admin_modify_mobile_verified:" + adminId;
    }

    @Override
    public List<Integer> getBindMainShopIds(String mainAccount, Boolean onlyAdmin) {
        return getBaseMapper().getBindMainShopIds(mainAccount, onlyAdmin);
    }

    @Override
    public List<Integer> getBindMainVendorIds(String mainAccount, Boolean onlyAdmin) {
        return getBaseMapper().getBindMainVendorIds(mainAccount, onlyAdmin);
    }

    @Override
    public AdminUserDTO detail(Integer id) {
        // 获取用户信息，若不存在则抛出异常
        AdminUser adminUser = this.getById(id);
        Assert.notNull(adminUser, () -> new GlobalException(USER_NOT_EXIST));

        // 复制属性
        AdminUserDTO adminUserDTO = new AdminUserDTO();
        BeanUtils.copyProperties(adminUser, adminUserDTO);

        // RBAC: 有角色时从角色动态解析权限列表
        if (adminUser.getRoleId() != null && adminUser.getRoleId() > 0) {
            AdminRole role = adminRoleService.getById(adminUser.getRoleId());
            if (role != null) {
                adminUserDTO.setRoleName(role.getRoleName());
                adminUserDTO.setAuthList(JSONUtil.toList(role.getAuthorityList(), String.class));
            } else {
                adminUserDTO.setAuthList(Collections.emptyList());
            }
        } else {
            adminUserDTO.setAuthList(
                    "[]".equals(adminUser.getAuthList())
                            ? Collections.emptyList()
                            : Arrays.asList(StringUtils.str2Array(adminUser.getAuthList()))
            );
        }

        List<AdminUserShop> adminUserShops = adminUserShopMapper.selectList(
                Wrappers.lambdaQuery(AdminUserShop.class).eq(AdminUserShop::getAdminId, adminUser.getAdminId())
        );
        if (ObjectUtil.isNotEmpty(adminUserShops)) {
            List<AdminUserDTO.UserShopDTO> userShops = adminUserShops.stream().map(adminUserShop -> {
                Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>().eq(Shop::getShopId, adminUserShop.getShopId()));
                if (shop == null) {
                    return null;
                }
                AdminUserDTO.UserShopDTO userShop = new AdminUserDTO.UserShopDTO();
                ShopVO shopVO = new ShopVO();
                long timestampInSeconds = shop.getAddTime();
                Date date = new Date(timestampInSeconds * 1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                shopVO.setAddTime(sdf.format(date));
                shopVO.setContactMobile(shop.getContactMobile());
                shopVO.setShopId(shop.getShopId());
                shopVO.setShopLogo(shop.getShopLogo());
                shopVO.setShopTitle(shop.getShopTitle());
                shopVO.setStatus(shop.getStatus());
                shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
                userShop.setShop(shopVO);
                return userShop;
            }).filter(Objects::nonNull).toList();
            adminUserDTO.setUserShop(userShops);
        }



        return adminUserDTO;
    }


    @Override
    public AdminUser getAdminUserByUsername(String username, Boolean cache) {
       return getAdminUserByUsername(username,cache,false);
    }

    @Override
    public AdminUser getAdminUserByUsername(String username, Boolean cache,Boolean isDeleteThrowException){
        AdminUser adminUser = null;
        if (cache) {
            adminUser = redisTemplate.opsForValue().get(StrUtil.format("{}{}", ADMIN_USER, username));
            if (adminUser != null) {
                if(Objects.equals(adminUser.getIsDeleted(), 0) ||
                        adminUser.getIsDeleted() == null){
                    //未删除标识才返回
                    return adminUser;
                }else if (isDeleteThrowException){
                    //被删除
                    throw new GlobalException(USER_DELETED);
                }
            }
        }
        Wrapper<AdminUser> wrapper = Wrappers.<AdminUser>lambdaQuery()
                .eq(AdminUser::getUsername, username)
                // 历史脏数据可能存在同名的已软删除账号，登录只允许使用有效账号。
                .and(qw -> qw.isNull(AdminUser::getIsDeleted).or().eq(AdminUser::getIsDeleted, 0))
                // 同名有效脏数据时保持选择稳定，避免 getOne 因多行直接失败。
                .orderByDesc(AdminUser::getAdminId)
                .last("LIMIT 1");
        adminUser = this.getOne(wrapper);

        if (adminUser != null) {
            redisTemplate.opsForValue().set(StrUtil.format("{}{}", ADMIN_USER, adminUser.getUsername()), adminUser);
            if(adminUser.getIsDeleted() == null || adminUser.getIsDeleted() == 0){
                //未删除才能进行登录
                return adminUser;
            }else if (isDeleteThrowException){
                //被删除
                throw new GlobalException(USER_DELETED);
            }
        }
        return null;
    }

    @Override
    public AdminUser getAdminUserByUseMobile(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return null;
        }
        return this.lambdaQuery().eq(AdminUser::getMobile, mobile).one();
    }

    @Override
    public List<AdminUser> getAdminUserByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AdminUser::getAdminId, ids);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adminUserDel(Integer id) {
        if (id == null) {
            return false;
        }

        if (id.equals(SecurityUtils.getCurrentAdminId())) {
            throw new GlobalException("不能删除当前登录用户");
        }

        // 获取用户信息，若不存在则抛出异常
        AdminUser adminUser = Optional.ofNullable(this.getById(id))
                .orElseThrow(() -> new GlobalException(USER_NOT_EXIST));
        // 店铺或供应商主账号必须先解除主体关联，避免删除后组织无法继续解绑/换绑主账号。
        AdminUserShop shopMainAccount = adminUserShopMapper.selectOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, id)
                .eq(AdminUserShop::getIsAdmin, 1)
                .last("LIMIT 1"));
        if (shopMainAccount != null) {
            throw new GlobalException("主账号不允许删除");
        }
        // 删除改后台用户
        boolean isDeleted = this.removeById(id);
        // 删除成功后需要清除后台的缓存
        if (isDeleted) {
            // 删除该用户所有token，使用模糊匹配
            Set<String> keys = redisTemplate.keys(Constants.getAllLoginTokenFormatByUserName(adminUser.getUsername()));
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            removePlatformWorkbenchBinding(id);
        }
        return isDeleted;
    }

    @Override
    public Page<AdminUserDTO> list(AdminUserPageQuery pageQuery) {
        String adminUserType = "admin,suppliers";
        if ("shop".equals(pageQuery.getAdminUserType())) {
            adminUserType = "shop";
        }
        if ("vendor".equals(pageQuery.getAdminUserType())) {
            adminUserType = "vendor";
        }

        List<String> types = StrUtil.split(adminUserType, StrUtil.COMMA);

        // 分页参数
        Page<AdminUser> adminUserPage = buildSortOrder(pageQuery);

        adminUserPage = this.lambdaQuery()
                .and(StrUtil.isNotBlank(pageQuery.getKeyword()), query ->
                        query.like(AdminUser::getUsername, pageQuery.getKeyword())
                                .or()
                                .like(AdminUser::getMobile, pageQuery.getKeyword()))
                .in(ObjectUtil.isNotEmpty(pageQuery.getAdminUserType()), AdminUser::getAdminType, types)
                .eq(AdminUser::getIsDeleted,0)
                .eq(StrUtil.isNotBlank(pageQuery.getSuppliersId()), AdminUser::getSuppliersId, pageQuery.getSuppliersId())
                .page(adminUserPage);

        List<AdminUser> records = adminUserPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageUtil.convertPage(adminUserPage, new ArrayList<>());
        }
        List<Integer> roleIds = records.stream()
                .map(AdminUser::getRoleId)
                .distinct()
                .toList();
        List<AdminRole> adminRoleList = adminRoleService.lambdaQuery()
                .in(AdminRole::getRoleId, roleIds)
                .list();
        Map<Integer, AdminRole> adminRoleMap = adminRoleList.stream()
                .collect(Collectors.toMap(AdminRole::getRoleId, Function.identity()));

        // 将 AdminUser 对象列表转换为 AdminUserDTO 对象列表
        List<AdminUserDTO> recordsDTO = records.stream()
                .map(adminUser -> {
                    AdminUserDTO userDTO = new AdminUserDTO();
                    // 复制基本属性
                    BeanUtils.copyProperties(adminUser, userDTO);
                    userDTO.setAddTime(adminUser.getAddTime().toString());
                    AdminRole adminRole = adminRoleMap.get(adminUser.getRoleId());
                    if (adminRole != null) {
                        userDTO.setRoleName(adminRole.getRoleName());
                    }
                    // 将 String 类型的 authList 转换为 List<String>
                    if (adminUser.getAuthList() != null) {
                        userDTO.setAuthList(Arrays.asList(adminUser.getAuthList().split(",")));
                    }
                    return userDTO;
                })
                .toList();
        return PageUtil.convertPage(adminUserPage, recordsDTO);
    }

    @Override
    @CacheEvict(value = "admin_user", key = "#adminUserDTO.getUsername()")
    @Transactional(rollbackFor = Exception.class)
    public boolean update(AdminUserDTO adminUserDTO) {
        AdminUser adminUser = buildAdminUser(adminUserDTO);
        boolean updated = this.updateById(adminUser);
        if (updated) {
            AdminUser persisted = this.getById(adminUser.getAdminId());
            syncPlatformWorkbenchBinding(persisted != null ? persisted : adminUser);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if (!"del".equals(batchDTO.getType())) {
            return false;
        }
        for (Integer id : batchDTO.getIds()) {
            adminUserDel(id);
        }
        return true;
    }

    private void syncPlatformWorkbenchBinding(AdminUser adminUser) {
        if (adminUser == null || adminUser.getAdminId() == null) {
            return;
        }
        AdminRole role = adminUser.getRoleId() != null && adminUser.getRoleId() > 0
                ? adminRoleService.getById(adminUser.getRoleId()) : null;
        AdminUserShop binding = adminUserShopMapper.selectOne(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, adminUser.getAdminId())
                .eq(AdminUserShop::getShopId, 0));
        boolean platformRole = role != null
                && AdminTypeEnum.ADMIN.getCode().equals(role.getAdminType())
                && Integer.valueOf(0).equals(role.getShopId());
        boolean existingPlatformBinding = binding != null
                && AdminTypeEnum.ADMIN.getCode().equals(adminUser.getAdminType());
        if (!platformRole && !existingPlatformBinding) {
            return;
        }
        AdminUserShop candidate = binding != null ? binding : new AdminUserShop();
        candidate.setAdminId(adminUser.getAdminId());
        candidate.setShopId(0);
        candidate.setRoleId(adminUser.getRoleId());
        candidate.setIsAdmin(0);
        boolean isKefu = platformRole && workbenchCandidatePolicy.isKefuCandidate(candidate, role);
        boolean isPos = platformRole && workbenchCandidatePolicy.isPosCandidate(candidate, role);
        if (binding == null && !isKefu && !isPos) {
            return;
        }

        candidate.setUserId(Objects.requireNonNullElse(adminUser.getUserId(), 0));
        candidate.setUsername(adminUser.getUsername());
        candidate.setEmail(adminUser.getEmail());
        candidate.setAvatar(adminUser.getAvatar());
        candidate.setAuthList("[]");
        candidate.setIsUsing(Objects.requireNonNullElse(adminUser.getIsUsing(), 0));
        if (binding == null) {
            candidate.setAddTime(Objects.requireNonNullElse(adminUser.getAddTime(), StringUtils.getCurrentTime()));
            adminUserShopMapper.insert(candidate);
        } else {
            adminUserShopMapper.updateById(candidate);
        }

        if (isKefu) {
            roleLifecycle.ensureKefu(adminUser.getAdminId(), 0);
            if (Integer.valueOf(1).equals(candidate.getIsUsing())) {
                roleLifecycle.disableKefu(adminUser.getAdminId(), 0);
            }
        } else {
            roleLifecycle.removeKefu(adminUser.getAdminId(), 0);
        }
    }

    private void removePlatformWorkbenchBinding(Integer adminId) {
        adminUserShopMapper.delete(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, adminId)
                .eq(AdminUserShop::getShopId, 0));
        roleLifecycle.removeKefu(adminId, 0);
    }

    /**
     * 构建 AdminUser 对象
     *
     * @param adminUserDTO 前端提交的数据
     * @return AdminUser
     */
    private AdminUser buildAdminUser(AdminUserDTO adminUserDTO) {
        // 判断用户名是否重复
        long usernameCount = this.count(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, adminUserDTO.getUsername())
                .ne(adminUserDTO.getAdminId() != null, AdminUser::getAdminId, adminUserDTO.getAdminId())
        );

        if (usernameCount > 0) {
            throw new GlobalException(USERNAME_IS_EXIST);
        }

        long mobileCount = this.count(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getMobile, adminUserDTO.getMobile())
                .ne(adminUserDTO.getAdminId() != null, AdminUser::getAdminId, adminUserDTO.getAdminId())
        );

        if (mobileCount > 0) {
            throw new GlobalException(MOBILE_IS_EXIST);
        }
        if (StrUtil.isNotBlank(adminUserDTO.getEmail())) {

            long emailCount = this.count(new LambdaQueryWrapper<AdminUser>()
                    .eq(AdminUser::getEmail, adminUserDTO.getEmail())
                    .ne(adminUserDTO.getAdminId() != null, AdminUser::getAdminId, adminUserDTO.getAdminId())
            );

            if (emailCount > 0) {
                throw new GlobalException(EMAIL_IS_EXIST);
            }
        }


        // 设置默认 roleId
        Integer roleId = adminUserDTO.getRoleId();
        adminUserDTO.setRoleId(Optional.ofNullable(roleId).orElse(0));

        AdminUser adminUser = new AdminUser();
        BeanUtils.copyProperties(adminUserDTO, adminUser);

        String avatar = adminUserDTO.getAvatar();
        if (ObjectUtil.isEmpty(avatar)) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
            String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
            adminUser.setAvatar(format);
        }

        Integer suppliersId = adminUserDTO.getSuppliersId();
        if (suppliersId != null && suppliersId != 0) {
            adminUser.setAdminType("suppliers");
        }
        String oldPassword = adminUserDTO.getOldPassword();
        if (StrUtil.isNotEmpty(oldPassword)) {
            AdminUser adminUserById = this.getById(adminUserDTO.getAdminId());
            boolean matches = passwordEncoder.matches(adminUserById.getPassword(), oldPassword);
            if (!matches) {
                throw new GlobalException(OLD_PASSWORD_ERROR);
            }

        }
        String password = adminUserDTO.getPassword();
        if (StrUtil.isNotEmpty(password)) {
            adminUser.setPassword(passwordEncoder.encode(password));
        }

        String pwdConfirm = adminUserDTO.getPwdConfirm();
        if (StrUtil.isNotEmpty(pwdConfirm) && StrUtil.isNotEmpty(password) && !password.equals(pwdConfirm)) {
            throw new GlobalException(PASSWORD_NOT_CONSISTENT);
        }
        // 设置初始密码
        if (StrUtil.isNotEmpty(adminUserDTO.getInitialPassword())) {
            adminUser.setPassword(passwordEncoder.encode(adminUserDTO.getInitialPassword()));
        }
        // RBAC: 只存 role_id，不再复制权限到 auth_list
        adminUser.setRoleId(Optional.ofNullable(adminUserDTO.getRoleId()).orElse(0));
        adminUser.setAuthList("[]");
        adminUser.setAddTime(StringUtils.getCurrentTime());
        return adminUser;
    }
}
