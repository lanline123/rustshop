// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.security.api.filter;

import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONUtil;
import com.tigshop.bean.dto.login.LoginDTO;
import com.tigshop.bean.vo.login.LoginVO;
import com.tigshop.common.core.AjaxResult;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.ServletUtils;
import com.tigshop.security.api.user.ClientUser;
import com.tigshop.security.common.handler.CaptchaHandler;
import com.tigshop.security.common.handler.SmsCodeAuthenticationToken;
import com.tigshop.security.common.utils.JwtUtil;
import com.tigshop.service.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import com.tigshop.common.constant.login.LoginConstants;

import static com.tigshop.common.constant.Constants.*;
import static com.tigshop.common.constant.HttpStatus.LOGIN_CHECK;
import static com.tigshop.common.constant.HttpStatus.SERVICE_DATA_ERROR;

/**
 * 客户端登录
 *
 * @author Tigshop团队
 * @create 2025年01月07日 14:10
 */
@Slf4j
public class ClientLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final CaptchaHandler captchaHandler;

    private final JwtUtil jwtUtil;

    private final RedisCache redisCache;

    private final Integer expiration;

    private LoginDTO login;

    private final UserService userService;

    private final TranslatePackage translatePackage;

    public ClientLoginFilter(AuthenticationManager authenticationManager, CaptchaHandler captchaHandler,
                             JwtUtil jwtUtil, RedisCache redisCache, Integer expiration, UserService userService, TranslatePackage translatePackage) {
        this.translatePackage = translatePackage;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.redisCache = redisCache;
        this.expiration = expiration;
        this.captchaHandler = captchaHandler;
        super.setAuthenticationManager(authenticationManager);
        super.setPostOnly(true);
        super.setFilterProcessesUrl("/api/user/login/signin");
        super.setUsernameParameter("username");
        super.setPasswordParameter("password");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
//        String csrfToken = HeaderUtils.getHeaderValue(X_CSRF_TOKEN);
//        Object csrf = redisCache.getCacheObject(csrfToken);
//        if (csrf == null || StrUtil.isEmpty(csrfToken) || !csrfToken.equals(csrf.toString())){
////            throw new BadCredentialsException("");
//            log.info("csrf错误");
//        }
        // 数据是通过 RequestBody 传输
        try {
            login = JSONUtil.toBean(IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8), LoginDTO.class);

            // 先校验真人校验（验证码），再校验账号密码
            String username = "password".equals(login.getLoginType()) ? login.getUsername() : login.getMobile();
            if (!captchaHandler.captcha(username, login.getVerifyToken())) {
                throw new BadCredentialsException("请完成真人校验");
            }

            if ("password".equals(login.getLoginType())) {
                return super.getAuthenticationManager().authenticate(
                        new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())
                );
            }
            if ("mobile".equals(login.getLoginType())) {
                try {
                    return super.getAuthenticationManager().authenticate(
                            new SmsCodeAuthenticationToken(login.getMobile(), login.getMobileCode())
                    );
                } catch (UsernameNotFoundException e) {
                    // 没有账号
                    userService.loginByPhoneAndRegister(login.getMobile());
                    return super.getAuthenticationManager().authenticate(
                            new SmsCodeAuthenticationToken(login.getMobile(), login.getMobileCode())
                    );
                }

            }
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
        throw new BadCredentialsException("无效的登录类型");
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) {
        // 如果验证成功, 就生成Token并返回
        ClientUser user = (ClientUser) authResult.getPrincipal();
        String username = user.getUsername();
        String token = jwtUtil.generateToken(user.getUserId(), false, username);
        response.setHeader(JwtUtil.HEADER, TOKEN_PREFIX + token);
        // 将token存入Redis中，key为 ADMIN_TOKEN + username + ":" + token，支持同一用户多设备登录
        redisCache.setCacheObject(getLoginTokenFormat(token,username), username, expiration, TimeUnit.DAYS);
        // 返回给前端
        ServletUtils.render(response, AjaxResult.success(new LoginVO(token)));
        captchaHandler.resetErrorCount(username);
    }

    /**
     * 如果 attemptAuthentication 抛出 AuthenticationException 则会调用这个方法
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) {
        if (login != null) {
            String username = "password".equals(login.getLoginType()) ? login.getUsername() : login.getMobile();
            // 增加错误次数
            int errorCount = captchaHandler.incrementErrorCount(username);
            int responseCode;
            if("请完成真人校验".equals(failed.getMessage())){
                //真人验证的错误,弹出
                // 判断是否需要验证码：达到阈值则返回 LOGIN_CHECK，否则返回 SERVICE_DATA_ERROR
                responseCode= errorCount >= LoginConstants.ERROR_THRESHOLD ? LOGIN_CHECK : SERVICE_DATA_ERROR;
            }else{
                responseCode = SERVICE_DATA_ERROR;
            }
            ServletUtils.render(response, AjaxResult.error(responseCode, translatePackage.translate(failed.getMessage())));
        } else {
            ServletUtils.render(response, AjaxResult.error(translatePackage.translate(failed.getMessage())));
        }
    }
}
