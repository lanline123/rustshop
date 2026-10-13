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

/**
 * 社交分享落地页工具
 * <p>
 * 生成带 Open Graph 标签的 HTML 落地页，供社交平台（Facebook 等）抓取链接预览卡；
 * 真实访客访问后由落地页自动跳转到对应的 H5 详情页。供商品、买家秀等多处分享场景复用。
 *
 * @author Tigshop团队
 */
public final class ShareHtmlUtil {

    private ShareHtmlUtil() {
    }

    /**
     * 构建带 Open Graph 标签的分享落地页 HTML。
     *
     * @param title       分享标题（og:title / title）
     * @param description 分享描述（og:description）
     * @param image       预览图绝对地址（og:image，为空则不输出该标签）
     * @param targetUrl   真实访客跳转的目标地址（og:url，同时用于 JS 跳转）
     * @param ogType      Open Graph 类型，如 product / article
     * @return 落地页 HTML
     */
    public static String buildOgHtml(String title, String description, String image, String targetUrl, String ogType) {
        String safeTitle = escapeHtmlAttr(title);
        String safeDesc = escapeHtmlAttr(description);
        String safeImage = escapeHtmlAttr(image);
        String safeUrl = escapeHtmlAttr(targetUrl);
        String safeType = escapeHtmlAttr(StrUtil.isNotBlank(ogType) ? ogType : "website");
        // JS 字符串上下文转义，避免脚本注入或截断
        String jsUrl = targetUrl == null ? "" : targetUrl.replace("\\", "\\\\").replace("'", "\\'").replace("<", "\\u003c");

        StringBuilder sb = new StringBuilder(512);
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
                .append("<meta property=\"og:type\" content=\"").append(safeType).append("\">")
                .append("<meta property=\"og:title\" content=\"").append(safeTitle).append("\">")
                .append("<meta property=\"og:description\" content=\"").append(safeDesc).append("\">");
        if (StrUtil.isNotBlank(safeImage)) {
            sb.append("<meta property=\"og:image\" content=\"").append(safeImage).append("\">");
        }
        sb.append("<meta property=\"og:url\" content=\"").append(safeUrl).append("\">")
                .append("<meta name=\"twitter:card\" content=\"summary_large_image\">")
                .append("<title>").append(safeTitle).append("</title>")
                .append("<script>location.replace('").append(jsUrl).append("');</script>")
                .append("</head><body><a href=\"").append(safeUrl).append("\">").append(safeTitle).append("</a></body></html>");
        return sb.toString();
    }

    /**
     * 把可能的相对路径补全为绝对 URL（社交平台抓取 og:image 需要绝对地址）。
     */
    public static String toAbsoluteUrl(String url, String h5Domain) {
        if (StrUtil.isBlank(url)) {
            return "";
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        String origin = h5Domain == null ? "" : h5Domain;
        int schemeEnd = origin.indexOf("://");
        if (schemeEnd > 0) {
            int pathStart = origin.indexOf('/', schemeEnd + 3);
            if (pathStart > 0) {
                origin = origin.substring(0, pathStart);
            }
        }
        if (StrUtil.isBlank(origin)) {
            return url;
        }
        return origin + (url.startsWith("/") ? url : "/" + url);
    }

    /**
     * HTML 属性值转义，防止破坏标签结构或注入。
     */
    public static String escapeHtmlAttr(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
