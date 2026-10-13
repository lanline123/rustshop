package com.tigshop.security.admin.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tigshop.common.constant.Constants;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.security.admin.service.UserDetailsServiceImpl;
import com.tigshop.security.common.utils.JwtUtil;
import com.tigshop.service.authority.AdminApiTokenService;
import com.tigshop.service.shop.ShopService;
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
import java.util.regex.Pattern;

import static com.tigshop.common.constant.authority.AdminUserConstants.USER_DELETED;
import static com.tigshop.security.admin.config.SecurityConfig.URL_WHITELIST;

@Slf4j
@Component
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Pattern HEX32_PATTERN = Pattern.compile("^[0-9a-fA-F]{32}$");

    @Resource
    private AdminApiTokenService adminApiTokenService;

    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Resource
    private PathMatcher pathMatcher;

    @Resource
    private ShopService shopService;

    @Resource
    private MessageHelper messageHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            chain.doFilter(request, response);
            return;
        }
        String requestURI = request.getRequestURI();
        if (Arrays.stream(URL_WHITELIST).anyMatch(pattern -> pathMatcher.match(pattern, requestURI))) {
            chain.doFilter(request, response);
            return;
        }

        String apiToken = extractApiToken(request);
        if (apiToken == null) {
            chain.doFilter(request, response);
            return;
        }

        String username = adminApiTokenService.validateToken(apiToken);
        if (username == null) {
            boolean fromDedicatedHeader = StrUtil.isNotBlank(request.getHeader(Constants.X_API_TOKEN_HEADER));
            if (fromDedicatedHeader) {
                throw new GlobalException("API Token无效或已过期");
            }
            chain.doFilter(request, response);
            return;
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (GlobalException e) {
            if (USER_DELETED.equals(e.getMessage())) {
                throw new GlobalException(messageHelper.get("error.account.deleted.cannot.login"));
            }
            throw e;
        }

        Integer userId = ((com.tigshop.security.admin.user.AccountUser) userDetails).getAdminId();
        // 门店/自提点由 shop 请求查出的真实店铺类型决定，不接受客户端自行声明。
        String requestedType = request.getHeader(Constants.X_ADMIN_TYPE);
        String authorityType = StrUtil.isEmpty(requestedType) ? "admin" : requestedType;
        if (!List.of("admin", "shop", "vendor", "pos").contains(authorityType)) {
            throw new GlobalException("无效后台类型");
        }
        String shopOrVendorId = null;
        if (ObjectUtil.equals(authorityType, "shop")) {
            shopOrVendorId = Optional.ofNullable(HeaderUtils.getShopId())
                    .map(String::valueOf).orElse(null);
        } else if (ObjectUtil.equals(authorityType, "vendor")) {
            shopOrVendorId = Optional.ofNullable(HeaderUtils.getVendorId())
                    .map(String::valueOf).orElse(null);
        } else if (ObjectUtil.equals(authorityType, "pos")) {
            shopOrVendorId = Optional.ofNullable(HeaderUtils.getShopId())
                    .map(String::valueOf).orElse(null);
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

    /**
     * 从请求中提取API Token：
     * 1. 优先检查 X-Api-Token 头
     * 2. 其次检查 Authorization 头中的 Bearer 值，仅当值为32位十六进制（非JWT格式）时匹配
     */
    private String extractApiToken(HttpServletRequest request) {
        String dedicatedToken = request.getHeader(Constants.X_API_TOKEN_HEADER);
        if (StrUtil.isNotBlank(dedicatedToken)) {
            return dedicatedToken.trim();
        }

        String authorization = request.getHeader(JwtUtil.HEADER);
        if (StrUtil.isBlankOrUndefined(authorization)) {
            return null;
        }
        String token = authorization.replaceAll(Constants.TOKEN_PREFIX, "").trim();
        if (StrUtil.isBlank(token)) {
            return null;
        }
        if (!token.contains(".") && HEX32_PATTERN.matcher(token).matches()) {
            return token;
        }
        return null;
    }
}
