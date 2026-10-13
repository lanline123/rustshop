package com.tigshop.service.authority.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminRoleDTO;
import com.tigshop.bean.dto.authority.AuthorityChildDTO;
import com.tigshop.bean.dto.authority.AuthorityDTO;
import com.tigshop.bean.dto.authority.AuthorityListDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.authority.Authority;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.bean.vo.authority.AuthorityVO;
import com.tigshop.bean.vo.authority.ChildAuthVO;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.authority.AuthorityMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.vendor.AdminUserVendorMapper;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AuthorityService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.ShopMemberSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.ADMIN_USER;
import static com.tigshop.common.constant.ResultTextConstants.USER_NOT_LOGGED_IN;
import static com.tigshop.common.constant.authority.AuthorityConstants.AUTHORITY_NOT_FOUND;
import static com.tigshop.common.constant.authority.AuthorityConstants.AUTHORITY_SN_EXISTS;

/**
 * 权限服务实现类
 *
 * @author kidd
 * @create 2025/7/3
 */
@RequiredArgsConstructor
@Service
public class AuthorityServiceImpl extends BaseServiceImpl<AuthorityMapper, Authority> implements AuthorityService {

    /**
     * 账期支付菜单：父级 termPayManagement 及其三个子级。
     * 需整组列举——扁平返回的接口不建树，只删父级的话子级会漏出；
     * 且不能用 sn 前缀/包含匹配，promoterManage 小写后含 "term" 会被误删。
     */
    private static final List<String> TERM_PAY_MENU_SN = List.of(
            "termPayManagement", "termOrderManage", "termPayManage", "termMemberManage");

    /**
     * 供应链菜单及其权限点。演示站只在供应商（S2B2C）、跨境或多门店版本展示；
     * 单商户、多商户和企业批发版本即使数据库仍保留权限数据，也必须整组隐藏。
     */
    private static final Set<String> SUPPLY_CHAIN_MENU_SN = Set.of(
            "supplyChain", "purchaseManagement", "selfVendorManagement", "supplyChainSetting",
            "purchaseOrderManage", "purchaseOrderModifyManage", "purchaseReceiptManage", "purchaseReceiptModifyManage",
            "purchaseReturnManage", "purchaseReturnModifyManage", "purchaseReconciliationManage", "purchaseReconciliationModifyManage",
            "selfVendorManage", "selfVendorModifyManage", "selfVendorCategoryManage", "selfVendorCategoryModifyManage",
            "selfVendorProductManage", "selfVendorProductModifyManage", "purchaseSettingManage", "purchaseSettingModifyManage");

    /**
     * 旧版小票打印菜单放在设置栏目；O2O 版本改由收银台设置承载。
     */
    private static final Set<String> LEGACY_RECEIPT_MENU_SN = Set.of(
            "baseReceiptManage", "shopBaseReceiptManage");

    /**
     * 收银菜单既有独立父级 groupPos，也有多个业务前缀，非 O2O 版本需要整组过滤。
     */
    private static boolean isPosMenuAuthority(String authoritySn) {
        return authoritySn != null && ("groupPos".equals(authoritySn)
                || authoritySn.startsWith("pos")
                || authoritySn.startsWith("cashRegisterManage")
                || authoritySn.startsWith("cashierList"));
    }

    static void filterReceiptPrintMenuByEdition(List<Authority> authorities, boolean o2oEnabled) {
        if (CollUtil.isEmpty(authorities)) {
            return;
        }
        authorities.removeIf(item -> {
            String authoritySn = item == null ? null : item.getAuthoritySn();
            return o2oEnabled
                    ? LEGACY_RECEIPT_MENU_SN.contains(authoritySn)
                    : isPosMenuAuthority(authoritySn);
        });
    }

    private final RedisCache redisCache;

    private final AdminRoleService adminRoleService;

    private final TigshopProperties tigshopProperties;

    private final AdminUserShopMapper adminUserShopMapper;

    private final AdminUserVendorMapper adminUserVendorMapper;

    private final ShopMemberSettingService shopMemberSettingService;

    private final com.tigshop.common.core.TranslatePackage translatePackage;


    @Override
    public boolean create(AuthorityDTO authorityDTO) {
        return this.save(buildChildAuth(authorityDTO));
    }

    @Override
    public boolean update(AuthorityDTO authorityDTO) {
        return this.updateById(buildChildAuth(authorityDTO));
    }

    /**
     * 构建子权限(create/update)
     *
     * @param authorityDTO 权限信息
     * @return Authority
     */
    public Authority buildChildAuth(AuthorityDTO authorityDTO) {
        // 检查权限编号是否已存在，如果存在则抛出异常
        if (this.count(new LambdaQueryWrapper<Authority>()
                .ne(authorityDTO.getAuthorityId() != null, Authority::getAuthorityId, authorityDTO.getAuthorityId())
                .eq(Authority::getAuthoritySn, authorityDTO.getAuthoritySn())
                .eq(Authority::getAdminType, authorityDTO.getAdminType())
                .select(Authority::getAuthorityName)) > 0) {
            throw new GlobalException(AUTHORITY_SN_EXISTS);
        }

        Authority authority = new Authority();
        BeanUtils.copyProperties(authorityDTO, authority);

        // 处理子权限，若为空则设置为 "[]"
        authority.setChildAuth(
                Optional.ofNullable(authorityDTO.getChildAuth())
                        .filter(list -> !list.isEmpty())
                        .map(JSONUtil::toJsonStr)
                        .orElse("[]")
        );

        return authority;
    }

    /**
     * 构建子权限(list/detail)
     *
     * @param authority 权限信息
     * @return AuthorityDTO
     */
    public AuthorityDTO buildChildAuth(Authority authority) {
        AuthorityDTO authorityDTO = new AuthorityDTO();
        String childAuth = authority.getChildAuth();
        // authority -> authorityDTO
        BeanUtils.copyProperties(authority, authorityDTO);
        // 处理子权限
        if (StringUtils.isNotBlank(childAuth)) {
            List<AuthorityChildDTO> list = JSONUtil.toList(JSONUtil.parseArray(childAuth), AuthorityChildDTO.class);
            authorityDTO.setChildAuth(list);
        }
        return authorityDTO;
    }

    @Override
    public AuthorityDTO detail(Integer id) {
        Authority authority = this.getById(id);
        if (authority == null) {
            throw new GlobalException(AUTHORITY_NOT_FOUND);
        }
        return buildChildAuth(authority);
    }

    @Override
    public List<AuthorityVO> getAuthorityByName(String name) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(authentication, () -> new GlobalException(USER_NOT_LOGGED_IN));

        String adminUserCacheKey = ADMIN_USER + authentication.getName();
        AdminUser adminUser = redisCache.getCacheObject(adminUserCacheKey);
        Assert.notNull(adminUser, () -> new GlobalException(USER_NOT_LOGGED_IN));

        List<String> userAuthorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        // 搜索关键词：原始关键词 + 译文反查出的中文菜单名，支持用外文关键词搜中文菜单
        List<String> searchKeywords = new ArrayList<>();
        if (StrUtil.isNotBlank(name)) {
            searchKeywords.add(name);
        }

        LambdaQueryWrapper<Authority> queryWrapper = new LambdaQueryWrapper<>();
        if (!searchKeywords.isEmpty()) {
            queryWrapper.and(w -> {
                for (int i = 0; i < searchKeywords.size(); i++) {
                    if (i > 0) {
                        w.or();
                    }
                    w.like(Authority::getAuthorityName, searchKeywords.get(i));
                }
            });
        }
        queryWrapper.eq(Authority::getIsShow, Constants.YES);

        String adminType = HeaderUtils.getAdminType();
        boolean isSupplier = false;

        if (ObjectUtil.equals(adminType, "suppliers")) {
            adminType = "admin";
        }

        if ("shop".equals(adminType) || "store".equals(adminType) || "pickup".equals(adminType)) {
            queryWrapper.eq(Authority::getAdminType, "shop");
            Integer shopId = HeaderUtils.getShopId();
            if (shopId != null && shopId != 0) {
                AdminUserShop adminUserShop = adminUserShopMapper.selectOne(
                        Wrappers.lambdaQuery(AdminUserShop.class).eq(AdminUserShop::getAdminId, adminUser.getAdminId()).eq(AdminUserShop::getShopId, shopId)
                );
                if (adminUserShop != null) {
                    if (adminUserShop.getRoleId() != null && adminUserShop.getRoleId() > 0) {
                        AdminRole role = adminRoleService.getById(adminUserShop.getRoleId());
                        if (role != null) {
                            userAuthorities = JSONUtil.toList(role.getAuthorityList(), String.class);
                        }
                    } else {
                        userAuthorities = JSONArray.parseArray(adminUserShop.getAuthList(), String.class);
                    }
                }
            }
        } else if ("admin".equals(adminType)) {
            queryWrapper.eq(Authority::getAdminType, "admin");
        } else if ("vendor".equals(adminType)) {
            queryWrapper.eq(Authority::getAdminType, "vendor");
            Integer vendorId = HeaderUtils.getVendorId();
            if (vendorId != null && vendorId != 0) {
                AdminUserVendor adminUserVendor = adminUserVendorMapper.selectOne(
                        Wrappers.lambdaQuery(AdminUserVendor.class).eq(AdminUserVendor::getAdminId, adminUser.getAdminId()).eq(AdminUserVendor::getVendorId, vendorId)
                );
                if (adminUserVendor != null) {
                    if (adminUserVendor.getRoleId() != null && adminUserVendor.getRoleId() > 0) {
                        AdminRole role = adminRoleService.getById(adminUserVendor.getRoleId());
                        if (role != null) {
                            userAuthorities = JSONUtil.toList(role.getAuthorityList(), String.class);
                        }
                    } else {
                        userAuthorities = JSONArray.parseArray(adminUserVendor.getAuthList(), String.class);
                    }
                }
            }
        } else if ("supplier".equals(adminType)) {
            isSupplier = true;
            AdminRoleDTO detail = adminRoleService.detail(2);
            if (detail == null) {
                return new ArrayList<>();
            } else {
                queryWrapper.in(Authority::getAuthoritySn, detail.getAuthorityList());
            }
        }

        if (!isSupplier) {
            boolean hasAllAuthority = CollUtil.isEmpty(userAuthorities) || userAuthorities.stream().anyMatch("all"::equals);
            queryWrapper.in(!hasAllAuthority, Authority::getAuthoritySn, userAuthorities);
        }

        List<Authority> authorities = this.list(queryWrapper.select(Authority::getAuthoritySn, Authority::getRouteLink, Authority::getAuthorityName, Authority::getParentId));

        if (CollUtil.isEmpty(authorities)) {
            return new ArrayList<>();
        }

        if (tigshopProperties.getIsOverseas() == 0) {
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "multilingual")).findFirst().ifPresent(authorities::remove);
            authorities.removeIf(item -> "mobileuserUserOverseasManage".equals(item.getAuthoritySn()));
            authorities.removeIf(item -> "taxRateManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsOverseas() == 1) {
            authorities.removeIf(item -> "mobileUserManage".equals(item.getAuthoritySn()));
//            authorities.removeIf(item -> item.getAuthoritySn().contains("grouponPromotionManage"));
        }
        if (tigshopProperties.getIsMerchant() == 0) {
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shop")).findFirst().ifPresent(authorities::remove);
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopManagement")).findFirst().ifPresent(authorities::remove);
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopManage")).findFirst().ifPresent(authorities::remove);
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopSettingsManage")).findFirst().ifPresent(authorities::remove);
        }
        if (tigshopProperties.getIsPro() == 0) {
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "salesman")).findFirst().ifPresent(authorities::remove);
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "eCardManage")).findFirst().ifPresent(authorities::remove);
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userLevelProManage")).findFirst().ifPresent(authorities::remove);
        }
        if (tigshopProperties.getIsPro() == 1) {
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "levelManageManage")).findFirst().ifPresent(authorities::remove);
        }
        if (!(tigshopProperties.getIsO2o() == 1 || tigshopProperties.getIsOverseas() == 1 || tigshopProperties.getIsVendor() == 1
                || tigshopProperties.getIsB2b() == 1)) {
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "enquiryManage")).findFirst().ifPresent(authorities::remove);
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "quotationManage")).findFirst().ifPresent(authorities::remove);
        }
        if (tigshopProperties.getIsB2b() == 0) {
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userCertificationManage")).findFirst().ifPresent(authorities::remove);
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "realNameAuthentication")).findFirst().ifPresent(authorities::remove);
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userAuthenticationManage")).findFirst().ifPresent(authorities::remove);

            authorities.removeIf(item -> item != null && TERM_PAY_MENU_SN.contains(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsVendor() == 0) {
            authorities.removeIf(item -> item.getAuthoritySn().contains("vendor"));
        }
        if (tigshopProperties.getIsO2o() == 0) {
            authorities.removeIf(item -> item.getAuthoritySn().contains("store"));
            // 进店规则为 o2o 专属栏目，非 o2o 版本移除
            authorities.removeIf(item -> "storeEntryRuleSetting".equals(item.getAuthoritySn()));
            authorities.removeIf(item -> item.getAuthoritySn().contains("intraCityDeliveryManage"));
            authorities.removeIf(item -> item.getAuthoritySn().contains("intraCityDeliveryManage"));

            authorities.removeIf(item -> item.getAuthoritySn().contains("deliverymanList"));
            authorities.removeIf(item -> item.getAuthoritySn().contains("servicemanList"));
            authorities.removeIf(item -> item.getAuthoritySn().contains("homeServiceManage"));
            authorities.removeIf(item -> item.getAuthoritySn().contains("cycleBuyManage"));
        }
        filterReceiptPrintMenuByEdition(authorities, tigshopProperties.getIsO2o() == 1);
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0 && tigshopProperties.getIsVendor() == 0) {
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "organize")).findFirst().ifPresent(authorities::remove);
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "adminMerchant")).findFirst().ifPresent(authorities::remove);

            authorities.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "profitsharingManage")).findFirst().ifPresent(authorities::remove);
            authorities.removeIf(item -> "shopUserfundsManagement".equals(item.getAuthoritySn()));
        }
        // 批发菜单由 VORT common.wholesale 编译开关控制，关闭时移除数据库中的批发菜单
        if (!isWholesaleMenuEnabled()) {
            authorities.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().contains("wholesale"));
        }
        removeSupplyChainMenus(authorities);

        return authorities.stream()
                .map(authority -> {
                    AuthorityVO item = BeanUtil.copyProperties(authority, AuthorityVO.class);

                    List<String> authorityNames = new ArrayList<>();
                    authorityNames.add(authority.getAuthorityName());
                    List<String> nameUntilRoot = getParentNameUntilRoot(authorityNames, authority.getParentId());
                    // 菜单名/路径翻译（dataType=PAGE 0）；List<String> 无法用 @JsonTranslate，手动翻译
                    item.setAuthorityName(translatePackage.translate(item.getAuthorityName(), com.tigshop.common.enums.DataType.PAGE.getCode()));
                    item.setAuthorityNames(nameUntilRoot.stream()
                            .map(n -> translatePackage.translate(n, com.tigshop.common.enums.DataType.PAGE.getCode()))
                            .toList());

                    return item;
                })
                .toList();
    }

    public List<String> getParentNameUntilRoot(List<String> authorityNames, int parentId) {
        if (parentId == 0) {
            return authorityNames;
        }
        Authority authority = this.getById(parentId);
        if (authority == null) {
            return authorityNames;
        }

        authorityNames.addFirst(authority.getAuthorityName());

        if (authority.getParentId() != 0) {
            return this.getParentNameUntilRoot(authorityNames, authority.getParentId());
        }

        return authorityNames;
    }

    @Override
    public List<Tree<Integer>> getAllAuthority(Integer type, String requestAdminType) {
        // 获取当前用户的权限列表
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(authentication, () -> new GlobalException(USER_NOT_LOGGED_IN));

        // 从缓存中获取用户信息
        String adminUserCacheKey = ADMIN_USER + authentication.getName();
        AdminUser adminUser = redisCache.getCacheObject(adminUserCacheKey);
        Assert.notNull(adminUser, () -> new GlobalException(USER_NOT_LOGGED_IN));

        // 获取当前用户的权限
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        // 设置当前账户是后台账户或者前台账户的查询条件
        String adminType = HeaderUtils.getAdminType();
        if (AdminTypeEnum.fromCode(adminType) == AdminTypeEnum.ADMIN && ObjectUtil.isNotEmpty(requestAdminType)) {
            adminType = requestAdminType;
        }
        if (ObjectUtil.equals(adminType, "suppliers")) {
            adminType = "admin";
        } else if (ObjectUtil.equals(adminType, "shop") || ObjectUtil.equals(adminType, "store") || ObjectUtil.equals(adminType, "pickup")) {
            Integer shopId = HeaderUtils.getShopId();
            if (shopId != null && shopId != 0) {
                AdminUserShop adminUserShop = adminUserShopMapper.selectOne(
                        Wrappers.lambdaQuery(AdminUserShop.class).eq(AdminUserShop::getAdminId, adminUser.getAdminId()).eq(AdminUserShop::getShopId, shopId)
                );
                if (adminUserShop != null) {
                    if (adminUserShop.getRoleId() != null && adminUserShop.getRoleId() > 0) {
                        AdminRole role = adminRoleService.getById(adminUserShop.getRoleId());
                        if (role != null) {
                            authorities = JSONUtil.toList(role.getAuthorityList(), String.class);
                        }
                    } else {
                        authorities = JSONArray.parseArray(adminUserShop.getAuthList(), String.class);
                    }
                }
            }
        } else if (ObjectUtil.equals(adminType, "vendor")) {
            Integer vendorId = HeaderUtils.getVendorId();
            if (vendorId != null && vendorId != 0) {
                AdminUserVendor adminUserVendor = adminUserVendorMapper.selectOne(
                        Wrappers.lambdaQuery(AdminUserVendor.class).eq(AdminUserVendor::getAdminId, adminUser.getAdminId()).eq(AdminUserVendor::getVendorId, vendorId)
                );
                if (adminUserVendor != null) {
                    if (adminUserVendor.getRoleId() != null && adminUserVendor.getRoleId() > 0) {
                        AdminRole role = adminRoleService.getById(adminUserVendor.getRoleId());
                        if (role != null) {
                            authorities = JSONUtil.toList(role.getAuthorityList(), String.class);
                        }
                    } else {
                        authorities = JSONArray.parseArray(adminUserVendor.getAuthList(), String.class);
                    }
                }
            }
        }


        // 检查是否包含 "all" 权限
        boolean hasAllAuthority = CollUtil.isEmpty(authorities) || authorities.stream().anyMatch("all"::equals);

        List<Authority> authorityList = this.lambdaQuery()
                .eq(Authority::getAdminType, adminType)
                .in(!hasAllAuthority, Authority::getAuthoritySn, authorities)
                .eq(Authority::getIsShow, 1)
                .orderByAsc(Authority::getParentId, Authority::getSortOrder, Authority::getAuthorityId)
                .list();

        if (tigshopProperties.getIsOverseas() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "multilingual")).findFirst().ifPresent(authorityList::remove);
            authorityList.removeIf(item -> "mobileuserUserOverseasManage".equals(item.getAuthoritySn()));
            authorityList.removeIf(item -> "taxRateManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsOverseas() == 1) {
            authorityList.removeIf(item -> "mobileUserManage".equals(item.getAuthoritySn()));
//            authorityList.removeIf(item -> item.getAuthoritySn().contains("grouponPromotionManage"));
        }
        if (tigshopProperties.getIsMerchant() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shop")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopManagement")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopSettingsManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsPro() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "salesman")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "eCardManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userLevelProManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsPro() == 1) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "levelManageManage")).findFirst().ifPresent(authorityList::remove);
        }
        // o2o,跨境,b2b,供应商 存在的话,就显示,就不移除
        if (!(tigshopProperties.getIsO2o() == 1 || tigshopProperties.getIsOverseas() == 1 || tigshopProperties.getIsVendor() == 1
                || tigshopProperties.getIsB2b() == 1)) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "enquiryManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "quotationManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsB2b() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userCertificationManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "realNameAuthentication")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userAuthenticationManage")).findFirst().ifPresent(authorityList::remove);

            authorityList.removeIf(item -> item != null && TERM_PAY_MENU_SN.contains(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsVendor() == 0) {
            authorityList.removeIf(item -> item.getAuthoritySn().contains("vendor"));
        }
        if (tigshopProperties.getIsO2o() == 0) {
            authorityList.removeIf(item -> item.getAuthoritySn().contains("store"));
            // 进店规则为 o2o 专属栏目，非 o2o 版本移除
            authorityList.removeIf(item -> "storeEntryRuleSetting".equals(item.getAuthoritySn()));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("intraCityDeliveryManage"));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("StoreDecorate"));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("staffFundManagement"));
            //移除对应的配送功能
            authorityList.removeIf(item -> item.getAuthoritySn().contains("deliverymanList"));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("servicemanList"));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("homeServiceManage"));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("cycleBuyManage"));
        }
        filterReceiptPrintMenuByEdition(authorityList, tigshopProperties.getIsO2o() == 1);
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0 && tigshopProperties.getIsVendor() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "organize")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "adminMerchant")).findFirst().ifPresent(authorityList::remove);

            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "profitsharingManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.removeIf(item -> "shopUserfundsManagement".equals(item.getAuthoritySn()));
        }
        // 批发菜单由 VORT common.wholesale 编译开关控制，关闭时移除数据库中的批发菜单
        if (!isWholesaleMenuEnabled()) {
            authorityList.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().contains("wholesale"));
        }
        removeSupplyChainMenus(authorityList);

        return convertTreeList(authorityList, type);
    }

    @Override
    public List<Authority> getAuthority() {
        // 获取当前用户的权限列表
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(authentication, () -> new GlobalException(USER_NOT_LOGGED_IN));

        // 从缓存中获取用户信息
        String adminUserCacheKey = ADMIN_USER + authentication.getName();
        AdminUser adminUser = redisCache.getCacheObject(adminUserCacheKey);
        Assert.notNull(adminUser, () -> new GlobalException(USER_NOT_LOGGED_IN));

        // 获取当前用户的权限
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        // 设置当前账户是后台账户或者前台账户的查询条件
        String adminType = adminUser.getAdminType();
        if (ObjectUtil.equals(adminType, "suppliers")) {
            adminType = "admin";
        }
        Integer shopId = HeaderUtils.getShopId();
        if (shopId != null && shopId != 0) {
            adminType = "shop";
            AdminUserShop adminUserShop = adminUserShopMapper.selectOne(
                    Wrappers.lambdaQuery(AdminUserShop.class).eq(AdminUserShop::getAdminId, adminUser.getAdminId()).eq(AdminUserShop::getShopId, shopId)
            );
            if (adminUserShop != null) {
                if (adminUserShop.getRoleId() != null && adminUserShop.getRoleId() > 0) {
                    AdminRole role = adminRoleService.getById(adminUserShop.getRoleId());
                    if (role != null) {
                        authorities = JSONUtil.toList(role.getAuthorityList(), String.class);
                    }
                } else {
                    authorities = JSONArray.parseArray(adminUserShop.getAuthList(), String.class);
                }
            }
        }

        Integer vendorId = HeaderUtils.getVendorId();
        if (vendorId != null && vendorId != 0) {
            adminType = "vendor";
            AdminUserVendor adminUserVendor = adminUserVendorMapper.selectOne(
                    Wrappers.lambdaQuery(AdminUserVendor.class).eq(AdminUserVendor::getAdminId, adminUser.getAdminId()).eq(AdminUserVendor::getVendorId, vendorId)
            );
            if (adminUserVendor != null) {
                if (adminUserVendor.getRoleId() != null && adminUserVendor.getRoleId() > 0) {
                    AdminRole role = adminRoleService.getById(adminUserVendor.getRoleId());
                    if (role != null) {
                        authorities = JSONUtil.toList(role.getAuthorityList(), String.class);
                    }
                } else {
                    authorities = JSONArray.parseArray(adminUserVendor.getAuthList(), String.class);
                }
            }
        }


        // 检查是否包含 "all" 权限
        boolean hasAllAuthority = authorities.stream().anyMatch("all"::equals);

        if (ObjectUtil.isEmpty(authorities)) {
            return new ArrayList<>();
        }

        List<Authority> authorityList = this.lambdaQuery()
                .eq(Authority::getAdminType, getAdminType())
                .in(!hasAllAuthority, Authority::getAuthoritySn, authorities)
                .eq(Authority::getIsShow, 1)
                .orderBy(true, true, Authority::getParentId)
                .orderBy(true, false, Authority::getSortOrder)
                .orderBy(true, true, Authority::getAuthorityId)
                .list();

        if (tigshopProperties.getIsOverseas() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "multilingual")).findFirst().ifPresent(authorityList::remove);
            authorityList.removeIf(item -> "mobileuserUserOverseasManage".equals(item.getAuthoritySn()));
            authorityList.removeIf(item -> "taxRateManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsOverseas() == 1) {
//            authorityList.removeIf(item -> item.getAuthoritySn().contains("grouponPromotionManage"));
        }
        if (tigshopProperties.getIsMerchant() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shop")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopManagement")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "shopSettingsManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsPro() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "salesman")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "eCardManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userLevelProManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsPro() == 1) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "levelManageManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (!(tigshopProperties.getIsO2o() == 1 || tigshopProperties.getIsOverseas() == 1 || tigshopProperties.getIsVendor() == 1
                || tigshopProperties.getIsB2b() == 1)) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "enquiryManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "quotationManage")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsB2b() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userCertificationManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "realNameAuthentication")).findFirst().ifPresent(authorityList::remove);
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "userAuthenticationManage")).findFirst().ifPresent(authorityList::remove);

            //兼容，有些环境他有多个
            authorityList.removeIf(item -> item != null && TERM_PAY_MENU_SN.contains(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsVendor() == 0) {
            authorityList.removeIf(item -> item.getAuthoritySn().contains("vendor"));
        }
        if (tigshopProperties.getIsO2o() == 0) {
            authorityList.removeIf(item -> item.getAuthoritySn().contains("store"));
            // 进店规则为 o2o 专属栏目，非 o2o 版本移除
            authorityList.removeIf(item -> "storeEntryRuleSetting".equals(item.getAuthoritySn()));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("intraCityDeliveryManage"));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("StoreDecorate"));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("staffFundManagement"));
            //
            authorityList.removeIf(item -> item.getAuthoritySn().contains("deliverymanList"));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("servicemanList"));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("homeServiceManage"));
            authorityList.removeIf(item -> item.getAuthoritySn().contains("cycleBuyManage"));
        }
        filterReceiptPrintMenuByEdition(authorityList, tigshopProperties.getIsO2o() == 1);
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0 && tigshopProperties.getIsVendor() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "organize")).findFirst().ifPresent(authorityList::remove);
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "adminMerchant")).findFirst().ifPresent(authorityList::remove);

            authorityList.stream().filter(item -> Objects.equals(item.getAuthoritySn(), "profitsharingManage")).findFirst().ifPresent(authorityList::remove);
            authorityList.removeIf(item -> "shopUserfundsManagement".equals(item.getAuthoritySn()));
        }
        // 批发菜单由 VORT common.wholesale 编译开关控制，关闭时移除数据库中的批发菜单
        if (!isWholesaleMenuEnabled()) {
            authorityList.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().contains("wholesale"));
        }
        removeSupplyChainMenus(authorityList);

        if (tigshopProperties.getIsO2o() == 1) {
            authorityList.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().toLowerCase().contains("deposit"));

            authorityList.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().toLowerCase().contains("Deposit"));
        }

        if (tigshopProperties.getIsMerchant() == 0) {
            authorityList.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().toLowerCase().contains("deposit"));

            authorityList.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().toLowerCase().contains("Deposit"));
        }

        // 平台「店铺会员总开关」关闭时，店铺端/门店端隐藏会员相关菜单：
        // 顶级「会员」(user) + 「会员管理」父级(userManagement) + 客户管理(shopMemberCustomerManage) + 会员标签(memberTagManage)。
        // 平台端(shopId=0)不受影响；其 admin_type=admin 的同名菜单不在店铺/门店端列表中，无误伤。
        Integer contextShopId = getShopId();
        if (contextShopId != null && contextShopId > 0 && !shopMemberSettingService.isEnabled()) {
            List<String> shopMemberMenuSn = List.of("user", "userManagement", "shopMemberCustomerManage", "memberTagManage");
            authorityList.removeIf(item -> item.getAuthoritySn() != null
                    && shopMemberMenuSn.contains(item.getAuthoritySn()));
        }

        return authorityList;
    }

    @Override
    public Page<AuthorityDTO> list(AuthorityListDTO dto) {
        //分页参数
        Page<Authority> page = buildSortOrder(dto);

        // 分页查询
        Page<Authority> authorityPage = this.lambdaQuery()
                .eq(StrUtil.isNotBlank(dto.getAdminType()), Authority::getAdminType, dto.getAdminType())
                .eq(dto.getParentId() != null && dto.getParentId() >= 0, Authority::getParentId, dto.getParentId())
                .page(page);

        // 查询子菜单信息
        List<Authority> records = authorityPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new Page<>();
        }

        List<Integer> authorityIds = records.stream().map(Authority::getAuthorityId).toList();
        List<Authority> childrenAuthorities = this.lambdaQuery().in(Authority::getParentId, authorityIds).list();

        List<AuthorityDTO> authorityList = new ArrayList<>();
        if (tigshopProperties.getIsOverseas() == 0) {
            records.removeIf(item -> "multilingual".equals(item.getAuthoritySn()));
            records.removeIf(item -> "mobileuserUserOverseasManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "taxRateManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsOverseas() == 1) {
//            records.removeIf(item -> item.getAuthoritySn().contains("grouponPromotionManage"));
        }
        if (tigshopProperties.getIsMerchant() == 0) {
            records.removeIf(item -> "shop".equals(item.getAuthoritySn()));
            records.removeIf(item -> "shopManagement".equals(item.getAuthoritySn()));
            records.removeIf(item -> "shopManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "shopSettingsManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsPro() == 0) {
            records.removeIf(item -> "salesman".equals(item.getAuthoritySn()));
            records.removeIf(item -> "eCardManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "userLevelProManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsPro() == 1) {
            records.removeIf(item -> "levelManageManage".equals(item.getAuthoritySn()));
        }
        if (!(tigshopProperties.getIsO2o() == 1 || tigshopProperties.getIsOverseas() == 1 || tigshopProperties.getIsVendor() == 1
                || tigshopProperties.getIsB2b() == 1)) {
            records.removeIf(item -> "enquiryManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "quotationManage".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsB2b() == 0) {
            records.removeIf(item -> "userCertificationManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "realNameAuthentication".equals(item.getAuthoritySn()));
            records.removeIf(item -> "userAuthenticationManage".equals(item.getAuthoritySn()));

            records.removeIf(item -> item != null && TERM_PAY_MENU_SN.contains(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsVendor() == 0) {
            records.removeIf(item -> item.getAuthoritySn().contains("vendor"));
        }
        if (tigshopProperties.getIsO2o() == 0) {
            records.removeIf(item -> item.getAuthoritySn().contains("store"));
            // 进店规则为 o2o 专属栏目，非 o2o 版本移除
            records.removeIf(item -> "storeEntryRuleSetting".equals(item.getAuthoritySn()));
            records.removeIf(item -> item.getAuthoritySn().contains("intraCityDeliveryManage"));
            records.removeIf(item -> item.getAuthoritySn().contains("staffFundManagement"));
            //
            records.removeIf(item -> item.getAuthoritySn().contains("deliverymanList"));
            records.removeIf(item -> item.getAuthoritySn().contains("servicemanList"));
            records.removeIf(item -> item.getAuthoritySn().contains("homeServiceManage"));
        }
        filterReceiptPrintMenuByEdition(records, tigshopProperties.getIsO2o() == 1);
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0 && tigshopProperties.getIsVendor() == 0) {
            records.removeIf(item -> "organize".equals(item.getAuthoritySn()));
        }
        if (tigshopProperties.getIsMerchant() == 0 && tigshopProperties.getIsO2o() == 0) {
            records.removeIf(item -> "adminMerchant".equals(item.getAuthoritySn()));

            records.removeIf(item -> "profitsharingManage".equals(item.getAuthoritySn()));
            records.removeIf(item -> "shopUserfundsManagement".equals(item.getAuthoritySn()));
        }
        // 批发菜单由 VORT common.wholesale 编译开关控制，关闭时移除数据库中的批发菜单
        if (!isWholesaleMenuEnabled()) {
            records.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().contains("wholesale"));
        }
        removeSupplyChainMenus(records);

        if (tigshopProperties.getIsO2o() == 1) {
            records.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().contains("deposit"));
            records.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().contains("Deposit"));
        }

        if (tigshopProperties.getIsMerchant() == 0) {
            records.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().contains("deposit"));
            records.removeIf(item -> item.getAuthoritySn() != null
                    && item.getAuthoritySn().contains("Deposit"));
        }

        for (Authority authority : records) {
            AuthorityDTO authorityDTO = buildChildAuth(authority);
            long hasChildren = childrenAuthorities.stream().filter(children -> authority.getAuthorityId().equals(children.getParentId())).count();
            authorityDTO.setHasChildren(hasChildren);
            authorityList.add(authorityDTO);
        }
        // 构建返回结果
        return PageUtil.convertPage(authorityPage, authorityList);
    }

    /**
     * 是否展示批发管理菜单。
     *
     * <p>批发代码由 VORT common.wholesale 编译开关控制。关闭该 feature 时，VORT
     * 会移除条件块，返回 false，避免数据库中残留的批发菜单继续展示。</p>
     */
    private boolean isWholesaleMenuEnabled() {
        // 演示站在 B2B、供应商（S2B2C）、跨境或多门店版本展示；其余版本隐藏。
        // 产品环境仍由 common.wholesale 编译开关决定，保留按产品选择批发能力。
        if (Objects.equals(tigshopProperties.getIsDemo(), 1)) {
            return Objects.equals(tigshopProperties.getIsB2b(), 1)
                    || Objects.equals(tigshopProperties.getIsVendor(), 1)
                    || Objects.equals(tigshopProperties.getIsOverseas(), 1)
                    || Objects.equals(tigshopProperties.getIsO2o(), 1);
        }

        boolean enabled = false;
        enabled = true;
        return enabled;
    }

    private void removeSupplyChainMenus(List<Authority> authorities) {
        if (isSupplyChainMenuEnabled()) {
            return;
        }
        authorities.removeIf(item -> item != null && SUPPLY_CHAIN_MENU_SN.contains(item.getAuthoritySn()));
    }

    /**
     * common.supplychain 编译进来时，演示站（is-demo=1）运行时不参与清单判断，只按演示版本收窄范围；
     * 产品环境（is-demo!=1）则由 common.supplychain 清单开关决定是否展示整组菜单。
     */
    private boolean isSupplyChainMenuEnabled() {

        boolean supplyChainCompiled = false;
        return supplyChainCompiled;
    }

    @Override
    public String getAuthorityParentName(Integer id) {
        // 获取父级id
        Authority authority = this.getOne(new LambdaQueryWrapper<Authority>()
                .eq(Authority::getAuthorityId, id)
                .select(Authority::getAuthorityName)
        );
        Integer parentId = authority.getParentId();
        String parentName = null;
        if (parentId != null && parentId > 0) {
            parentName = getParentName(parentId);
        }
        return parentName;
    }

    /**
     * 获取父级名称
     *
     * @param parentId 父级id
     * @return 父级名称
     */
    public String getParentName(int parentId) {
        Authority authority = this.getOne(new LambdaQueryWrapper<Authority>()
                .eq(Authority::getAuthorityId, parentId)
                .select(Authority::getAuthorityName)
        );
        return authority != null ? authority.getAuthorityName() : null;
    }

    /**
     * 获取所有权限sn
     *
     * @return List<String>
     */
    public List<String> getAuthoritySnList(List<String> authorities) {
        LambdaQueryWrapper<Authority> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 指定要查询的字段
        lambdaQueryWrapper.select(Authority::getAuthoritySn);

        //查询出所有的权限标识
        List<String> list = this.list(lambdaQueryWrapper)
                .stream()
                .map(Authority::getAuthoritySn)
                .toList();

        // 筛出权限并返回
        return authorities.stream()
                .filter(list::contains)
                .toList();
    }

    /**
     * 转换为树形结构
     *
     * @param authorityList 权限列表
     * @return List<Tree < String>>
     */
    public List<Tree<Integer>> convertTreeList(List<Authority> authorityList, Integer type) {
        if (authorityList.isEmpty()) {
            return null;
        }
        // 构建树形结构
        TreeNodeConfig config = new TreeNodeConfig();
        //默认id，可以不设置
        config.setIdKey("authorityId");
        //父id
        config.setParentIdKey("parentId");
        //分类名称
        config.setNameKey("authorityName");
        //排序
        config.setWeightKey("sortOrder");
        //最大递归深度
        config.setDeep(5);

        // 转树
        List<Tree<Integer>> result = TreeUtil.build(authorityList, 0, config, ((object, treeNode) -> {
            String childAuth = object.getChildAuth();
            List<ChildAuthVO> childAuthList = new ArrayList<>();
            if (!StrUtil.isEmpty(childAuth) && !"[]".equals(childAuth)) {
                childAuthList = JSONArray.parseArray(childAuth, ChildAuthVO.class);
            }
            treeNode.putExtra("authorityId", object.getAuthorityId());
            treeNode.putExtra("parentId", object.getParentId());
            treeNode.putExtra("authorityName", object.getAuthorityName());
            treeNode.putExtra("sortOrder", object.getSortOrder());
            treeNode.putExtra("isShow", object.getIsShow());
            treeNode.putExtra("childAuth", childAuthList);
            treeNode.putExtra("routeLink", object.getRouteLink());
            treeNode.putExtra("authorityIco", object.getAuthorityIco());
            treeNode.putExtra("authoritySn", object.getAuthoritySn());

        }));

        /*if (type != null && type == 1) {
            for (Tree<Integer> treeNode : result) {
                List<Tree<Integer>> allChildren = treeNode.getChildren();
                List<Tree<Integer>> deepChildren = this.deepChildren(treeNode.getChildren());
                allChildren.addAll(deepChildren);
                treeNode.setChildren(allChildren);
}
        }*/

                return result;
    }

private List<Tree<Integer>> deepChildren(List<Tree<Integer>> treeNodes) {
        List<Tree<Integer>> allChildren = new ArrayList<>();

        for (Tree<Integer> treeNode : treeNodes) {
            List<Tree<Integer>> children = treeNode.getChildren();
            if (CollUtil.isNotEmpty(children)) {
                // 先加入当前节点的直接子节点
                allChildren.addAll(children);
                // 再递归加入子节点的所有子孙节点
                allChildren.addAll(deepChildren(children));
            }
        }

        return allChildren;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDel(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 查询所有要删除的权限
        List<Authority> authorities = this.listByIds(ids);
        // 构建 ID -> Authority 映射
        Map<Integer, Authority> authorityMap = authorities.stream()
                .collect(Collectors.toMap(Authority::getAuthorityId, a -> a));
        // 按层级深度排序：深度大的（子类）排在前面先删除
        List<Integer> sortedIds = authorities.stream()
                .sorted((a1, a2) -> {
                    int depth1 = calculateDepth(a1, authorityMap);
                    int depth2 = calculateDepth(a2, authorityMap);
                    return Integer.compare(depth2, depth1);
                })
                .map(Authority::getAuthorityId)
                .toList();

        // 先检查所有权限是否可删除
        for (Integer id : sortedIds) {
            // 检查是否有子权限（不在本次删除列表中的子权限）
            Authority child = this.lambdaQuery()
                    .eq(Authority::getParentId, id)
                    .notIn(Authority::getAuthorityId, ids)
                    .last("limit 1").one();
            if (child != null) {
                Authority current = authorityMap.get(id);
                String authName = current != null ? current.getAuthorityName() : String.valueOf(id);
                throw new GlobalException(
                        String.format("权限[" + authName + "]下有子权限,无法删除"));
            }
        }
        // 全部检查通过，执行删除
        for (Integer id : sortedIds) {
            this.del(id);
        }
    }

    /**
     * 计算权限深度
     */
    private int calculateDepth(Authority authority, Map<Integer, Authority> authorityMap) {
        int depth = 0;
        Integer parentId = authority.getParentId();
        while (parentId != null && parentId != 0 && authorityMap.containsKey(parentId)) {
            depth++;
            parentId = authorityMap.get(parentId).getParentId();
        }
        return depth;
    }
}
