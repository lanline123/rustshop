package com.tigshop.service.account.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.vendor.AdminInfoEnum;
import com.tigshop.bean.enums.vendor.VendorIsAdminEnum;
import com.tigshop.bean.enums.supplychain.VendorSourceTypeEnum;
import com.tigshop.bean.event.VendorMainAccountBindEvent;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.param.account.*;
import com.tigshop.bean.vo.account.AdminUserAccountListVO;
import com.tigshop.bean.vo.account.ShopOrVendorListVO;
import com.tigshop.bean.vo.authority.AdminUserAccountVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.vendor.AdminUserVendorMapper;
import com.tigshop.service.account.AdminAccountService;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.merchant.MerchantService;
import com.tigshop.service.shop.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static com.tigshop.common.constant.authority.AdminUserConstants.PASSWORD;
import static com.tigshop.common.constant.vendor.VendorConstants.VENDOR_ADMIN_USER_NOT_EXIST;
import static com.tigshop.common.constant.vendor.VendorConstants.VENDOR_USER_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class AdminAccountServiceImpl implements AdminAccountService {

    private final AdminUserMapper adminUserMapper;
    private final AdminUserShopMapper adminUserShopMapper;
    private final AdminUserVendorMapper adminUserVendorMapper;
    private final AdminUserService adminUserService;
    private final AdminRoleService adminRoleService;
    private final ShopService shopService;
    private final MerchantService merchantService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private final ApplicationContext applicationContext;

    @Override
    public AdminUserAccountVO getMainAccountByShopOrVendorId(Integer adminId, Integer id, String type) {

        AdminUser mainAdminUser = null;

        if (adminId != null && adminId > 0) {
            AdminUserAccountVO accountVO = new AdminUserAccountVO();
            BeanUtils.copyProperties(adminUserMapper.selectById(adminId), accountVO);
            return accountVO;
        }

        if (id == null || StrUtil.isBlank(type)) {
            throw new GlobalException("参数不能为空");
        }

        if ("shop".equals(type) || "store".equals(type) || "pickup".equals(type)) {
            // 查询店铺主账号
            LambdaQueryWrapper<AdminUserShop> shopQueryWrapper = new LambdaQueryWrapper<>();
            shopQueryWrapper.eq(AdminUserShop::getShopId, id).eq(AdminUserShop::getIsAdmin, 1);
            AdminUserShop adminUserShop = adminUserShopMapper.selectOne(shopQueryWrapper);

            if (adminUserShop != null) {
                mainAdminUser = adminUserMapper.selectById(adminUserShop.getAdminId());
            }
        } else {
            {
                throw new GlobalException("类型参数错误，只支持shop或vendor");
            }
        }

        if (mainAdminUser == null) {
            throw new GlobalException("未找到对应的主账号信息");
        }

        // 转换为VO对象
        AdminUserAccountVO accountVO = new AdminUserAccountVO();
        BeanUtils.copyProperties(mainAdminUser, accountVO);

        return accountVO;
    }

    @Override
    public Page<ShopOrVendorListVO> pageShopOrVendor(ShopOrVendorPageParam shopOrVendorPageParam) {
        if (shopOrVendorPageParam == null || shopOrVendorPageParam.getAdminId() == null || StrUtil.isBlank(shopOrVendorPageParam.getType())) {
            return new Page<>();
        }

        // 分页
        Page<ShopOrVendorListVO> page = new Page<>(shopOrVendorPageParam.getPage(), shopOrVendorPageParam.getSize());

        if (List.of("shop", "store", "pickup").contains(shopOrVendorPageParam.getType())) {
            return pageShops(shopOrVendorPageParam, page);
        }
        else {
            throw new GlobalException("类型参数错误，只支持shop或vendor");
        }
    }

    @Transactional
    @Override
    public void bindMainAccount(ShopOrVendorBindParam shopOrVendorBindParam) {
        if ("shop".equals(shopOrVendorBindParam.getType()) || "store".equals(shopOrVendorBindParam.getType()) || "pickup".equals(shopOrVendorBindParam.getType())) {
            bindShopMainAccount(shopOrVendorBindParam);
            return;
        }
        throw new GlobalException("类型参数错误，只支持shop或vendor");
    }


    private void bindShopMainAccount(ShopOrVendorBindParam shopOrVendorBindParam) {
        AdminUser admin = applicationContext.getBean(this.getClass()).getAdmin(shopOrVendorBindParam);

        AdminUserShop oldUserShop = adminUserShopMapper.selectOne(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getShopId, shopOrVendorBindParam.getId()).eq(AdminUserShop::getAdminId, admin.getAdminId()));

        // 获取供应商的主账号
        LambdaQueryWrapper<AdminUserShop> shopQueryWrapper = new LambdaQueryWrapper<>();
        shopQueryWrapper.eq(AdminUserShop::getShopId, shopOrVendorBindParam.getId()).eq(AdminUserShop::getIsAdmin, 1);
        AdminUserShop adminUserShop = adminUserShopMapper.selectOne(shopQueryWrapper);
        if (adminUserShop == null) {
            recoverShopMainAccount(shopOrVendorBindParam, admin, oldUserShop);
            return;
        }

        Integer oldAdminId = adminUserShop.getAdminId();
        String oldAdminUserName = adminUserShop.getUsername();
        adminUserShop.setUsername(admin.getUsername());
        adminUserShop.setAdminId(admin.getAdminId());
        adminUserShop.setAuthList(JSONUtil.toJsonStr(List.of("all")));
        adminUserShop.setIsAdmin(1);
        // 绑定最新的
        adminUserShopMapper.updateById(adminUserShop);

        if (oldUserShop != null && !Objects.equals(oldUserShop.getId(), adminUserShop.getId())) {
            oldUserShop.setAdminId(oldAdminId);
            oldUserShop.setIsAdmin(0);
            oldUserShop.setUsername(oldAdminUserName);
            adminUserShopMapper.updateById(oldUserShop);
        }
    }

    private void recoverShopMainAccount(ShopOrVendorBindParam bindParam, AdminUser admin,
                                        AdminUserShop targetRecord) {
        Shop shop = shopService.getById(bindParam.getId());
        if (shop == null) {
            throw new GlobalException("店铺不存在");
        }
        Integer roleId = adminRoleService.initAdminRole(shop.getMerchantId(), bindParam.getId());
        boolean createRecord = targetRecord == null;
        AdminUserShop mainAccount = createRecord ? new AdminUserShop() : targetRecord;
        mainAccount.setShopId(bindParam.getId());
        mainAccount.setAdminId(admin.getAdminId());
        mainAccount.setUserId(resolveBoundUserId(bindParam));
        mainAccount.setUsername(admin.getUsername());
        mainAccount.setEmail(admin.getEmail());
        mainAccount.setAvatar(admin.getAvatar());
        mainAccount.setAuthList(JSONUtil.toJsonStr(List.of("all")));
        mainAccount.setIsUsing(0);
        mainAccount.setIsAdmin(1);
        mainAccount.setRoleId(roleId);
        if (createRecord) {
            mainAccount.setAddTime(StringUtils.getCurrentTime());
            adminUserShopMapper.insert(mainAccount);
        } else {
            adminUserShopMapper.updateById(mainAccount);
        }
    }

    private Integer resolveBoundUserId(ShopOrVendorBindParam bindParam) {
        if (ObjectUtil.equals(bindParam.getAdminData().getType(), AdminInfoEnum.USER.getCode())) {
            return bindParam.getAdminData().getUserId();
        }
        return 0;
    }

    @Transactional
    @Override
    public AdminUser getAdmin(ShopOrVendorBindParam createParam) {
        AdminUser admin = null;
        if (ObjectUtil.equals(createParam.getAdminData().getType(), AdminInfoEnum.USER.getCode())) {
            //会员类型
            User user = userMapper.selectById(createParam.getAdminData().getUserId());
            if (user == null) {
                throw new GlobalException(VENDOR_USER_NOT_EXIST);
            }
            if (ObjectUtil.isEmpty(user.getMobile())) {
                throw new GlobalException("会员手机号码不能为空");
            }
            //通过手机查询是否有对应管理员
            admin = adminUserService.getAdminUserByUseMobile(user.getMobile());
            if (admin == null) {
                AdminUser adminUserCreate = applicationContext.getBean(this.getClass()).getAdminUser(user, createParam);
                adminUserService.save(adminUserCreate);
                admin = adminUserCreate;
            }
        } else {
            //管理员类型
            admin = adminUserService.getById(createParam.getAdminData().getAdminId());
            if (admin == null) {
                throw new GlobalException(VENDOR_ADMIN_USER_NOT_EXIST);
            }
        }
        return admin;
    }

    @Transactional
    @Override
    public AdminUser getAdminUser(User user, ShopOrVendorBindParam createParam) {
        AdminUser adminUserCreate = new AdminUser();
        adminUserCreate.setUsername(user.getUsername());
        adminUserCreate.setUserId(user.getUserId());
        adminUserCreate.setMobile(user.getMobile());
        adminUserCreate.setEmail(user.getEmail());
        adminUserCreate.setPassword(passwordEncoder.encode(PASSWORD));
        adminUserCreate.setAdminType(createParam.getType());
        adminUserCreate.setIsUsing(0);
        adminUserCreate.setAuthList("[]");
        adminUserCreate.setRoleId(1);
        // 设置默认头像
        int randomNumber = ThreadLocalRandom.current().nextInt(1, 34);
        String format = StrUtil.format("../assets/avatar/{}.jpeg", randomNumber);
        adminUserCreate.setAvatar(format);
        return adminUserCreate;
    }

    @Transactional
    @Override
    public void updateMainAccount(UpdateMainAccountParam updateMainAccountParam) {
        AdminUser adminUser = adminUserMapper.selectById(updateMainAccountParam.getAdminId());
        if (adminUser == null) {
            throw new GlobalException("用户不存在");
        }
        if (ObjectUtil.isNotEmpty(updateMainAccountParam.getUsername())) {
            // 判断用户名是否已存在，但出去自己
            Long count = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, updateMainAccountParam.getUsername()).ne(AdminUser::getAdminId, adminUser.getAdminId()));
            if (count > 0) {
                throw new GlobalException("账号名称已经存在");
            } else {
                adminUser.setUsername(updateMainAccountParam.getUsername());
            }
        }
        if (ObjectUtil.isNotEmpty(updateMainAccountParam.getMobile())) {
            // 判断用户名是否已存在，但出去自己
            Long count = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getMobile, updateMainAccountParam.getMobile()).ne(AdminUser::getAdminId, adminUser.getAdminId()));
            if (count > 0) {
                throw new GlobalException("手机号已经使用");
            } else {
                adminUser.setMobile(updateMainAccountParam.getMobile());
            }
        }
        if (ObjectUtil.isNotEmpty(updateMainAccountParam.getEmail())) {
            // 判断用户名是否已存在，但出去自己
            Long count = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getEmail, updateMainAccountParam.getEmail()).ne(AdminUser::getAdminId, adminUser.getAdminId()));
            if (count > 0) {
                throw new GlobalException("邮箱已经使用");
            } else {
                adminUser.setEmail(updateMainAccountParam.getEmail());
            }
        }
        adminUserMapper.updateById(adminUser);
    }

    @Transactional
    @Override
    public void updateMainAccountPwd(UpdateMainAccountPwdParam updateMainAccountPwdParam) {
        AdminUser adminUser = adminUserMapper.selectById(updateMainAccountPwdParam.getAdminId());
        if (adminUser == null) {
            throw new GlobalException("用户不存在");
        }
        // 判断两次密码是否一致
        if (!updateMainAccountPwdParam.getPassword().equals(updateMainAccountPwdParam.getPwdConfirm())) {
            throw new GlobalException("两次密码不一致");
        }
        adminUser.setPassword(passwordEncoder.encode(updateMainAccountPwdParam.getPassword()));
        adminUserMapper.updateById(adminUser);
    }

    @Override
    public Page<AdminUserAccountListVO> pageAdminUser(AdminUserPageParam pageQuery) {
        // 分页
        Page<AdminUser> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();

        // 搜索关键字
        if (StringUtils.isNotEmpty(pageQuery.getKeyword())) {
            queryWrapper.nested(qw -> {
                qw.like(AdminUser::getUsername, pageQuery.getKeyword()).or().like(AdminUser::getMobile, pageQuery.getKeyword());
            });
        }

        // 软删除搜索标识
        if (pageQuery.getIsDeleted() != null) {
            queryWrapper.eq(AdminUser::getIsDeleted, pageQuery.getIsDeleted());
        }

        queryWrapper.orderByDesc(AdminUser::getAdminId);
        // 执行查询
        Page<AdminUser> vendorPage = adminUserService.page(page, queryWrapper);
        // 获取查询结果
        List<AdminUser> vendorPageRecords = vendorPage.getRecords();
        // 转换为VO
        List<AdminUserAccountListVO> voList = vendorPageRecords.stream().filter(Objects::nonNull).map(adminUser -> {
            AdminUserAccountListVO accountListVO = new AdminUserAccountListVO();
            accountListVO.setAdminId(adminUser.getAdminId());
            accountListVO.setAvatar(adminUser.getAvatar());
            accountListVO.setUsername(adminUser.getUsername());
            accountListVO.setMobile(adminUser.getMobile());
            accountListVO.setEmail(adminUser.getEmail());
            accountListVO.setAddTime(adminUser.getAddTime().toString());
            // 查询关联店铺数/门店数（查询方法仅统计主账号关系）
            Long shopCount = adminUserShopMapper.selectShopCountByAdminIdAndType(adminUser.getAdminId(), ShopTypeEnum.SHOP.getCode());
            Long storeCount = adminUserShopMapper.selectShopCountByAdminIdAndType(adminUser.getAdminId(), ShopTypeEnum.STORE.getCode());
            accountListVO.setShopCount(shopCount);
            accountListVO.setStoreCount(storeCount);
            // 查询供应商数量
            Long vendorCount = adminUserVendorMapper.selectCount(new LambdaQueryWrapper<AdminUserVendor>()
                    .eq(AdminUserVendor::getAdminId, adminUser.getAdminId())
                    .eq(AdminUserVendor::getIsAdmin, VendorIsAdminEnum.YES.getCode()));
            accountListVO.setVendorCount(vendorCount);
            accountListVO.setIsAdmin(shopCount != null && shopCount > 0
                    || storeCount != null && storeCount > 0
                    || vendorCount != null && vendorCount > 0 ? VendorIsAdminEnum.YES.getCode() : 0);
            return accountListVO;
        }).toList();
        return PageUtil.convertPage(vendorPage, voList);
    }

    private Page<ShopOrVendorListVO> pageShops(ShopOrVendorPageParam pageParam, Page<ShopOrVendorListVO> page) {
        // 查询该主账号关联的店铺ID列表（仅主账号关系，避免把该账号作为子账号挂靠的他人店铺一并查出）
        List<AdminUserShop> adminUserShops = adminUserShopMapper.selectList(new LambdaQueryWrapper<AdminUserShop>()
                .eq(AdminUserShop::getAdminId, pageParam.getAdminId())
                .eq(AdminUserShop::getIsAdmin, VendorIsAdminEnum.YES.getCode()));

        if (adminUserShops.isEmpty()) {
            return page;
        }

        List<Integer> shopIds = adminUserShops.stream().map(AdminUserShop::getShopId).toList();

        // 构造查询构造器
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Shop::getShopId, shopIds);
        if (AdminTypeEnum.fromCode(pageParam.getType()) == AdminTypeEnum.SHOP) {
            queryWrapper.eq(Shop::getShopType, ShopTypeEnum.SHOP.getCode());
        }
        if (AdminTypeEnum.fromCode(pageParam.getType()) == AdminTypeEnum.STORE) {
            queryWrapper.eq(Shop::getShopType, ShopTypeEnum.STORE.getCode());
        }
        if (AdminTypeEnum.fromCode(pageParam.getType()) == AdminTypeEnum.PICKUP) {
            queryWrapper.eq(Shop::getShopType, ShopTypeEnum.PICKUP.getCode());
        }

        // 过滤已删除店铺：is_delete=1 为删除，null/0 均视为未删除（列可为 null，不能直接 eq 0）
        queryWrapper.and(qw -> qw.isNull(Shop::getIsDelete).or().eq(Shop::getIsDelete, 0));

        // 搜索关键字
        if (StrUtil.isNotBlank(pageParam.getKeyword())) {
            queryWrapper.nested(qw -> {
                qw.like(Shop::getShopTitle, pageParam.getKeyword());
            });
        }

        queryWrapper.orderByDesc(Shop::getShopId);

        // 执行查询
        Page<Shop> shopPage = new Page<>(pageParam.getPage(), pageParam.getSize());
        shopPage = shopService.page(shopPage, queryWrapper);

        // 转换为VO
        List<ShopOrVendorListVO> voList = shopPage.getRecords().stream().map(shop -> {
            ShopOrVendorListVO vo = new ShopOrVendorListVO();
            vo.setId(shop.getShopId());
            vo.setName(shop.getShopTitle());
            vo.setLogo(shop.getShopLogo());
            vo.setStatus(shop.getStatus());
            vo.setAddTime(shop.getAddTime().toString());
            vo.setType("shop");

            if (shop.getMerchantId() != null && shop.getMerchantId() > 0) {
                Merchant merchant = merchantService.getById(shop.getMerchantId());
                if (merchant != null) {
                    vo.setMerchantName(merchant.getCorporateName());
                }
            }

            AdminUserShop adminUserShop = adminUserShopMapper.selectOne(new LambdaQueryWrapper<AdminUserShop>()
                    .eq(AdminUserShop::getShopId, shop.getShopId())
                    .eq(AdminUserShop::getIsAdmin, VendorIsAdminEnum.YES.getCode())
                    .last("LIMIT 1"));
            if (adminUserShop != null) {
                AdminUser adminUser = adminUserService.getById(adminUserShop.getAdminId());
                if (adminUser != null) {
                    vo.setAdminUsername(adminUser.getUsername());
                }
            }

            return vo;
        }).toList();

        Page<ShopOrVendorListVO> resultPage = new Page<>(shopPage.getCurrent(), shopPage.getSize(), shopPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }


    @Transactional
    @Override
    public void deleteAdminUser(Integer adminId) {
        AdminUser adminUser = adminUserMapper.selectById(adminId);
        if (adminUser == null) {
            throw new GlobalException("用户不存在");
        }
        AdminUserShop adminUserShop = adminUserShopMapper
                .selectOne(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getAdminId, adminId)
                        .eq(AdminUserShop::getIsAdmin, 1).last("limit 1"));
        if (adminUserShop != null) {
            throw new GlobalException("主账号不允许删除");
        }
        adminUser.setIsDeleted(1);
        adminUserMapper.updateById(adminUser);
    }
}
