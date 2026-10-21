// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * 商米云打印 OpenAPI 工具类
 * <p>
 * 覆盖商米开放平台（https://openapi.sunmi.com）的公共协议：
 * <ul>
 *     <li>HMAC-SHA256 签名算法：{@code hmac256(body + appid + timestamp + nonce, appkey)}</li>
 *     <li>10 位 Unix 时间戳（秒）</li>
 *     <li>6 位数字 nonce（云打印机 V2 文档原文要求，不同于通用 sunmi-openapi Java SDK 的 UUID）</li>
 * </ul>
 * 官方参考：<a href="https://docs.sunmi.com/zh-CN/cdixeghjk491/xffdeghjk524">开发对接 API 接口</a>。
 *
 * @author Tigshop团队
 * @since 2026/4/21
 */
@Slf4j
public class SunmiCloudPrintApiUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private static final SecureRandom RANDOM = new SecureRandom();

    private SunmiCloudPrintApiUtils() {
    }

    /**
     * 生成商米签名
     *
     * @param jsonBody  请求 JSON 字符串（需与最终发送的请求体完全一致）
     * @param appid     商米分配的 AppID
     * @param timestamp 10 位 Unix 时间戳（秒）
     * @param nonce     6 位随机串
     * @param appkey    商米分配的 AppKey（HMAC 密钥）
     * @return 小写 hex 签名
     */
    public static String generateSign(String jsonBody, String appid, String timestamp, String nonce, String appkey) {
        String content = (jsonBody == null ? "" : jsonBody) + appid + timestamp + nonce;
        return hmacSha256(content, appkey);
    }

    /**
     * 校验商米响应签名
     *
     * @param responseBody 响应体原文
     * @param appid        请求时使用的 AppID
     * @param respTimestamp 响应 header 中的 Sunmi-Timestamp
     * @param respNonce    响应 header 中的 Sunmi-Nonce
     * @param appkey       请求时使用的 AppKey
     * @param expectSign   响应 header 中的 Sunmi-Sign
     * @return 匹配为 true
     */
    public static boolean verifyResponse(String responseBody, String appid, String respTimestamp,
                                         String respNonce, String appkey, String expectSign) {
        if (expectSign == null || expectSign.isEmpty()) {
            return false;
        }
        String calc = generateSign(responseBody, appid, respTimestamp, respNonce, appkey);
        return calc.equalsIgnoreCase(expectSign);
    }

    /**
     * 10 位 Unix 时间戳（秒）
     */
    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 6 位数字随机串（云打印机 V2 文档原文要求）
     */
    public static String generateNonce() {
        int n = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(n);
    }

    private static String hmacSha256(String content, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] bytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                String hex = Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3);
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("生成商米签名失败", e);
            return "";
        }
    }
}
