// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.security.admin.handler;

import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.authority.Authority;
import com.tigshop.service.authority.AuthorityService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.hutool.json.JSONUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.security.admin.user.AccountUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * 自定义默认权限
 *
 * @author Jayce
 * @create 2024年10月16日 13:04
 */
public class PermissionHandler {

    private final AuthorityService authorityService;

    public PermissionHandler(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * 判断接口是否有任意xxx，xxx权限
     *
     * @param permissions 权限
     * @return {boolean}
     */
    public boolean hasPermission(String... permissions) {

        if (permissions == null || permissions.length == 0) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // all 只展开为认证时确认的后台范围内的权限，不能作为全平台通配符。
        if (authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("all"::equals)) {
            return hasScopedAllPermission(authentication, permissions);
        }

        // 检查是否有传入的权限
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(StringUtils::hasText)
                .anyMatch(auth -> PatternMatchUtils.simpleMatch(permissions, auth));
    }

    private boolean hasScopedAllPermission(Authentication authentication, String[] permissions) {
        if (!(authentication.getPrincipal() instanceof AccountUser accountUser)
                || accountUser.getAdminId() == null || accountUser.getAdminId() <= 0
                || !(authentication.getDetails() instanceof AdminTypeEnum scope)) {
            return false;
        }
        // 工作台员工仍可使用精确授权，但不能通过请求头把店铺 all 升为平台 all。
        if (scope == AdminTypeEnum.ADMIN && !scope.getCode().equals(accountUser.getAdminType())) {
            return false;
        }
        for (Authority authority : authorityService.list(Wrappers.lambdaQuery(Authority.class)
                .eq(Authority::getAdminType, scope.getCode())
                .select(Authority::getAuthoritySn, Authority::getChildAuth))) {
            if (StringUtils.hasText(authority.getAuthoritySn())
                    && PatternMatchUtils.simpleMatch(permissions, authority.getAuthoritySn())) {
                return true;
            }
            if (!StringUtils.hasText(authority.getChildAuth())) {
                continue;
            }
            // 历史 SQL 使用 auth_sn，新菜单使用 authSn，两种格式都属于同一权限目录。
            for (var child : JSONUtil.parseArray(authority.getChildAuth()).jsonIter()) {
                String sn = child.getStr("authSn");
                if (!StringUtils.hasText(sn)) {
                    sn = child.getStr("auth_sn");
                }
                if (StringUtils.hasText(sn) && PatternMatchUtils.simpleMatch(permissions, sn)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断当前登录用户是否为平台管理后台的超级管理员。
     *
     * <p>平台（admin）与店铺、供应商等后台都会使用 {@code all} 权限，
     * 因此仅检查 {@code all} 不能区分平台管理员。这里同时校验认证主体
     * 中的后台类型，避免店铺等其他后台超级管理员读取平台 MCP 密钥。</p>
     */
    public boolean isPlatformSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication) || !authentication.isAuthenticated()
                || authentication.getDetails() != AdminTypeEnum.ADMIN
                || !(authentication.getPrincipal() instanceof AccountUser accountUser)) {
            return false;
        }
        if (!AdminTypeEnum.ADMIN.getCode().equals(accountUser.getAdminType())) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities != null && authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("all"::equals);
    }


    public boolean hasPermissionOrSelf(String permission, Integer targetUserId) {
        Integer currentUserId = SecurityUtils.getCurrentAdminId();
        return targetUserId.equals(currentUserId) || hasPermission(permission);
    }

}
