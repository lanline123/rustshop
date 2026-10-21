package com.tigshop.adminapi.mcp;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;

/**
 * 为 WorkBuddy 开放接口调用旧后台服务时提供平台管理员请求上下文。
 *
 * <p>WorkBuddy 使用独立 API 密钥鉴权，不携带后台登录态的 {@code X-ADMIN-TYPE}。
 * 商品服务仍通过 {@code HeaderUtils} 读取该头，因此在调用期间仅对当前请求包装出
 * {@code X-ADMIN-TYPE=admin}，避免把 MCP 参数泄漏到业务方法或全局线程状态。
 */
public final class WorkBuddyRequestContext {

    private static final String ADMIN_TYPE_HEADER = "x-admin-type";

    private WorkBuddyRequestContext() {
    }

    public static <T> T runAsPlatformAdmin(HttpServletRequest request, Supplier<T> action) {
        RequestAttributes previous = RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new PlatformRequest(request)));
        try {
            return action.get();
        } finally {
            RequestContextHolder.setRequestAttributes(previous);
        }
    }

    private static final class PlatformRequest extends HttpServletRequestWrapper {
        private PlatformRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            return ADMIN_TYPE_HEADER.equalsIgnoreCase(name) ? "admin" : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (!ADMIN_TYPE_HEADER.equalsIgnoreCase(name)) {
                return super.getHeaders(name);
            }
            return Collections.enumeration(List.of("admin"));
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Enumeration<String> original = super.getHeaderNames();
            List<String> names = original == null ? new ArrayList<>() : Collections.list(original);
            if (names.stream().noneMatch(ADMIN_TYPE_HEADER::equalsIgnoreCase)) {
                names = new ArrayList<>(names);
                names.add("X-ADMIN-TYPE");
            }
            return Collections.enumeration(names);
        }
    }
}
