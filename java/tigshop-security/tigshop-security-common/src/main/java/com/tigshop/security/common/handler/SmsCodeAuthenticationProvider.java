// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.security.common.handler;

import com.tigshop.common.utils.RedisCache;
import com.tigshop.security.common.exception.SmsCodeAuthenticationException;
import com.tigshop.security.common.service.TigUserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Component
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    private final TigUserService userDetailsService;

    private final RedisCache redisCache;

    public SmsCodeAuthenticationProvider(TigUserService userDetailsService, RedisCache redisCache) {
        this.userDetailsService = userDetailsService;
        this.redisCache = redisCache;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        SmsCodeAuthenticationToken authenticationToken = (SmsCodeAuthenticationToken) authentication;

        String mobile = authenticationToken.getPrincipal().toString();
        String smsCode = authenticationToken.getCredentials().toString();

        if (!StringUtils.hasText(mobile) || !StringUtils.hasText(smsCode)) {
            throw new SmsCodeAuthenticationException("手机号或验证码不能为空");
        }
        Object mobileObj = redisCache.getCacheObject("login" + mobile);
        // 验证码校验（此处从 Redis 或数据库查询）
        if (mobileObj == null || !smsCode.equals(mobileObj.toString())) {
            throw new SmsCodeAuthenticationException("短信验证码错误或已过期，请重试");
        }

        // 根据手机号获取用户信息（UserDetailsService 需实现手机号查询用户）
        UserDetails userDetails = userDetailsService.loadUserByUserMobile(mobile);
        if (Objects.isNull(userDetails)) {
            throw new UsernameNotFoundException("用户信息不存在");
        }

        // 认证成功后，返回已认证的 `SmsCodeAuthenticationToken`
        return new SmsCodeAuthenticationToken(userDetails, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
