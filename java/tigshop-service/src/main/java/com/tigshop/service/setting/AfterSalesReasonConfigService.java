package com.tigshop.service.setting;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.param.settings.config.OrderSettingsSaveParam;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.ThreadLocalUtil;
import com.tigshop.helper.SettingsHelper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AfterSalesReasonConfigService {

    private static final int MAX_REASON_COUNT = 10;
    private static final int MAX_REASON_LENGTH = 50;

    private final SettingsHelper settingsHelper;

    // 翻译包经 LocalesService → CurrencyService → ConfigService 回指本类，@Lazy 打破启动期循环依赖
    @Lazy
    @Resource
    private TranslatePackage translatePackage;

    public ReasonConfig getConfig() {
        Map<String, String> snapshot = settingsHelper.getRawSnapshot(
                SettingsEnum.AFTER_SALES_REASON_ENABLED,
                SettingsEnum.AFTER_SALES_REASON_LIST);
        String enabledRaw = snapshot.getOrDefault(
                SettingsEnum.AFTER_SALES_REASON_ENABLED.getBizCode(),
                SettingsEnum.AFTER_SALES_REASON_ENABLED.getDefaultValue());
        String reasonsRaw = snapshot.getOrDefault(
                SettingsEnum.AFTER_SALES_REASON_LIST.getBizCode(),
                SettingsEnum.AFTER_SALES_REASON_LIST.getDefaultValue());
        Integer enabled = parseEnabled(enabledRaw);
        List<String> reasons = parseStoredReasons(reasonsRaw);
        validateEnabledList(enabled, reasons);
        return new ReasonConfig(enabled, List.copyOf(reasons));
    }

    public void normalizeForSave(OrderSettingsSaveParam param) {
        Integer incomingEnabled = param.getAfterSalesReasonEnabled();
        List<String> incomingReasons = param.getAfterSalesReasonList();
        if (incomingEnabled == null && incomingReasons == null) {
            return;
        }

        List<String> normalizedIncoming = incomingReasons == null ? null : normalizeReasons(incomingReasons);
        ReasonConfig current = null;
        if (incomingEnabled == null || incomingReasons == null) {
            current = getConfig();
        }
        Integer effectiveEnabled = incomingEnabled != null ? parseEnabled(String.valueOf(incomingEnabled)) : current.enabled();
        List<String> effectiveReasons = normalizedIncoming != null ? normalizedIncoming : current.reasons();
        validateEnabledList(effectiveEnabled, effectiveReasons);
        if (normalizedIncoming != null) {
            param.setAfterSalesReasonList(normalizedIncoming);
        }
    }

    public String validateNewBuyerReason(String incomingReason) {
        return validateNewBuyerReason(incomingReason, getConfig());
    }

    public String validateNewBuyerReason(String incomingReason, ReasonConfig config) {
        String normalized = trimToNull(incomingReason);
        if (config.enabled() == 0) {
            return normalized;
        }
        if (normalized == null) {
            throw new GlobalException("请选择原因");
        }
        if (config.reasons().contains(normalized)) {
            return normalized;
        }
        // 配置接口按当前语言返回译文，买家提交的是译文，需按当前语言反查回配置原文后入库
        String configuredReason = matchTranslatedReason(normalized, config.reasons());
        if (configuredReason == null) {
            throw new GlobalException("所选原因已失效，请重新选择");
        }
        return configuredReason;
    }

    private String matchTranslatedReason(String normalized, List<String> reasons) {
        String localeCode = trimToNull(ThreadLocalUtil.getValue());
        if (localeCode == null || reasons.isEmpty()) {
            return null;
        }
        Map<String, String> translations =
                translatePackage.translateBatch(reasons, DataType.API.getCode(), localeCode);
        for (String reason : reasons) {
            if (normalized.equals(trimToNull(translations.get(reason)))) {
                return reason;
            }
        }
        return null;
    }

    public String resolveUpdateReason(String storedReason, String incomingReason) {
        String storedNormalized = trimToNull(storedReason);
        String incomingNormalized = trimToNull(incomingReason);
        if (storedNormalized != null && (incomingNormalized == null || storedNormalized.equals(incomingNormalized))) {
            return storedReason;
        }
        return validateNewBuyerReason(incomingReason);
    }

    private Integer parseEnabled(String raw) {
        if ("0".equals(raw)) {
            return 0;
        }
        if ("1".equals(raw)) {
            return 1;
        }
        throw new GlobalException("原因开关配置异常");
    }

    private List<String> parseStoredReasons(String raw) {
        try {
            JSONArray array = JSON.parseArray(raw);
            if (array == null) {
                throw new GlobalException("原因列表配置异常");
            }
            List<String> reasons = new ArrayList<>(array.size());
            for (Object item : array) {
                if (!(item instanceof String reason)) {
                    throw new GlobalException("原因列表配置异常");
                }
                reasons.add(reason);
            }
            return normalizeReasons(reasons);
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            throw new GlobalException("原因列表配置异常");
        }
    }

    private List<String> normalizeReasons(List<String> reasons) {
        if (reasons.size() > MAX_REASON_COUNT) {
            throw new GlobalException("原因最多添加10项");
        }
        List<String> normalized = new ArrayList<>(reasons.size());
        Set<String> unique = new HashSet<>();
        for (String reason : reasons) {
            String value = trimToNull(reason);
            if (value == null) {
                throw new GlobalException("原因内容不能为空");
            }
            int length = value.codePointCount(0, value.length());
            if (length > MAX_REASON_LENGTH) {
                throw new GlobalException("原因内容不能超过50个字符");
            }
            if (!unique.add(value)) {
                throw new GlobalException("原因内容不能重复");
            }
            normalized.add(value);
        }
        return normalized;
    }

    private void validateEnabledList(Integer enabled, List<String> reasons) {
        if (enabled == 1 && reasons.isEmpty()) {
            throw new GlobalException("开启原因功能时至少保留1项原因");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record ReasonConfig(Integer enabled, List<String> reasons) {
    }
}
