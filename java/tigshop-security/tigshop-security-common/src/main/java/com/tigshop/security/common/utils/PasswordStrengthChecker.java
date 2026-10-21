package com.tigshop.security.common.utils;

/**
 * 密码强度规则校验
 *
 * @author kidd
 * @since 2025/6/19 15:20
 */
public class PasswordStrengthChecker {

    public enum StrengthLevel {
        WEAK, MEDIUM, STRONG
    }

    /**
     * 校验密码强度
     * @param password 密码字符串
     * @return 密码强度等级：WEAK, MEDIUM, STRONG
     */
    public static StrengthLevel checkStrength(String password) {
        if (password == null || password.isEmpty()) {
            return StrengthLevel.WEAK;
        }

        int length = password.length();
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");

        // 弱密码：长度<=8 或者 是常见弱口令（如纯数字、纯字母、常用词）
        if (length <= 8 || isCommonWeakPassword(password)) {
            return StrengthLevel.WEAK;
        }

        // 强密码：长度>=16，包含大小写、数字、特殊符号，无明显规律
        if (length >= 16 && hasLower && hasUpper && hasDigit && hasSpecial) {
            return StrengthLevel.STRONG;
        }

        // 中等密码：长度>=12，包含大小写字母 + 数字
        if (length >= 12 && hasLower && hasUpper && hasDigit) {
            return StrengthLevel.MEDIUM;
        }

        // 默认认为弱密码
        return StrengthLevel.WEAK;
    }

    /**
     * 判断是否为常见弱密码
     * @param password 输入密码
     * @return 是否为弱密码
     */
    private static boolean isCommonWeakPassword(String password) {
        String lower = password.toLowerCase();
        String[] weakPasswords = {
                "123456", "password", "12345678", "qwerty", "abc123", "111111",
                "123123", "123456789", "iloveyou", "admin"
        };
        for (String weak : weakPasswords) {
            if (lower.equals(weak)) {
                return true;
            }
        }

        // 纯数字或纯字母
        if (password.matches("\\d+") || password.matches("[a-zA-Z]+")) {
            return true;
        }

        return false;
    }

}
