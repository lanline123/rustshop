package com.tigshop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 快递100 实时查询接口客户端。
 * <p>文档：https://api.kuaidi100.com/document/5f0ffb5ebc8da837cbd8aefc.html</p>
 * <p>签名：MD5(param + key + customer) → 32 位大写。</p>
 */
@FeignClient(name = "kuaidi100-poll-api", url = "https://poll.kuaidi100.com")
public interface Kuaidi100PollClient {

    @PostMapping(value = "/poll/query.do", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String query(@RequestParam("customer") String customer,
                 @RequestParam("sign") String sign,
                 @RequestParam("param") String param);
}
