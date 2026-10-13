package com.tigshop.service.sms;

import com.tigshop.bean.enums.setting.SmsProviderEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 短信出口唯一的号码格式规则。不做账号归一化，也不改变国内外通道选择。
 * 只识别明确格式；无法识别的旧输入保留各供应商原行为，不猜测丢失的区号。
 */
final class SmsPhoneNumberCodec {
    private static final Logger log = LoggerFactory.getLogger(SmsPhoneNumberCodec.class);
    private static final Pattern MAINLAND = Pattern.compile("(?:\\+86|0086|86)?(1[3-9][0-9]{9})");
    private static final Pattern INTERNATIONAL = Pattern.compile("\\+[1-9][0-9]{1,14}");

    private SmsPhoneNumberCodec() {
    }

    static String format(String raw, SmsProviderEnum provider, Boolean useInternational) {
        String canonical = canonicalize(raw);
        if (canonical == null) {
            // 不记录完整号码、验证码、模板或凭证。
            log.warn("短信号码兼容路径：reason=LEGACY_UNRESOLVED, provider={}, inputLength={}",
                    provider, raw == null ? null : raw.length());
            return legacyFormat(raw, provider);
        }
        if (provider == SmsProviderEnum.ALIYUN) {
            boolean mainland = canonical.startsWith("+86");
            if (mainland && !Boolean.TRUE.equals(useInternational)) {
                return canonical.substring(3);
            }
            if (!mainland && Boolean.TRUE.equals(useInternational)) {
                return canonical.substring(1);
            }
            // 通道已由业务层选择；配置与号码不匹配时，不替业务层改路由或拒绝。
            log.warn("短信号码兼容路径：reason=CHANNEL_COMPATIBILITY, provider={}", provider);
            return raw;
        }
        return canonical;
    }

    /**
     * 生成短信出口诊断信息。只返回脱敏号码，避免把完整手机号写入生产日志。
     */
    static String diagnostic(String raw, String formatted, SmsProviderEnum provider, Boolean useInternational) {
        boolean canonical = canonicalize(raw) != null;
        boolean duplicate86 = hasDuplicateCountryCode(formatted);
        boolean formatValid = canonical && isProviderFormat(formatted, provider, useInternational) && !duplicate86;
        String mode = canonical ? "CANONICAL" : "LEGACY_COMPAT";
        return "provider=" + provider
                + ", before=" + mask(raw)
                + ", after=" + mask(formatted)
                + ", formatValid=" + formatValid
                + ", duplicate86=" + duplicate86
                + ", mode=" + mode;
    }

    /** 返回明确格式的统一表示；null 表示未识别，不代表号码已验证有效。 */
    static String canonicalize(String raw) {
        if (raw == null) {
            return null;
        }
        Matcher mainland = MAINLAND.matcher(raw);
        if (mainland.matches()) {
            return "+86" + mainland.group(1);
        }
        // +86 下不符合上述格式的输入也属于历史兼容，不误当成其他国家号码。
        if (!raw.startsWith("+86") && INTERNATIONAL.matcher(raw).matches()) {
            return raw;
        }
        return null;
    }

    /** 冻结旧规则，仅供未识别输入兼容；禁止在供应商中复制此逻辑。 */
    private static String legacyFormat(String raw, SmsProviderEnum provider) {
        return switch (provider) {
            case ALIYUN -> raw;
            case TENCENT, HUAWEI -> raw.startsWith("+") ? raw : "+86" + raw;
            case VOLCENGINE -> raw.startsWith("+86") ? raw : "+86" + raw;
        };
    }

    private static boolean isProviderFormat(String formatted, SmsProviderEnum provider, Boolean useInternational) {
        if (formatted == null) {
            return false;
        }
        if (provider == SmsProviderEnum.ALIYUN) {
            if (Boolean.TRUE.equals(useInternational)) {
                return formatted.matches("[1-9][0-9]{1,14}");
            }
            return formatted.matches("1[3-9][0-9]{9}");
        }
        return formatted.matches("\\+[1-9][0-9]{1,14}");
    }

    private static boolean hasDuplicateCountryCode(String formatted) {
        return formatted != null && (formatted.startsWith("+8686") || formatted.startsWith("8686"));
    }

    private static String mask(String value) {
        if (value == null) {
            return "<null>";
        }
        if (value.length() <= 4) {
            return "****";
        }
        int visiblePrefix = Math.min(3, value.length() - 4);
        return value.substring(0, visiblePrefix) + "******" + value.substring(value.length() - 4);
    }
}
