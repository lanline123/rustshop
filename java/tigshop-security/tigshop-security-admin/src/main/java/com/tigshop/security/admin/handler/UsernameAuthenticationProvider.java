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

import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.security.admin.service.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.tigshop.common.constant.authority.AdminUserConstants.USER_DELETED;
import static com.tigshop.common.constant.login.LoginConstants.ADMIN_USER_PASSWORD_ERROR;

/**
 * 自定义认证
 *
 * @author Jayce
 * @create 2024年11月22日 09:52
 */
@Slf4j
@Component
public class UsernameAuthenticationProvider implements AuthenticationProvider {
    private final UserDetailsServiceImpl userDetailService;

    private final PasswordEncoder passwordEncoder;

    private final MessageHelper messageHelper;

    public UsernameAuthenticationProvider(UserDetailsServiceImpl userDetailService, PasswordEncoder passwordEncoder, MessageHelper messageHelper) {
        this.userDetailService = userDetailService;
        this.passwordEncoder = passwordEncoder;
        this.messageHelper = messageHelper;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 获取用户输入的用户名和密码
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserDetails userDetails = null;
        try{
            userDetails  =  userDetailService.loadUserByUsername(username);
        }catch (GlobalException e){
            String message = e.getMessage();
            if(USER_DELETED.equals(message)){
                throw new GlobalException(messageHelper.get("error.account.deleted.cannot.login"));
            }
            throw e;
        }
        boolean matches = passwordEncoder.matches(password, userDetails.getPassword());
        if(!matches){
            throw new GlobalException(ADMIN_USER_PASSWORD_ERROR);
        }
        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
