package com.tigshop.service.sms;

import com.tigshop.bean.enums.setting.SmsProviderEnum;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsPlaceholderConverter {

    private static final Pattern DOLLAR_BRACE = Pattern.compile("\\$\\{(\\w+)}");
    private static final Pattern BRACE_ONLY = Pattern.compile("(?<!\\$)\\{(\\w+)}");
    private static final Pattern ALL_PLACEHOLDER = Pattern.compile("\\$?\\{(\\w+)}");

    private SmsPlaceholderConverter() {
    }

    /**
     * 将 content 中的占位符按目标服务商格式转换。
     * 同时兼容 ${variable}（短信模板）和 {variable}（站内信模板）两种 DB 存储格式。
     */
    public static String convert(String content, SmsProviderEnum provider) {
        if (content == null || content.isEmpty() || provider == null) {
            return content;
        }

        return switch (provider) {
            case ALIYUN, HUAWEI -> convertToAliyun(content);
            case TENCENT -> convertToTencent(content);
            case VOLCENGINE -> convertToVolcengine(content);
        };
    }

    /**
     * 阿里云/华为云：{variable} → ${variable}，已有 ${variable} 不变
     */
    private static String convertToAliyun(String content) {
        return BRACE_ONLY.matcher(content).replaceAll("\\${$1}");
    }

    /**
     * 腾讯云：${variable} 和 {variable} 均转为 {1},{2},... 按出现顺序
     */
    private static String convertToTencent(String content) {
        AtomicInteger index = new AtomicInteger(1);
        Matcher matcher = ALL_PLACEHOLDER.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "{" + index.getAndIncrement() + "}");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 火山引擎：${variable} → {variable}，已有 {variable} 不变
     */
    private static String convertToVolcengine(String content) {
        return DOLLAR_BRACE.matcher(content).replaceAll("{$1}");
    }
}
