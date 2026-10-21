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

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminRoleDTO;
import com.tigshop.bean.dto.authority.AdminRoleListDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.common.constant.authority.AdminUserConstants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.authority.AdminRoleMapper;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.vendor.AdminUserVendorMapper;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.shop.ShopEmployeeRoleLifecycle;
import com.tigshop.service.shop.ShopEmployeeWorkbenchCandidatePolicy;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.tigshop.common.constant.authority.AdminRoleConstants.ROLE_NOT_EXIST;

/**
 * 管理员角色服务实现类
 *
 * @author Tigshop团队
 * @create 2024年11月04日 17:16
 */
@Service
public class AdminRoleServiceImpl extends BaseServiceImpl<AdminRoleMapper, AdminRole> implements AdminRoleService {
    @Resource
    private ShopMapper shopMapper;
    @Resource
    private MerchantMapper merchantMapper;
    @Autowired
    private AdminUserMapper adminUserMapper;
    @Autowired
    private AdminUserShopMapper adminUserShopMapper;
    @Autowired
    private AdminUserVendorMapper adminUserVendorMapper;
    @Resource
    private ShopEmployeeRoleLifecycle roleLifecycle;
    @Resource
    private ShopEmployeeWorkbenchCandidatePolicy workbenchCandidatePolicy;

    @Override
    public Page<AdminRoleDTO> list(AdminRoleListDTO adminRoleListDTO) {
        // 初始化分页对象，用于MyBatis-Plus的分页查询。
        Page<AdminRole> page = new Page<>(adminRoleListDTO.getPage(), adminRoleListDTO.getSize());

        // 创建查询条件构造器。
        LambdaQueryWrapper<AdminRole> queryWrapper = new LambdaQueryWrapper<>();

        AdminTypeEnum adminTypeEnum = AdminTypeEnum.fromCode(getAdminType());
        switch (adminTypeEnum) {
            case AdminTypeEnum.ADMIN:
                queryWrapper.and(qw -> qw
                        .eq(AdminRole::getShopId, getShopId())
                        .eq(AdminRole::getAdminType, "admin")
                );
                break;
            case AdminTypeEnum.SHOP:
            case AdminTypeEnum.STORE:
            case AdminTypeEnum.PICKUP:
                Shop shop = shopMapper.selectById(getShopId());
                if (shop == null) {
                    throw new GlobalException("店铺不存在");
                }
                Merchant merchant;
                if (shop.getMerchantId() > 0) {
                    merchant = merchantMapper.selectById(shop.getMerchantId());
                    if (merchant == null) {
                        throw new GlobalException("商户不存在");
                    }
                } else {
                    merchant = null;
                }
                queryWrapper.and(qw -> {
                            if (merchant != null && merchant.getMerchantId() > 0) {
                                // 查询商户级别角色（shopId=0）或当前店铺的角色
                                qw.and(inner -> inner
                                        .eq(AdminRole::getMerchantId, merchant.getMerchantId())
                                        .eq(AdminRole::getShopId, 0)
                                ).or().eq(AdminRole::getShopId, shop.getShopId());
                            } else {
                                qw.eq(AdminRole::getShopId, shop.getShopId());
                            }
                        }
                );
                break;
            case AdminTypeEnum.VENDOR:
                queryWrapper.and(qw -> qw
                        .eq(AdminRole::getVendorId, getVendorId())
                        .eq(AdminRole::getAdminType, "vendor")
                );
                break;
            default:
                throw new GlobalException("管理员类型错误");
        }

        queryWrapper.and(qw -> qw
                .isNull(AdminRole::getRoleCode)
                .or()
                .ne(AdminRole::getRoleCode, "kefu_agent"));

        // 获取搜索关键字，并检查是否为空。
        String keyword = adminRoleListDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            // 如果关键字不为空，构建模糊查询条件。
            queryWrapper.and(qw -> qw
                    .like(AdminRole::getRoleName, keyword)
                    .or()
                    .like(AdminRole::getRoleDesc, keyword));
        }
        // 排序字段
        buildSortOrder(page, adminRoleListDTO.getSortField(), adminRoleListDTO.getSortOrder());

        Page<AdminRole> adminRolePage = this.page(page, queryWrapper);
        List<AdminRoleDTO> adminRoles = adminRolePage.getRecords().stream().map(this::convertToDTO).toList();
        // 批量查询 username 并返回分页结果
        return PageUtil.convertPage(adminRolePage, adminRoles);
    }

    @Override
    public AdminRoleDTO detail(Integer id) {
        AdminRole adminRole = getAccessibleRole(id);
        return convertToDTO(adminRole);
    }

    @Override
    public boolean create(AdminRoleDTO adminRoleDTO) {
        return this.save(convertToBean(adminRoleDTO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(AdminRoleDTO adminRoleDTO) {
        AdminRole existing = getAccessibleRole(adminRoleDTO.getRoleId());
        if (Integer.valueOf(1).equals(existing.getIsSystem())) {
            throw new GlobalException("系统内置角色不可修改");
        }
        AdminRole updated = convertToBean(adminRoleDTO);
        updated.setAdminType(existing.getAdminType());
        updated.setMerchantId(existing.getMerchantId());
        updated.setShopId(existing.getShopId());
        updated.setVendorId(existing.getVendorId());
        updated.setRoleCode(existing.getRoleCode());
        updated.setIsSystem(existing.getIsSystem());
        List<AdminUserShop> bindings = adminUserShopMapper.selectList(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getRoleId, existing.getRoleId()));
        boolean updatedSuccessfully = this.updateById(updated);
        if (!updatedSuccessfully) {
            return false;
        }
        bindings.forEach(binding -> syncKefuCandidate(binding, existing, updated));
        syncPlatformWorkbenchBindings(updated);
        return true;
    }

    private void syncPlatformWorkbenchBindings(AdminRole role) {
        if (!AdminTypeEnum.ADMIN.getCode().equals(role.getAdminType())
                || !Integer.valueOf(0).equals(role.getShopId())) {
            return;
        }
        AdminUserShop candidate = AdminUserShop.builder()
                .shopId(0).roleId(role.getRoleId()).isAdmin(0).build();
        boolean isKefu = workbenchCandidatePolicy.isKefuCandidate(candidate, role);
        boolean isPos = workbenchCandidatePolicy.isPosCandidate(candidate, role);
        if (!isKefu && !isPos) {
            return;
        }
        List<AdminUser> platformUsers = adminUserMapper.selectList(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getRoleId, role.getRoleId())
                .eq(AdminUser::getAdminType, AdminTypeEnum.ADMIN.getCode())
                .and(query -> query.isNull(AdminUser::getIsDeleted).or().eq(AdminUser::getIsDeleted, 0)));
        for (AdminUser adminUser : platformUsers) {
            AdminUserShop binding = adminUserShopMapper.selectOne(new LambdaQueryWrapper<AdminUserShop>()
                    .eq(AdminUserShop::getAdminId, adminUser.getAdminId())
                    .eq(AdminUserShop::getShopId, 0));
            if (binding == null) {
                binding = AdminUserShop.builder()
                        .adminId(adminUser.getAdminId())
                        .userId(Objects.requireNonNullElse(adminUser.getUserId(), 0))
                        .shopId(0)
                        .username(adminUser.getUsername())
                        .email(adminUser.getEmail())
                        .avatar(adminUser.getAvatar())
                        .authList("[]")
                        .isUsing(Objects.requireNonNullElse(adminUser.getIsUsing(), 0))
                        .isAdmin(0)
                        .addTime(Objects.requireNonNullElse(adminUser.getAddTime(), StringUtils.getCurrentTime()))
                        .roleId(role.getRoleId())
                        .build();
                adminUserShopMapper.insert(binding);
            } else {
                binding.setUserId(Objects.requireNonNullElse(adminUser.getUserId(), 0));
                binding.setUsername(adminUser.getUsername());
                binding.setEmail(adminUser.getEmail());
                binding.setAvatar(adminUser.getAvatar());
                binding.setAuthList("[]");
                binding.setIsUsing(Objects.requireNonNullElse(adminUser.getIsUsing(), 0));
                binding.setIsAdmin(0);
                binding.setRoleId(role.getRoleId());
                adminUserShopMapper.updateById(binding);
            }
            if (isKefu) {
                roleLifecycle.ensureKefu(adminUser.getAdminId(), 0);
                if (Integer.valueOf(1).equals(binding.getIsUsing())) {
                    roleLifecycle.disableKefu(adminUser.getAdminId(), 0);
                }
            }
        }
    }

    private void syncKefuCandidate(AdminUserShop binding, AdminRole existing, AdminRole updated) {
        boolean wasKefu = workbenchCandidatePolicy.isKefuCandidate(binding, existing);
        boolean isKefu = workbenchCandidatePolicy.isKefuCandidate(binding, updated);
        if (!wasKefu && isKefu) {
            roleLifecycle.ensureKefu(binding.getAdminId(), binding.getShopId());
        } else if (wasKefu && !isKefu) {
            roleLifecycle.removeKefu(binding.getAdminId(), binding.getShopId());
        }
    }

    @Override
    public boolean updateField(UpdateFieldDTO updateField, String[] allowFields) {
        AdminRole existing = getAccessibleRole(updateField.getId());
        if (Integer.valueOf(1).equals(existing.getIsSystem())) {
            throw new GlobalException("系统内置角色不可修改");
        }
        return super.updateField(updateField, allowFields);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer initAdminRole(Integer merchantId, Integer shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new GlobalException("店铺不存在");
        }
        String adminType = AdminTypeEnum.resolveCodeByShopType(shop.getShopType());

        AdminRole adminRole = this.lambdaQuery()
                .eq(AdminRole::getAdminType, adminType)
                .eq(AdminRole::getMerchantId, merchantId)
                .eq(AdminRole::getShopId, shopId)
                .eq(AdminRole::getIsSystem, 1)
                .one();

        if (adminRole == null && !AdminTypeEnum.SHOP.getCode().equals(adminType)) {
            adminRole = this.lambdaQuery()
                    .eq(AdminRole::getAdminType, AdminTypeEnum.SHOP.getCode())
                    .eq(AdminRole::getMerchantId, merchantId)
                    .eq(AdminRole::getShopId, shopId)
                    .eq(AdminRole::getIsSystem, 1)
                    .one();
            if (adminRole != null) {
                adminRole.setAdminType(adminType);
                this.updateById(adminRole);
                return adminRole.getRoleId();
            }
        }

        if (adminRole != null) {
            return adminRole.getRoleId();
        }

        adminRole = AdminRole.builder()
                .roleName(AdminTypeEnum.systemSuperRoleNameByCode(adminType))
                .roleDesc("系统内置角色")
                .authorityList("[\"all\"]")
                .adminType(adminType)
                .merchantId(merchantId)
                .shopId(shopId)
                .isSystem(1)
                .build();
        this.save(adminRole);
        return adminRole.getRoleId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initEmployeeRoles(Integer shopId) {
        if (shopId == null || shopId == 0) {
            return;
        }
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new GlobalException("店铺不存在");
        }
        if (Integer.valueOf(1).equals(shop.getShopType()) || Integer.valueOf(2).equals(shop.getShopType())) {
            initKefuRole(shopId);
        }
    }

    private Integer initKefuRole(Integer shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new GlobalException("店铺不存在");
        }
        AdminRole adminRole = this.lambdaQuery()
                .eq(AdminRole::getRoleCode, "kefu_agent")
                .eq(AdminRole::getShopId, shopId)
                .eq(AdminRole::getAdminType, AdminTypeEnum.SHOP.getCode())
                .one();
        if (adminRole != null) {
            return adminRole.getRoleId();
        }

        adminRole = AdminRole.builder()
                .roleName("客服")
                .roleCode("kefu_agent")
                .roleDesc("系统内置客服角色")
                .authorityList("[\"groupCustomerService\",\"kefuWorkbench\",\"kefuHistory\",\"kefuHistoryList\",\"kefuPersonal\",\"kefuQuickReply\",\"kefuAccountInfo\"]")
                .adminType(AdminTypeEnum.SHOP.getCode())
                .shopId(shopId)
                .isSystem(1)
                .build();
        this.save(adminRole);
        return adminRole.getRoleId();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminRole initVendorRole(Integer vendorId) {
        AdminRole adminRole = this.lambdaQuery()
                .eq(AdminRole::getVendorId, vendorId)
                .eq(AdminRole::getAdminType, AdminUserConstants.VENDOR_TYPE)
                .eq(AdminRole::getIsSystem, 1)
                .one();

        if (adminRole != null) {
            return adminRole;
        }

        adminRole = AdminRole.builder()
                .roleName(AdminTypeEnum.VENDOR.getSystemSuperRoleName())
                .roleDesc("系统内置角色")
                .authorityList("[\"all\"]")
                .adminType(AdminUserConstants.VENDOR_TYPE)
                .vendorId(vendorId)
                .isSystem(1)
                .build();
        this.save(adminRole);
        return adminRole;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delRole(Integer id) {
        if (id == null) {
            throw new GlobalException(ROLE_NOT_EXIST);
        }
        AdminRole existing = getAccessibleRole(id);
        if (Integer.valueOf(1).equals(existing.getIsSystem())) {
            throw new GlobalException("系统内置角色不可删除");
        }
        // RBAC: 删除前检查是否有账号绑定该角色
        long bindCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getRoleId, id))
                + adminUserShopMapper.selectCount(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getRoleId, id))
                + adminUserVendorMapper.selectCount(new LambdaQueryWrapper<AdminUserVendor>().eq(AdminUserVendor::getRoleId, id));
        if (bindCount > 0) {
            throw new GlobalException("该角色下有 " + bindCount + " 个账号，请先重新分配角色");
        }
        boolean del = del(id);
        if (del) {
            adminUserMapper.update(new LambdaUpdateWrapper<AdminUser>().set(AdminUser::getRoleId, 0).eq(AdminUser::getRoleId, id));
            adminUserShopMapper.update(new LambdaUpdateWrapper<AdminUserShop>().set(AdminUserShop::getRoleId, 0).eq(AdminUserShop::getRoleId, id));
            adminUserVendorMapper.update(new LambdaUpdateWrapper<AdminUserVendor>().set(AdminUserVendor::getRoleId, 0).eq(AdminUserVendor::getRoleId, id));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if ("del".equals(batchDTO.getType())) {
            batchDTO.getIds().forEach(this::delRole);
        }
        return false;
    }

    @Override
    public boolean del(Integer id) {
        return this.removeById(id);
    }

    /**
     * 转换为 DTO
     *
     * @param adminRole 属性模板实体
     * @return 属性模板 DTO
     */
    private AdminRoleDTO convertToDTO(AdminRole adminRole) {
        AdminRoleDTO dto = new AdminRoleDTO();
        BeanUtils.copyProperties(adminRole, dto);

        dto.setAuthorityList(
                JSONUtil.toList(adminRole.getAuthorityList(), String.class)
        );

        return dto;
    }

    /**
     * 转换为实体
     *
     * @param dto 属性模板 DTO
     * @return 属性模板实体
     */
    private AdminRole convertToBean(AdminRoleDTO dto) {
        AdminRole adminRole = new AdminRole();
        BeanUtils.copyProperties(dto, adminRole);
        adminRole.setShopId(getShopId());
        adminRole.setVendorId(getVendorId());
        adminRole.setAdminType(getAdminType());
        adminRole.setAuthorityList(
                JSONUtil.toJsonStr(dto.getAuthorityList())
        );
        adminRole.setIsSystem(null);
        return adminRole;
    }
    /**
     * 角色管理接口的统一数据边界。所有按角色 ID 的读写操作都必须先通过该校验，
     * 避免仅凭主键访问其他店铺、商户或供应商的角色。
     */
    private AdminRole getAccessibleRole(Integer roleId) {
        if (roleId == null) {
            throw new GlobalException(ROLE_NOT_EXIST);
        }
        AdminRole role = this.getById(roleId);
        if (role == null || !isRoleAccessible(role)) {
            throw new GlobalException(ROLE_NOT_EXIST);
        }
        return role;
    }

    private boolean isRoleAccessible(AdminRole role) {
        AdminTypeEnum currentType = AdminTypeEnum.fromCode(getAdminType());
        return switch (currentType) {
            case ADMIN -> Objects.equals(role.getShopId(), getShopId())
                    && AdminTypeEnum.ADMIN.getCode().equals(role.getAdminType());
            case SHOP, STORE, PICKUP -> isShopRoleAccessible(role);
            case VENDOR -> Objects.equals(role.getVendorId(), getVendorId())
                    && AdminTypeEnum.VENDOR.getCode().equals(role.getAdminType());
            case POS -> false;
        };
    }

    private boolean isShopRoleAccessible(AdminRole role) {
        Shop currentShop = shopMapper.selectById(getShopId());
        if (currentShop == null || !(AdminTypeEnum.SHOP.getCode().equals(role.getAdminType())
                || AdminTypeEnum.STORE.getCode().equals(role.getAdminType())
                || AdminTypeEnum.PICKUP.getCode().equals(role.getAdminType()))) {
            return false;
        }
        if (Objects.equals(role.getShopId(), currentShop.getShopId())) {
            return true;
        }
        return Objects.equals(role.getShopId(), 0)
                && currentShop.getMerchantId() != null
                && currentShop.getMerchantId() > 0
                && Objects.equals(role.getMerchantId(), currentShop.getMerchantId());
    }
}
