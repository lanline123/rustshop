package com.tigshop.feign;

import com.tigshop.feign.config.AuthorizationFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 官网实例授权生命周期接口。
 *
 * <p>官网没有内部网关，实例直接通过 HTTPS 调用官网公开接口。请求签名由
 * Tigshop 服务层的 Feign {@code RequestInterceptor} 统一注入。</p>
 */
@FeignClient(name = "authorizationLifecycle", url = "${tigshop.authorization.base-url:https://api.tigshop.com}",
        configuration = AuthorizationFeignConfig.class)
public interface AuthorizationLifecycleApiClient {

    @PostMapping("/open-api/v1/installations/register")
    Map<String, Object> register(@RequestBody Map<String, Object> request);

    @PostMapping("/open-api/v1/installations/sync")
    Map<String, Object> sync(@RequestBody Map<String, Object> request);
}
