// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.security.api.service;

import cn.hutool.core.lang.Assert;
import com.tigshop.bean.model.user.User;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.security.api.user.ClientUser;
import com.tigshop.security.common.service.TigUserService;
import com.tigshop.service.user.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.tigshop.common.constant.login.LoginConstants.USER_PASSWORD_ERROR;

/**
 * 客户端登录
 *
 * @author Tigshop团队
 * @create 2025年01月07日 14:23
 */
@Service
public class ClientUserServiceImpl implements TigUserService {
    @Resource
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByUsername(username);

        Assert.notNull(user, () -> new UsernameNotFoundException(USER_PASSWORD_ERROR));
        Assert.isFalse(Constants.NO.equals(user.getStatus()), () -> new GlobalException("您的账号已被禁用"));

        return new ClientUser(user.getUserId(), user.getUsername(), user.getPassword(), user.getMobile());
    }

    @Override
    public UserDetails loadUserByUserMobile(String mobile) throws UsernameNotFoundException {
        User user = userService.getUserByMobile(mobile);

        Assert.notNull(user, () -> new UsernameNotFoundException(USER_PASSWORD_ERROR));
        Assert.isFalse(Constants.NO.equals(user.getStatus()), () -> new GlobalException("您的账号已被禁用"));

        return new ClientUser(user.getUserId(), user.getUsername(), user.getPassword(), user.getMobile());
    }
}
