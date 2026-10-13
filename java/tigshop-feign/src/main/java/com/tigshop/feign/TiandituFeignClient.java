package com.tigshop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 天地图服务端接口。
 * <p>
 * URL 硬编码在注解，与 {@link AmapFeignClient} 保持同一范式，不引入配置化 URL。
 * 天地图用 tk 鉴权，无需签名。
 * <p>
 * 三个接口的传参风格并不统一：{@code geocoder} 与 {@code v2/search} 把业务参数打包进
 * {@code postStr} JSON 串，{@code v2/administrative} 则是平铺查询参数。
 *
 * @author Tigshop团队
 */
@FeignClient(name = "tiandituClient", url = "https://api.tianditu.gov.cn")
public interface TiandituFeignClient {

    /**
     * 逆地理编码。
     *
     * @param postStr {@code {"lon":x,"lat":y,"ver":1}}
     * @param type    固定 {@code geocode}
     */
    @GetMapping("/geocoder")
    Map<String, Object> geocoder(@RequestParam("postStr") String postStr,
                                 @RequestParam("type") String type,
                                 @RequestParam("tk") String tk);

    /**
     * 行政区划查询。
     *
     * @param keyword    行政区划名称或 9 位国标码
     * @param childLevel 下级行政区级数，0 为不返回
     * @param extensions 是否返回轮廓数据
     */
    @GetMapping("/v2/administrative")
    Map<String, Object> administrative(@RequestParam("keyword") String keyword,
                                       @RequestParam("childLevel") String childLevel,
                                       @RequestParam("extensions") String extensions,
                                       @RequestParam("tk") String tk);

    /**
     * 地名搜索。
     *
     * @param postStr 普通搜索必填 {@code keyWord}、{@code mapBound}、{@code level}、
     *                {@code queryType}、{@code start}、{@code count}
     * @param type    固定 {@code query}
     */
    @GetMapping("/v2/search")
    Map<String, Object> search(@RequestParam("postStr") String postStr,
                               @RequestParam("type") String type,
                               @RequestParam("tk") String tk);
}
