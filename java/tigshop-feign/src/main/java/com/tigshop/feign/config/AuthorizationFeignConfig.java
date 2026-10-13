package com.tigshop.feign.config;

import feign.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/** 官网回源客户端专用超时配置，避免官网不可达拖住应用线程。 */
public class AuthorizationFeignConfig {

    @Bean
    public Request.Options authorizationRequestOptions(
            @Value("${tigshop.authorization.connect-timeout-ms:3000}") long connectTimeoutMs,
            @Value("${tigshop.authorization.read-timeout-ms:5000}") long readTimeoutMs) {
        return new Request.Options(connectTimeoutMs, TimeUnit.MILLISECONDS,
                readTimeoutMs, TimeUnit.MILLISECONDS, true);
    }
}
