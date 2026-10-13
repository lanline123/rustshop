package com.tigshop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 微信开发平台远程接口
 *
 * @author kidd
 * @since 2025/4/23 14:29
 */
@FeignClient(name = "open-wechat-api", url = "https://open.weixin.qq.com")
public interface OpenWechatApiClient {

    /**
     * 微信登陆url
     */
    @PostMapping("/connect/qrconnect")
    String connectQrconnect(@RequestParam("appid") String appid,
                                   @RequestParam("redirect_uri") String redirectUri,
                                   @RequestParam(value = "response_type") String responseType,
                                   @RequestParam(value = "scope") String scope
                                   );
}
