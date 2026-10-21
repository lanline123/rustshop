package com.tigshop.common.utils;

import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

public class AMapSignUtils {

    /**
     * 生成高德 API 请求签名
     * @param params 请求参数（不含 sig）
     * @param secret 高德控制台生成的 secret
     * @return 签名字符串（小写）
     */
    public static String generateSignature(Map<String, String> params, String secret) {
        try {
            // 使用 TreeMap 保证参数名按字典序排序
            Map<String, String> sortedParams = new TreeMap<>(params);

            // 拼接参数
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            // 去掉最后一个 &
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }

            // 在最后拼接 secret
            sb.append(secret);

            // 进行 MD5 加密
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes("UTF-8"));

            // 转换成小写十六进制
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toLowerCase();

        } catch (Exception e) {
            throw new RuntimeException("生成高德地图签名失败", e);
        }
    }

    public static void main(String[] args) {
        // 示例参数
        Map<String, String> params = new TreeMap<>();
        params.put("key", "你的key");
        params.put("location", "116.481488,39.990464");

        String secret = "你的secret";
        String sig = generateSignature(params, secret);

        System.out.println("生成的签名: " + sig);
    }
}
