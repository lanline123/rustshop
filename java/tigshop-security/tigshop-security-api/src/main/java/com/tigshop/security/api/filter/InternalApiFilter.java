// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.security.api.filter;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 内部 API 密钥验证过滤器
 * 用于保护内部服务间调用的接口，防止外部滥用
 *
 * @author Tigshop团队
 */
@Slf4j
@Component
public class InternalApiFilter extends OncePerRequestFilter {

    /**
     * 内部 API 请求头名称
     */
    public static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    /**
     * 需要内部 API 密钥验证的接口路径
     */
    private static final String[] INTERNAL_API_PATHS = {
            "/api/socket/send",
            "/api/socket/broadcast"
    };

    /**
     * 内部 API 密钥，从配置文件读取
     */
    @Value("${internal.api.key:}")
    private String internalApiKey;

    private final PathMatcher pathMatcher = new AntPathMatcher();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 检查是否是需要内部 API 密钥验证的路径
        boolean isInternalApiPath = Arrays.stream(INTERNAL_API_PATHS)
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));

        if (isInternalApiPath) {
            // 检查是否配置了内部 API 密钥
            if (StrUtil.isBlank(internalApiKey)) {
                log.warn("内部 API 密钥未配置，接口 {} 被阻止访问", requestURI);
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "内部接口未正确配置");
                return;
            }

            // 验证请求头中的密钥
            String providedKey = request.getHeader(INTERNAL_API_KEY_HEADER);
            if (StrUtil.isBlank(providedKey)) {
                log.warn("请求缺少内部 API 密钥，接口 {} 被拒绝访问，IP: {}", requestURI, getClientIp(request));
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "缺少内部 API 密钥");
                return;
            }

            if (!internalApiKey.equals(providedKey)) {
                log.warn("内部 API 密钥验证失败，接口 {} 被拒绝访问，IP: {}", requestURI, getClientIp(request));
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "内部 API 密钥验证失败");
                return;
            }

            log.debug("内部 API 密钥验证通过，接口: {}", requestURI);
        }

        chain.doFilter(request, response);
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", status);
        errorBody.put("message", message);
        errorBody.put("success", false);

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

