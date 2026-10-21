package com.tigshop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 快递100 电子面单 V2 客户端。
 * <p>文档：https://api.kuaidi100.com/document/dianzimiandanV2</p>
 * <p>签名：MD5(param + t + key + secret) → 32 位大写。</p>
 */
@FeignClient(name = "kuaidi100-label-api", url = "https://api.kuaidi100.com")
public interface Kuaidi100LabelClient {

    @PostMapping(value = "/label/order", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String order(@RequestParam("method") String method,
                 @RequestParam("key") String key,
                 @RequestParam("sign") String sign,
                 @RequestParam("t") String t,
                 @RequestParam("param") String param);
}
