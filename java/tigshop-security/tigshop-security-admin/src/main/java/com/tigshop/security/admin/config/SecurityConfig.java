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

package com.tigshop.security.admin.config;

import com.tigshop.common.utils.RedisCache;
import com.tigshop.security.admin.filter.ApiTokenAuthenticationFilter;
import com.tigshop.security.admin.filter.JwtAuthenticationFilter;
import com.tigshop.security.admin.filter.LoginFilter;
import com.tigshop.security.admin.handler.PermissionHandler;
import com.tigshop.security.admin.handler.UsernameAuthenticationProvider;
import com.tigshop.security.common.handler.CaptchaHandler;
import com.tigshop.security.common.handler.JwtAccessDeniedHandler;
import com.tigshop.security.common.handler.JwtAuthenticationEntryPoint;
import com.tigshop.security.common.handler.SmsCodeAuthenticationProvider;
import com.tigshop.security.common.utils.JwtUtil;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.authority.AuthorityService;
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

import jakarta.servlet.DispatcherType;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security配置类
 *
 * @author Tigshop团队
 * @create 2024年11月15日 13:28
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    public static final String[] URL_WHITELIST = {
            // 行为验证
            "/adminapi/login/**",
            "/adminapi/common/verification/**",
            "/adminapi/common/csrf/**",
            "/adminapi/setting/config/getAdmin",
            "/adminapi/setting/config/getLoginProtocol",
            "/adminapi/setting/config/getLoginProtocolContent",
            // 保证金到期扫描手动触发(测试/运维用，免登录免鉴权)
            "/adminapi/deposit/account/runDueCheck",
            "/doc.html",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-resources/**",
            "/favicon.ico",
            "/img/**",
            "/video/**",
            "/app/**",
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ApiTokenAuthenticationFilter apiTokenAuthenticationFilter;

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final UsernameAuthenticationProvider usernameAuthenticationProvider;

    private final SmsCodeAuthenticationProvider smsCodeAuthenticationProvider;

    private final AdminLogService adminLogService;

    private final CaptchaHandler captchaHandler;

    private final RedisCache redisCache;

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          ApiTokenAuthenticationFilter apiTokenAuthenticationFilter,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          UsernameAuthenticationProvider usernameAuthenticationProvider,
                          SmsCodeAuthenticationProvider smsCodeAuthenticationProvider,
                          AdminLogService adminLogService,
                          CaptchaHandler captchaHandler,
                          RedisCache redisCache, JwtUtil jwtUtil) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.apiTokenAuthenticationFilter = apiTokenAuthenticationFilter;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.usernameAuthenticationProvider = usernameAuthenticationProvider;
        this.smsCodeAuthenticationProvider = smsCodeAuthenticationProvider;
        this.adminLogService = adminLogService;
        this.captchaHandler = captchaHandler;
        this.redisCache = redisCache;
        this.jwtUtil = jwtUtil;
    }

    @Bean("pms")
    public PermissionHandler permissionService(AuthorityService authorityService) {
        return new PermissionHandler(authorityService);
    }

    /**
     * 基于用户名和密码或使用用户名和密码进行身份验证
     *
     * @param http 设置
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(usernameAuthenticationProvider)
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
                .authenticationProvider(usernameAuthenticationProvider)
                .authenticationProvider(smsCodeAuthenticationProvider)
                // 设置白名单
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        // MCP 通过专用 X-MCP-Api-Key 在 Controller 内完成 scope/店铺鉴权，不能要求 JWT/API Token。
                        .requestMatchers("/adminapi/openapi/mcp/**").permitAll()
                        // MCP 端点同理：密钥由传输层放入上下文，鉴权在工具内完成。
                        .requestMatchers("/adminapi/mcp").permitAll()
                        .requestMatchers(URL_WHITELIST).permitAll()
                        .anyRequest().authenticated())
                // 异常处理器
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint).accessDeniedHandler(jwtAccessDeniedHandler))
                .addFilterAt(
                        new LoginFilter(
                                authenticationManager(http),
                                captchaHandler,
                                jwtUtil,
                                redisCache,
                                30,
                                adminLogService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, LoginFilter.class)
                .addFilterBefore(apiTokenAuthenticationFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}
