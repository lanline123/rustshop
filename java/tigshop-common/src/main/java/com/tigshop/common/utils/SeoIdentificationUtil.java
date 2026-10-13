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

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.exception.GlobalException;

/**
 * SEO标识处理工具类
 *
 * @author Tigshop团队
 * @create 2025年07月08日 14:30
 */
public class SeoIdentificationUtil {

    /**
     * 从SEO链接中提取seoIdentification
     *
     * @param seoLink SEO链接
     * @return seoIdentification
     */
    public static String extractSeoIdentificationFromLink(String seoLink) {
        if (seoLink == null || seoLink.isEmpty()) {
            throw new GlobalException("SEO链接不能为空");
        }

        // 处理链接格式，移除前缀和后缀
        if (seoLink.startsWith("/category/")) {
            seoLink = seoLink.substring("/category/".length());
        }

        // 如果链接包含多级路径，取最后一级
        String[] parts = seoLink.split("/");
        return parts[parts.length - 1];
    }

    /**
     * 格式化英文文本
     * 1. 转换为小写
     * 2. 将空格替换为中划线
     * 3. 去除所有非字母数字和中划线的字符
     *
     * @param englishText 英文文本
     * @return 格式化后的文本
     */
    public static String formatEnglishText(String englishText) {
        if (englishText == null || englishText.isEmpty()) {
            return "";
        }

        // 转小写
        String lowerCase = englishText.toLowerCase();

        // 将空格替换为中划线
        String withDashes = lowerCase.replaceAll("\\s+", "-");

        // 去除所有非字母数字和中划线的字符
        return withDashes.replaceAll("[^a-z0-9-]", "");
    }

    /**
     * 验证SEO标识是否只包含小写字母、数字和连字符(-)
     *
     * @param seoIdentification SEO标识
     * @return 如果只包含小写字母、数字和连字符(-)返回true，否则返回false
     */
    public static boolean isValidSeoIdentification(String seoIdentification) {
        // 如果为空字符串，则返回true，表示通过验证，用户可不填
        if (StrUtil.isBlank(seoIdentification)) {
            return true;
        }

        // 正则表达式：只允许小写字母、数字和连字符(-)
        return seoIdentification.matches("^[a-z0-9\\-]+$");
    }

    public static void main(String[] args) {
        System.out.println(isValidSeoIdentification(""));
        System.out.println(isValidSeoIdentification("-"));
        System.out.println(isValidSeoIdentification("中"));
    }
}