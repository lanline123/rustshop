package com.tigshop.security.api.filter;

import com.tigshop.common.utils.ThreadLocalUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LanguageFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 获取语言头
        String localeCode = request.getHeader("x-locale-code");
        ThreadLocalUtil.setValue(localeCode);

        try {
            // 继续后续过滤器链
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束清理 ThreadLocal
            ThreadLocalUtil.clear();
        }
    }
}
