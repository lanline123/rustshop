/*
 * ---------------------------------------------------------------------+
 *  文件 --
 * ---------------------------------------------------------------------+
 *  版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
 * ---------------------------------------------------------------------+
 *  作者：Tigshop团队，yq@tigshop.com
 * ---------------------------------------------------------------------+
 *  提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
 * ---------------------------------------------------------------------+
 */

package com.tigshop.security.admin.user;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import java.util.Collection;

import static com.tigshop.common.constant.login.LoginConstants.ADMIN_USER_PASSWORD_ERROR;

/**
 * @author Tigshop团队
 */
@Slf4j
@Data
public class AccountUser implements UserDetails {

    private Integer adminId;
    private String password;
    private String username;
    private String adminType;

    private Collection<? extends GrantedAuthority> authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    // 必须提供无参构造器
    public AccountUser() {
    }

    public AccountUser(Integer userId, String username, String password, String adminType, Collection<? extends GrantedAuthority> authorities) {
        this(userId, username, password, adminType, true, true, true, true, authorities);
    }

    public AccountUser(Integer adminId, String username, String password, String adminType, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        Assert.isTrue(username != null && !username.isEmpty() && password != null, ADMIN_USER_PASSWORD_ERROR);
        this.adminId = adminId;
        this.username = username;
        this.password = password;
        this.adminType = adminType;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

}