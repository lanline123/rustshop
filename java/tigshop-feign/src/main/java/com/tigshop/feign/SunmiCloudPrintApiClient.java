// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 商米云打印机 V2 OpenAPI 接口（直推模式）
 * <p>
 * 官方文档：<a href="https://docs.sunmi.com/zh-CN/cdixeghjk491/xffdeghjk524">开发对接 API 接口</a>
 * <p>
 * <b>公共 Header 契约（以 2026/04 云打印机 V2 文档为准）</b>：
 * <ul>
 *     <li>{@code Sunmi-Appid}：必填，appid</li>
 *     <li>{@code Sunmi-Timestamp}：必填，10 位 Unix 时间戳（秒）</li>
 *     <li>{@code Sunmi-Nonce}：必填，6 位随机数</li>
 *     <li>{@code Sunmi-Sign}：必填，{@code hmac256(json_body + appid + timestamp + nonce, appkey)}，hex 小写</li>
 *     <li>{@code Source}：必填，<b>固定值 {@code openapi}</b>（注意：与通用 sunmi-openapi Java SDK 不同，
 *         云打印机 V2 要求此 header 必须存在，否则返回 {@code 10071400 parameter error}）</li>
 *     <li>{@code Content-Type}：必填，{@code application/json;charset=utf-8}</li>
 * </ul>
 * <p>
 * 响应成功码为 {@code 10000}，不是通用 SDK 的 {@code 1}。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@FeignClient(name = "sunmi-cloud-print-api", url = "https://openapi.sunmi.com")
public interface SunmiCloudPrintApiClient {

    /**
     * 云打印机 V2 固定 Source 值。<b>不能省略</b>：缺失会被商米网关判定为 parameter error（10071400）。
     */
    String SOURCE_OPENAPI = "openapi";

    /**
     * 与商米文档原文对齐的 Content-Type（文档表格就写这个值，不带 charset）
     */
    String CONTENT_TYPE_JSON = "application/json";

    /**
     * 打印机绑定店铺
     * <pre>
     * body: {"sn":"...","shop_id":123}            // 有店铺：JSON number
     *   或: {"sn":"...","shop_id":""}             // 无店铺：按文档可传空字符串
     * </pre>
     */
    @PostMapping(value = "/v2/printer/open/open/device/bindShop",
            consumes = CONTENT_TYPE_JSON,
            produces = CONTENT_TYPE_JSON)
    String bindShop(
            @RequestHeader("Sunmi-Appid") String appid,
            @RequestHeader("Sunmi-Timestamp") String timestamp,
            @RequestHeader("Sunmi-Nonce") String nonce,
            @RequestHeader("Sunmi-Sign") String sign,
            @RequestHeader("Source") String source,
            @RequestBody String body);

    /**
     * 打印机解绑店铺
     * <pre>
     * body: {"sn":"...","shop_id":123}
     * </pre>
     */
    @PostMapping(value = "/v2/printer/open/open/device/unbindShop",
            consumes = CONTENT_TYPE_JSON,
            produces = CONTENT_TYPE_JSON)
    String unbindShop(
            @RequestHeader("Sunmi-Appid") String appid,
            @RequestHeader("Sunmi-Timestamp") String timestamp,
            @RequestHeader("Sunmi-Nonce") String nonce,
            @RequestHeader("Sunmi-Sign") String sign,
            @RequestHeader("Source") String source,
            @RequestBody String body);

    /**
     * 查询设备在线状态
     * <pre>
     * body: {"sn":"...","shop_id":123,"page_no":1,"page_size":20}
     * </pre>
     */
    @PostMapping(value = "/v2/printer/open/open/device/onlineStatus",
            consumes = CONTENT_TYPE_JSON,
            produces = CONTENT_TYPE_JSON)
    String onlineStatus(
            @RequestHeader("Sunmi-Appid") String appid,
            @RequestHeader("Sunmi-Timestamp") String timestamp,
            @RequestHeader("Sunmi-Nonce") String nonce,
            @RequestHeader("Sunmi-Sign") String sign,
            @RequestHeader("Source") String source,
            @RequestBody String body);

    /**
     * 推送订单详情给打印机（自动打印 / 手动补打 核心入口）。
     * <p>
     * <b>注意</b>：云打印机 V2 的字段名为 {@code trade_no / count}（与通用 OpenAPI 的 {@code order_id / copies} 不同）。
     * <pre>
     * body: {"sn":"...","trade_no":"...","order_type":1,"content":"<hex>","count":1}
     * </pre>
     */
    @PostMapping(value = "/v2/printer/open/open/device/pushContent",
            consumes = CONTENT_TYPE_JSON,
            produces = CONTENT_TYPE_JSON)
    String pushContent(
            @RequestHeader("Sunmi-Appid") String appid,
            @RequestHeader("Sunmi-Timestamp") String timestamp,
            @RequestHeader("Sunmi-Nonce") String nonce,
            @RequestHeader("Sunmi-Sign") String sign,
            @RequestHeader("Source") String source,
            @RequestBody String body);

    /**
     * 查询订单打印状态（按 trade_no）
     * <pre>
     * body: {"sn":"...","trade_no":"..."}
     * data: {"status":0|1|2,"printed_time":123456}
     * </pre>
     */
    @PostMapping(value = "/v2/printer/open/open/ticket/printStatus",
            consumes = CONTENT_TYPE_JSON,
            produces = CONTENT_TYPE_JSON)
    String printStatus(
            @RequestHeader("Sunmi-Appid") String appid,
            @RequestHeader("Sunmi-Timestamp") String timestamp,
            @RequestHeader("Sunmi-Nonce") String nonce,
            @RequestHeader("Sunmi-Sign") String sign,
            @RequestHeader("Source") String source,
            @RequestBody String body);
}
