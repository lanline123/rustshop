package com.tigshop.common.interceptor;

//**---------------------------------------------------------------------+
//** 拦截器文件 -- 请求头
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import com.tigshop.common.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
/**
 * @author Jayce
 * @create 2024/9/26 16:59
 */
@Slf4j
public class HeaderInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler){
        // 在处理请求之前捕获Header数据
        String headerValue = request.getHeader("x-locale-code");
        ThreadLocalUtil.setValue(headerValue);

        // 返回true继续处理请求，返回false则终止请求
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex){
        // 请求完成之后执行的操作
        ThreadLocalUtil.clear();
    }
}
