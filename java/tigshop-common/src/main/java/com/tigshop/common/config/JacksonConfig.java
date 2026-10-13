package com.tigshop.common.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.properties.TigshopProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * 用 BeanSerializerModifier 在字段级强制替换 serializer，
     * 解决 SimpleModule.addSerializer 对 String 等内置类型注册不被 Jackson 优先识别的问题。
     */
    @Bean
    public Module translateModule(TranslatePackage translationService, TigshopProperties tigshopProperties) {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new TranslateSerializerModifier(translationService, tigshopProperties));
        return module;
    }
}