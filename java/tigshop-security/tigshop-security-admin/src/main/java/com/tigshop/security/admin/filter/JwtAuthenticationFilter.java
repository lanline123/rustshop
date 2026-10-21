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
package com.tigshop.security.admin.filter;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tigshop.common.constant.Constants;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.security.admin.service.UserDetailsServiceImpl;
import com.tigshop.security.common.utils.JwtUtil;
import com.tigshop.service.shop.ShopService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.DispatcherType;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.tigshop.common.constant.authority.AdminUserConstants.USER_DELETED;
import static com.tigshop.security.admin.config.SecurityConfig.URL_WHITELIST;

/**
 * @author Tigshop团队
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Resource
    private PathMatcher pathMatcher;

    @Resource
    private RedisCache redisCache;

    @Resource
    private ShopService shopService;

    @Resource
    private MessageHelper messageHelper;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // SSE 等异步 dispatch 由 SecurityConfig 的 dispatcherTypeMatchers(ASYNC).permitAll() 放行，
        // 此时原始请求头已不可用，跳过 JWT 解析避免误报 Access Denied
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            chain.doFilter(request, response);
            return;
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }
        String requestURI = request.getRequestURI();
        if (Arrays.stream(URL_WHITELIST).anyMatch(pattern -> pathMatcher.match(pattern, requestURI))) {
            chain.doFilter(request, response);
            return;
        }
        final String authorization = request.getHeader(JwtUtil.HEADER);

        // 如果请求头中没有token，不能访问需要权限的数据
        if (StrUtil.isBlankOrUndefined(authorization)) {
            chain.doFilter(request, response);
            return;
        }
        // 去掉 Bearer
        final String token = authorization.replaceAll(Constants.TOKEN_PREFIX, "");
        if (StrUtil.isBlankOrUndefined(token)) {
            chain.doFilter(request, response);
            return;
        }
        // 验证token
        Claims claims = jwtUtil.getClaimsByToken(token);
        if (claims == null) {
            throw new GlobalException("token异常");
        }
        if (jwtUtil.isTokenExpired(claims.getExpiration())) {
            throw new GlobalException("token已过期");
        }
        // 后台只接受 isAdmin=true 的 token，拒绝前台 token 跨端越权
        Boolean isAdmin = jwtUtil.getIsAdmin(claims);
        if (!Boolean.TRUE.equals(isAdmin)) {
            throw new GlobalException("token异常");
        }

            String username = claims.getSubject();
            // 判断redis中是否存在token（key为 ADMIN_TOKEN + username + ":" + token，支持同一用户多设备登录）
            Object redisValue = redisCache.getCacheObject(Constants.getLoginTokenFormat(token,username));
            Assert.notNull(redisValue, () -> new GlobalException("token已过期"));

        UserDetails userDetails = null;
        try{
            userDetails  =  userDetailsService.loadUserByUsername(username);
        }catch (GlobalException e){
            String message = e.getMessage();
            if(USER_DELETED.equals(message)){
                throw new GlobalException(messageHelper.get("error.account.deleted.cannot.login"));
            }
            throw e;
        }
        // 构建UsernamePasswordAuthenticationToken
        Integer userId = claims.get("userId", Integer.class);
        // 门店/自提点由 shop 请求查出的真实店铺类型决定，不接受客户端自行声明。
        String requestedType = request.getHeader(Constants.X_ADMIN_TYPE);
        String authorityType = StrUtil.isEmpty(requestedType) ? "admin" : requestedType;
        if (!List.of("admin", "shop", "vendor", "pos").contains(authorityType)) {
            throw new GlobalException("无效后台类型");
        }
        String shopOrVendorId = null;
        if (ObjectUtil.equals(authorityType, "shop")) {
            shopOrVendorId = Optional.ofNullable(HeaderUtils.getShopId())
                    .map(String::valueOf)
                    .orElse(null);
        } else if (ObjectUtil.equals(authorityType, "vendor")) {
            shopOrVendorId = Optional.ofNullable(HeaderUtils.getVendorId())
                    .map(String::valueOf)
                    .orElse(null);
        } else if (ObjectUtil.equals(authorityType, "pos")) {
            shopOrVendorId = Optional.ofNullable(HeaderUtils.getShopId())
                    .map(String::valueOf)
                    .orElse(null);
        }
        List<String> authorityList = List.of();
        if (!requestURI.equals("/adminapi/merchant/shop/myShop")) {
            boolean using = shopService.checkIsUsing(userId, shopOrVendorId, authorityType);
            if (!using) {
                throw new GlobalException("用户已经被禁用");
            }
            authorityList = shopService.checkAndReturnAuthList(userId, shopOrVendorId, authorityType);
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, userDetails.getPassword(), AuthorityUtils.createAuthorityList(authorityList));
        // shopService 已校验绑定并写入真实 shop_type，权限来源随认证保存。
        String shopType = HeaderUtils.getAttrValue(Constants.X_SHOP_TYPE);
        AdminTypeEnum permissionScope = "shop".equals(authorityType)
                ? AdminTypeEnum.fromCode(AdminTypeEnum.resolveCodeByShopType(
                        shopType == null ? null : Integer.valueOf(shopType)))
                : AdminTypeEnum.fromCode(authorityType);
        authentication.setDetails(permissionScope);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
}
