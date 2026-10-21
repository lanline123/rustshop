package com.tigshop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "amapClient", url = "https://restapi.amap.com/v3")
public interface AmapFeignClient {

    // IP定位
    @GetMapping("/ip")
    Map<String, Object> getLocationByIp(@RequestParam("ip") String ip,
                                        @RequestParam("key") String key,
                                        @RequestParam("sig") String sig);

    // 地理编码（地址 -> 经纬度）
    @GetMapping("/geocode/regeo")
    Map<String, Object> regeo(@RequestParam("location") String location,
                              @RequestParam("key") String key,
                              @RequestParam("sig") String sig);

    @GetMapping("/config/district")
    Map<String, Object> district(@RequestParam("keywords") String keywords,
                                 @RequestParam("subdistrict") String subdistrict,
                                 @RequestParam("key") String key,
                                 @RequestParam("sig") String sig);

    // 逆地理编码详细信息查询
    @GetMapping("/geocode/regeo")
    Map<String, Object> regeoDetail(@RequestParam("location") String location,
                                    @RequestParam("key") String key,
                                    @RequestParam("sig") String sig,
                                    @RequestParam(value = "radius", required = false) String radius,
                                    @RequestParam(value = "extensions", required = false) String extensions,
                                    @RequestParam(value = "roadlevel", required = false) String roadlevel,
                                    @RequestParam(value = "poitype", required = false) String poitype);

    // 输入提示（关键字地址搜索）
    @GetMapping("/assistant/inputtips")
    Map<String, Object> inputTips(@RequestParam("keywords") String keywords,
                                  @RequestParam("key") String key,
                                  @RequestParam("sig") String sig,
                                  @RequestParam(value = "type", required = false) String type,
                                  @RequestParam(value = "location", required = false) String location,
                                  @RequestParam(value = "city", required = false) String city,
                                  @RequestParam(value = "datatype", required = false) String datatype);

}