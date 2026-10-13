// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.security.admin.service;

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.security.admin.user.AccountUser;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.security.common.service.TigUserService;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.bean.model.authority.AdminUser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.tigshop.common.constant.login.LoginConstants.ADMIN_USER_PASSWORD_ERROR;

/**
 * 用户登录服务
 *
 * @author Tigshop团队
 * @create 2024年11月04日 17:16
 */
@Slf4j
@Service("userDetailsService")
public class UserDetailsServiceImpl implements TigUserService {

    @Resource
    private AdminUserService userService;

    @Resource
    private AdminLogService adminLogService;

    @Resource
    private MessageHelper messageHelper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser user = userService.getAdminUserByUsername(username, false,true);
        if (user == null) {
            throw new UsernameNotFoundException(ADMIN_USER_PASSWORD_ERROR);
        }
        return new AccountUser(user.getAdminId(), user.getUsername(), user.getPassword(), user.getAdminType(), getUserAuthority(user.getUsername()));
    }

    /**
     * 获取用户权限信息（角色、菜单权限）
     *
     * @param username 用户名
     * @return List<GrantedAuthority>
     */
    public Collection<? extends GrantedAuthority> getUserAuthority(String username) {
        // 获取权限
        AdminUser adminUser = userService.getAdminUserByUsername(username, true);
        if (adminUser == null) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList("");
        }

        // 从数据库获取用户权限
        String permissions = adminUser.getAuthList();
        // 转为List<String>
        List<String> authorityList = getAuthorityList(permissions);

        // 将权限拼接
        String authority = StrUtil.join(",", authorityList);

        return AuthorityUtils.commaSeparatedStringToAuthorityList(authority);
    }

    /**
     * 将权限字符串转换为List<String>
     *
     * @param permissions 权限字符串
     * @return List<String>
     */
    private List<String> getAuthorityList(String permissions) {
        if ("[]".equals(permissions)) {
            // 如果权限为空，返回空列表
            return Collections.emptyList();
        }
        // 将权限字符串转换为数组，并转为List<String>
        return Arrays.asList(StringUtils.str2Array(permissions));
    }

    /**
     * 保存登录日志
     *
     * @param adminUser 用户信息
     */
    public void saveAdminLog(AccountUser adminUser) {
        boolean isCreated = adminLogService.createByLogInfo(StrUtil.format("管理员登录:{}", adminUser.getUsername()));
        if (!isCreated) {
            log.error("记录登录日志失败");
        }
    }

    @Override
    public UserDetails loadUserByUserMobile(String mobile) throws UsernameNotFoundException {
        AdminUser user = userService.getAdminUserByUseMobile(mobile);
        if (user == null) {
            throw new UsernameNotFoundException(ADMIN_USER_PASSWORD_ERROR);
        }
        if(Objects.equals(1,user.getIsDeleted())){
            throw new GlobalException(messageHelper.get("error.account.deleted.cannot.login"));
        }
        return new AccountUser(user.getAdminId(), user.getUsername(), user.getPassword(), user.getAdminType(), getUserAuthority(user.getUsername()));
    }
}