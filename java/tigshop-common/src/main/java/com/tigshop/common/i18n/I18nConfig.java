package com.tigshop.common.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * UI 文案国际化（MessageSource）基建。
 *
 * <p>承载「满/减/折/立减」等 UI 文案、异常提示、时间单位等纯展示文案的多语言资源，
 * 与业务数据翻译（TranslationsData 表）解耦。资源文件位于
 * {@code i18n/messages.properties}（中文基线）/ {@code messages_en.properties}（英文）。
 *
 * <p>Bean 命名为 {@code messageSource} 以覆盖 Spring Boot 自动配置
 * （{@code MessageSourceAutoConfiguration} 带 {@code @ConditionalOnMissingBean(name="messageSource")}）。
 *
 * @author Tigshop团队
 */
@Configuration
public class I18nConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        // 找不到对应语言资源时回退基线 messages.properties（中文），不回退系统 Locale
        messageSource.setFallbackToSystemLocale(false);
        // 缺失 key 时返回 code 本身，避免抛 NoSuchMessageException
        messageSource.setUseCodeAsDefaultMessage(true);
        // 缓存常驻（文案随发布更新，无需热加载）
        messageSource.setCacheSeconds(-1);
        return messageSource;
    }
}
