package com.tigshop.common.interceptor;

//**---------------------------------------------------------------------+
//** 拦截器文件 -- 接口耗时日志
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import com.tigshop.common.properties.TigshopProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 接口耗时日志拦截器：输出 接口 / 开始请求时间 / 结束请求时间 / 最终消耗时间。
 * 仅当 tigshop.is-debug=1（调试/演示站模式）时生效，默认关闭无性能开销。
 *
 * @author Tigshop团队
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeCostInterceptor implements HandlerInterceptor {

    /** 请求开始时间在 request 中的存放 key */
    private static final String START_TIME_ATTR = "TIME_COST_START";

    /** 时间格式（精确到毫秒） */
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private final TigshopProperties tigshopProperties;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (tigshopProperties.debugEnabled()) {
            request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        if (!tigshopProperties.debugEnabled()) {
            return;
        }
        Object startAttr = request.getAttribute(START_TIME_ATTR);
        if (!(startAttr instanceof Long startTime)) {
            return;
        }
        long endTime = System.currentTimeMillis();
        long cost = endTime - startTime;
        String api = request.getMethod() + " " + request.getRequestURI();
        log.info("[接口耗时] 接口={}, 开始时间={}, 结束时间={}, 耗时={}ms",
                api,
                TIME_FORMATTER.format(Instant.ofEpochMilli(startTime)),
                TIME_FORMATTER.format(Instant.ofEpochMilli(endTime)),
                cost);
    }
}
