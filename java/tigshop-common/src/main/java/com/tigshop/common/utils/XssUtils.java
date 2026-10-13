package com.tigshop.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;

import java.util.regex.Pattern;

/**
 * XSS防护工具类
 * 用于过滤用户输入中的危险脚本内容，防止XSS攻击
 *
 * @author Tigshop团队
 */
public class XssUtils {

    /**
     * 匹配script标签及其内容的正则
     */
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "<script[^>]*?>.*?</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * 匹配javascript:协议的正则
     */
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
            "javascript\\s*:",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 匹配vbscript:协议的正则
     */
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile(
            "vbscript\\s*:",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 匹配事件处理器属性的正则 (如 onclick, onload, onerror 等)
     */
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
            "on\\w+\\s*=",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 匹配expression()的正则 (CSS表达式注入)
     */
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
            "expression\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 匹配data:协议的正则
     */
    private static final Pattern DATA_PATTERN = Pattern.compile(
            "data\\s*:",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 匹配eval()函数的正则
     */
    private static final Pattern EVAL_PATTERN = Pattern.compile(
            "eval\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 匹配alert()函数的正则
     */
    private static final Pattern ALERT_PATTERN = Pattern.compile(
            "alert\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 匹配confirm()函数的正则
     */
    private static final Pattern CONFIRM_PATTERN = Pattern.compile(
            "confirm\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 匹配prompt()函数的正则
     */
    private static final Pattern PROMPT_PATTERN = Pattern.compile(
            "prompt\\s*\\(",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 清理XSS危险内容
     * 会移除script标签、javascript:协议等危险内容
     *
     * @param content 原始内容
     * @return 过滤后的安全内容
     */
    public static String clean(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }

        String result = content;

        // 移除script标签
        result = SCRIPT_PATTERN.matcher(result).replaceAll("");

        // 移除javascript:协议
        result = JAVASCRIPT_PATTERN.matcher(result).replaceAll("");

        // 移除vbscript:协议
        result = VBSCRIPT_PATTERN.matcher(result).replaceAll("");

        // 移除事件处理器
        result = EVENT_HANDLER_PATTERN.matcher(result).replaceAll("");

        // 移除CSS表达式
        result = EXPRESSION_PATTERN.matcher(result).replaceAll("");

        // 移除eval函数
        result = EVAL_PATTERN.matcher(result).replaceAll("");

        // 移除alert函数
        result = ALERT_PATTERN.matcher(result).replaceAll("");

        // 移除confirm函数
        result = CONFIRM_PATTERN.matcher(result).replaceAll("");

        // 移除prompt函数
        result = PROMPT_PATTERN.matcher(result).replaceAll("");

        return result;
    }

    /**
     * 转义HTML特殊字符，防止XSS
     * 将 < > & " ' 等字符转换为HTML实体
     *
     * @param content 原始内容
     * @return 转义后的安全内容
     */
    public static String escape(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        return HtmlUtil.escape(content);
    }

    /**
     * 清理并转义XSS危险内容
     * 先清理危险标签，再转义特殊字符
     *
     * @param content 原始内容
     * @return 安全内容
     */
    public static String cleanAndEscape(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        return escape(clean(content));
    }

    /**
     * 移除所有HTML标签，只保留纯文本
     *
     * @param content 原始内容
     * @return 纯文本内容
     */
    public static String stripHtml(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        return HtmlUtil.cleanHtmlTag(content);
    }

    /**
     * 判断内容是否包含XSS危险代码
     *
     * @param content 待检测内容
     * @return true-包含危险代码, false-安全
     */
    public static boolean containsXss(String content) {
        if (StrUtil.isBlank(content)) {
            return false;
        }

        return SCRIPT_PATTERN.matcher(content).find()
                || JAVASCRIPT_PATTERN.matcher(content).find()
                || VBSCRIPT_PATTERN.matcher(content).find()
                || EVENT_HANDLER_PATTERN.matcher(content).find()
                || EXPRESSION_PATTERN.matcher(content).find()
                || EVAL_PATTERN.matcher(content).find()
                || ALERT_PATTERN.matcher(content).find()
                || CONFIRM_PATTERN.matcher(content).find()
                || PROMPT_PATTERN.matcher(content).find();
    }
}

