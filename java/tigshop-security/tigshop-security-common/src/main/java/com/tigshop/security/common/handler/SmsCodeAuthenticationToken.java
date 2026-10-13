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

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 短信验证码登录的token验证
 *
 * @author Tigshop团队
 * @create 2025年3月5日 13:28
 */
public class SmsCodeAuthenticationToken extends AbstractAuthenticationToken {

    // 这里存手机号
    private final Object principal;
    // 这里存验证码
    private final String credentials;

    // 未认证时的构造方法
    public SmsCodeAuthenticationToken(String phone, String smsCode) {
        super(null);
        this.principal = phone;
        this.credentials = smsCode;
        setAuthenticated(false);
    }

    // 认证成功后的构造方法
    public SmsCodeAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
