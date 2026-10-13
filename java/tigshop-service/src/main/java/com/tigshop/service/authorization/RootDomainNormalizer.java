package com.tigshop.service.authorization;

import java.net.IDN;
import java.util.Locale;
import java.util.Set;

/** 将运行时站点域名归一化为官网记录的唯一 rootDomain（eTLD+1）。 */
public final class RootDomainNormalizer {

    private static final Set<String> COMMON_TWO_LEVEL_SUFFIXES = Set.of(
            "co.uk", "org.uk", "me.uk", "com.cn", "net.cn", "org.cn",
            "com.au", "net.au", "org.au", "co.jp", "co.kr", "com.br",
            "com.hk", "com.sg", "com.tw", "co.nz", "co.za");

    private RootDomainNormalizer() {
    }

    /**
     * 返回 ASCII、小写、无端口和路径的 eTLD+1；IP、localhost 或非法域名返回空串。
     */
    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String host = value.trim();
        try {
            int schemeEnd = host.indexOf("://");
            if (schemeEnd >= 0) {
                host = host.substring(schemeEnd + 3);
            }
            int end = host.length();
            for (char separator : new char[]{'/', '?', '#'}) {
                int index = host.indexOf(separator);
                if (index >= 0) {
                    end = Math.min(end, index);
                }
            }
            host = host.substring(0, end);
            int at = host.lastIndexOf('@');
            if (at >= 0) {
                host = host.substring(at + 1);
            }
            // IPv6 和端口不属于授权域名；IPv6 直接拒绝，普通域名只去掉数字端口。
            if (host.startsWith("[")) {
                return "";
            }
            int colon = host.lastIndexOf(':');
            if (colon > 0 && host.substring(colon + 1).matches("\\d+")) {
                host = host.substring(0, colon);
            }
            if (host == null || host.isBlank() || host.equalsIgnoreCase("localhost")
                    || host.matches("(?:\\d{1,3}\\.){3}\\d{1,3}")) {
                return "";
            }
            host = IDN.toASCII(host, IDN.USE_STD3_ASCII_RULES)
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("\\.+$", "");
            String[] labels = host.split("\\.");
            if (labels.length < 2) {
                return "";
            }
            String suffix = labels[labels.length - 2] + "." + labels[labels.length - 1];
            int start = labels.length - (COMMON_TWO_LEVEL_SUFFIXES.contains(suffix) ? 3 : 2);
            if (start < 0 || labels[start].isBlank()) {
                return "";
            }
            return String.join(".", java.util.Arrays.copyOfRange(labels, start, labels.length));
        } catch (Exception ignored) {
            return "";
        }
    }
}
