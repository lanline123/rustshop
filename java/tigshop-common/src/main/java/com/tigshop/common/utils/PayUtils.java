package com.tigshop.common.utils;

public class PayUtils {


    public static String getPayCodeByAuthCode(String authCode) {
        // 区分支付宝支付和微信支付


        if (authCode == null || authCode.isEmpty()) {
            return null;
        }

        // 微信支付：18位纯数字，以1[0-5]开头
        if (authCode.matches("^1[0-5]\\d{16}$")) {
            return "wechat";
        }

        // 支付宝支付：25~30开头的16~24位数字 或 fp开头的35位字符串
        if (authCode.matches("^(2[5-9]|30)\\d{14,22}$|^fp.{33}$")) {
            return "alipay";
        }

        return null;
    }
}
