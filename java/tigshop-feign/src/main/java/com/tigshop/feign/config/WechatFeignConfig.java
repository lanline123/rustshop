// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.feign.config;

import feign.Request;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * 微信接口（wechat-api）专用 Feign 配置：限定连接/读取超时。
 *
 * <p>背景：海外站事故复盘 #4 —— WechatApiClient 默认无超时（Feign 默认 readTimeout 60s），
 * 微信网络偶尔抖动就会把 Tomcat 工作线程卡住 60s，叠加其它问题诱发雪崩。这里给它单独设
 * connect 3s / read 5s，快速失败、由上层（getMiniCode/getMiniAccessToken）降级返回 null。
 *
 * <p>注意：本类<b>不加</b> {@code @Configuration}，仅通过
 * {@code @FeignClient(configuration = WechatFeignConfig.class)} 注入到 wechat-api 的子上下文，
 * 避免成为所有 Feign 客户端的全局默认（其它如打印/AI 客户端可能需要更长读超时）。
 */
public class WechatFeignConfig {

    /** 连接超时（毫秒） */
    private static final long CONNECT_TIMEOUT_MS = 3000L;
    /** 读取超时（毫秒） */
    private static final long READ_TIMEOUT_MS = 5000L;

    @Bean
    public Request.Options wechatRequestOptions() {
        return new Request.Options(
                CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS,
                READ_TIMEOUT_MS, TimeUnit.MILLISECONDS,
                true);
    }
}
