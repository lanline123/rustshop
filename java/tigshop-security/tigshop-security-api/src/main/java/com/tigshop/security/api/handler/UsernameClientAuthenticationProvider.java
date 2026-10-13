// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.security.api.handler;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.tigshop.common.constant.login.LoginConstants.USER_PASSWORD_ERROR;

/**
 * 自定义认证
 *
 * @author Jayce
 * @create 2024年11月22日 09:52
 */
@Component
public class UsernameClientAuthenticationProvider implements AuthenticationProvider {
    private final UserDetailsService userService;

    private final PasswordEncoder passwordEncoder;

    public UsernameClientAuthenticationProvider(UserDetailsService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        // 获取用户输入的用户名和密码
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        try {
            UserDetails userDetails = userService.loadUserByUsername(username);
            boolean matches = passwordEncoder.matches(password, userDetails.getPassword());
            if (!matches) {
                throw new BadCredentialsException(USER_PASSWORD_ERROR);
            }
            return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        } catch (UsernameNotFoundException e) {
            // 用户不存在时也抛出 BadCredentialsException，避免被 ProviderManager 转换为默认的 "Bad credentials"
            throw new BadCredentialsException(USER_PASSWORD_ERROR);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
