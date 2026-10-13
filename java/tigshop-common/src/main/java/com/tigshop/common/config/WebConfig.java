//**---------------------------------------------------------------------+
//** 配置文件 -- web
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+
package com.tigshop.common.config;

import com.tigshop.common.interceptor.HeaderInterceptor;
import com.tigshop.common.interceptor.TimeCostInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Tigshop团队
 * @create 2024/9/26 17:12
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TimeCostInterceptor timeCostInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(new HeaderInterceptor())
                // 拦截所有路径
                .addPathPatterns("/**");
        // 接口耗时日志拦截器（tigshop.is-debug=1 时生效）
        registry.addInterceptor(timeCostInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //指定允许跨域请求的路径模式为"/**"，表示所有的路径都将允许跨域访问。
        registry.addMapping("/**")
                // 允许访问资源的域名
                .allowedOriginPatterns("*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("Authorization", "*")
                // 是否允许发送凭证信息
                .allowCredentials(true)
                // 预检请求的有效期
                .maxAge(3600);
    }
}