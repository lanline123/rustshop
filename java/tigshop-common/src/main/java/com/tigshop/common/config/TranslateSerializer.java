package com.tigshop.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.core.TranslatePackage;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author Tigshop团队
 * @create 2025/8/6 20:36
 */
public class TranslateSerializer extends StdSerializer<String> implements ContextualSerializer {

    private final TranslatePackage translatePackage;
    private final boolean shouldTranslate;
    private final Integer dataType;

    public TranslateSerializer(TranslatePackage translatePackage) {
        this(translatePackage, false, null);
    }

    public TranslateSerializer(TranslatePackage translatePackage, boolean shouldTranslate, Integer dataType) {
        super(String.class);
        this.translatePackage = translatePackage;
        this.shouldTranslate = shouldTranslate;
        this.dataType = dataType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (shouldTranslate && StringUtils.hasText(value)) {
            if (dataType != null && dataType >= 0) {
                gen.writeString(translatePackage.translate(value, dataType));
            } else {
                gen.writeString(translatePackage.translate(value));
            }
        } else {
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            JsonTranslate translate = property.getAnnotation(JsonTranslate.class);
            if (translate != null) {
                int dataType = translate.dataType();
                return new TranslateSerializer(translatePackage, true, dataType);
            }
        }
        return new TranslateSerializer(translatePackage, false, null);
    }
}