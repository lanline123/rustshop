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

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.security.api.service.ClientUserServiceImpl;
import com.tigshop.security.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.tigshop.security.api.config.WhiteListConfig.URL_WHITELIST;

/**
 * @author Tigshop团队
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private ClientUserServiceImpl userDetailsService;

    @Resource
    private PathMatcher pathMatcher;

    @Resource
    private RedisCache redisCache;

    /**
     * 在白名单内，输入有效token 也进行登录
     * 在白名单内，输入无效token 不进行登录，但正常访问
     * 不在白名单内，需要校验 token
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        // 在白名单内 如果设置了有效的token也进行登录（部分接口不登录可以返回部分数据，登录后返回详细数据）
        final String authorization = request.getHeader(JwtUtil.HEADER);
        String token = null;
        if (!StrUtil.isBlankOrUndefined(authorization)) {
            token = authorization.replaceAll(Constants.TOKEN_PREFIX, "");
        }
        // 是否是有效的token
        boolean isNotValidToken = true;
        // 是否在白名单内
        boolean isWhite = Arrays.stream(URL_WHITELIST).anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
        if (!StrUtil.isBlankOrUndefined(authorization) && !StrUtil.isBlankOrUndefined(token)) {
            isNotValidToken = false;
        }

        // 如果在白名单内，并且token无效，则直接放行
        if (isNotValidToken && isWhite) {
            chain.doFilter(request, response);
            return;
        }

        // 如果请求头中没有token，不能访问需要权限的数据
        if (StrUtil.isBlankOrUndefined(authorization)) {
            chain.doFilter(request, response);
            return;
        }
        // 去掉 Bearer
        if (StrUtil.isBlankOrUndefined(token)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            // 验证token
            Claims claims = jwtUtil.getClaimsByToken(token);
            if (claims == null) {
                throw new GlobalException("token异常");
            }
            if (jwtUtil.isTokenExpired(claims.getExpiration())) {
                throw new GlobalException("token已过期");
            }
            // 前台只接受 isAdmin=false 的 token，拒绝后台 token 跨端越权
            Boolean isAdmin = jwtUtil.getIsAdmin(claims);
            if (!Boolean.FALSE.equals(isAdmin)) {
                throw new GlobalException("token异常");
            }

            String username = claims.getSubject();
            // 判断redis中是否存在token（key为 ADMIN_TOKEN + username + ":" + token，支持同一用户多设备登录）
            Object redisValue = redisCache.getCacheObject(Constants.getLoginTokenFormat(token,username));
            Assert.notNull(redisValue, () -> new GlobalException("token已过期"));

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // 构建UsernamePasswordAuthenticationToken
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            userDetails, userDetails.getPassword(), List.of()
                    )
            );
        } catch (GlobalException | UsernameNotFoundException e) {
            if (!isWhite) {
                log.info("用户输入token校验发现异常", e);
                throw e;
            }
        }

        chain.doFilter(request, response);
    }
}