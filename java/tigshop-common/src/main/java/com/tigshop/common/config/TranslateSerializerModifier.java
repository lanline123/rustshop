package com.tigshop.common.config;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.properties.TigshopProperties;

import java.util.List;

/**
 * Bean 字段级序列化器修改器
 *
 * <p>用 BeanSerializerModifier 替代 SimpleModule.addSerializer 注册方式，
 * 直接给带有 {@link JsonTranslate} / {@link Sensitive} 注解的字段赋值自定义 Serializer。
 *
 * <p>背景：Jackson 对 String 等内置类型有 well-known 默认 serializer 的优先级机制，
 * 通过 {@code SimpleModule.addSerializer(new TranslateSerializer(...))} 注册的全局 String
 * Serializer 可能无法触发 {@code ContextualSerializer.createContextual}，导致 @JsonTranslate
 * / @Sensitive 注解无法被识别。本 modifier 在 bean 字段级别强制替换 serializer，绕开该问题。
 */
public class TranslateSerializerModifier extends BeanSerializerModifier {

    private final TranslatePackage translatePackage;
    private final TigshopProperties tigshopProperties;

    public TranslateSerializerModifier(TranslatePackage translatePackage, TigshopProperties tigshopProperties) {
        this.translatePackage = translatePackage;
        this.tigshopProperties = tigshopProperties;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            if (writer.getType() == null || !String.class.equals(writer.getType().getRawClass())) {
                continue;
            }
            JsonTranslate translate = writer.getAnnotation(JsonTranslate.class);
            if (translate != null) {
                writer.assignSerializer((JsonSerializer) new TranslateSerializer(translatePackage, true, translate.dataType()));
                continue;
            }
            Sensitive sensitive = writer.getAnnotation(Sensitive.class);
            if (sensitive != null) {
                writer.assignSerializer((JsonSerializer) new SensitiveSerializer(tigshopProperties, sensitive.type()));
            }
        }
        return beanProperties;
    }
}
