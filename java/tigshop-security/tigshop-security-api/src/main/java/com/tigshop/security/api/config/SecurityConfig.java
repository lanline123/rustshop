// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.security.api.config;

import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.security.api.filter.ClientLoginFilter;
import com.tigshop.security.api.filter.InternalApiFilter;
import com.tigshop.security.api.filter.JwtAuthenticationFilter;
import com.tigshop.security.api.filter.LanguageFilter;
import com.tigshop.security.common.handler.SmsCodeAuthenticationProvider;
import com.tigshop.security.api.handler.UsernameClientAuthenticationProvider;
import com.tigshop.security.common.handler.CaptchaHandler;
import com.tigshop.security.common.handler.JwtAccessDeniedHandler;
import com.tigshop.security.common.handler.JwtAuthenticationEntryPoint;
import com.tigshop.security.common.utils.JwtUtil;
import com.tigshop.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.tigshop.security.api.config.WhiteListConfig.URL_WHITELIST;
import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security配置类
 *
 * @author Tigshop团队
 * @create 2024年11月15日 13:28
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final UsernameClientAuthenticationProvider usernameClientAuthenticationProvider;

    private final SmsCodeAuthenticationProvider smsCodeAuthenticationProvider;

    private final CaptchaHandler captchaHandler;

    private final RedisCache redisCache;

    private final JwtUtil jwtUtil;

    private final UserService userService;

    private final TranslatePackage translatePackage;

    private final LanguageFilter languageFilter;

    private final InternalApiFilter internalApiFilter;

    /**
     * 基于用户名和密码或使用用户名和密码进行身份验证
     *
     * @param http 设置
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(usernameClientAuthenticationProvider)
                .authenticationProvider(smsCodeAuthenticationProvider)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                // 禁用csrf(防止跨站请求伪造攻击)
                .csrf(AbstractHttpConfigurer::disable)
                // 配置跨域
                .cors(withDefaults())
                // 使用无状态session，即不使用session缓存数据
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 身份认证过滤器
                .authenticationManager(authenticationManager(http))
                .authenticationProvider(usernameClientAuthenticationProvider)
                .authenticationProvider(smsCodeAuthenticationProvider)
                // 设置白名单
                .authorizeHttpRequests(auth -> auth.requestMatchers(URL_WHITELIST).permitAll().anyRequest().authenticated())
                // 异常处理器
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint).accessDeniedHandler(jwtAccessDeniedHandler))
                // 登录过滤器（替换 UsernamePasswordAuthenticationFilter）
                .addFilterAt(
                        new ClientLoginFilter(
                                authenticationManager(http),
                                captchaHandler,
                                jwtUtil,
                                redisCache,
                                30,
                                userService,
                                translatePackage), UsernamePasswordAuthenticationFilter.class)
                /*
                 * 过滤器执行顺序（从上到下）：
                 * 1. InternalApiFilter - 内部 API 密钥验证
                 * 2. LanguageFilter - 语言设置
                 * 3. JwtAuthenticationFilter - JWT 校验
                 * 4. ClientLoginFilter - 登录处理
                 * 
                 * 注意：addFilterBefore 是后添加的在前面执行
                 */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(languageFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(internalApiFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}