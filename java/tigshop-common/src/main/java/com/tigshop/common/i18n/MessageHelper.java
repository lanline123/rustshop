package com.tigshop.common.i18n;

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * UI 文案取值助手：按当前请求 locale 从 MessageSource 解析多语言文案。
 *
 * <p>locale 来源 {@link ThreadLocalUtil#getValue()}（语言码，如 {@code zh}/{@code en}，
 * 由 HeaderInterceptor 按请求头写入）：{@code en*} → {@link Locale#ENGLISH}（命中
 * {@code messages_en.properties}）；其余 / 空 → {@link Locale#SIMPLIFIED_CHINESE}
 * （回退基线 {@code messages.properties} = 中文），与现状默认中文行为一致。促销描述先
 * 取中文完整模板，再交由现有业务翻译模块按语言配置翻译。
 *
 * @author Tigshop团队
 */
@Component
@RequiredArgsConstructor
public class MessageHelper {

    private final MessageSource messageSource;

    /**
     * 按当前 locale 取文案。
     *
     * @param code 文案 key
     * @param args MessageFormat 占位参数（数值建议预格式化为 String 传入，避免数字重格式化）
     * @return 解析后的文案；key 缺失时返回 code 本身
     */
    public String get(String code, Object... args) {
        return messageSource.getMessage(code, args, resolveLocale());
    }

    /**
     * 取简体中文基线文案。促销描述先按中文模板生成，再交给业务翻译模块处理其它语言，
     * 因此不能依赖当前请求 locale 解析 resources 中的固定语言集合。
     */
    public String getChinese(String code, Object... args) {
        return messageSource.getMessage(code, args, Locale.SIMPLIFIED_CHINESE);
    }

    /**
     * 读取未格式化的中文模板，供业务翻译模块翻译模板后再填充活动参数。
     */
    public String getChineseTemplate(String code) {
        return messageSource.getMessage(code, null, Locale.SIMPLIFIED_CHINESE);
    }

    /**
     * 只有简体中文请求使用资源模板。zh-tw 等其它语言仍由业务翻译模块按数据库语言配置处理。
     */
    public boolean isSimplifiedChineseLocale() {
        String localeCode = ThreadLocalUtil.getValue();
        if (StrUtil.isBlank(localeCode)) {
            return true;
        }
        String normalized = localeCode.trim().toLowerCase().replace('_', '-');
        return "zh".equals(normalized) || "zh-cn".equals(normalized);
    }

    private Locale resolveLocale() {
        String localeCode = ThreadLocalUtil.getValue();
        if (StrUtil.isNotBlank(localeCode) && localeCode.trim().toLowerCase().startsWith("en")) {
            return Locale.ENGLISH;
        }
        return Locale.SIMPLIFIED_CHINESE;
    }
}
