package com.tigshop.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 将 JSON 数字或数字数组反序列化为 {@code List<Integer>}。
 * 素材中心视频编辑入口 historically 传 number，表单入口传 number[]。
 */
public class IntegerListFlexibleDeserializer extends JsonDeserializer<List<Integer>> {

    @Override
    public List<Integer> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        List<Integer> result = new ArrayList<>();
        if (token == JsonToken.START_ARRAY) {
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                Integer value = readInteger(parser);
                if (value != null) {
                    result.add(value);
                }
            }
            return result;
        }
        Integer single = readInteger(parser);
        if (single != null) {
            result.add(single);
        }
        return result;
    }

    private static Integer readInteger(JsonParser parser) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == null || token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token.isNumeric()) {
            return parser.getIntValue();
        }
        if (token == JsonToken.VALUE_STRING) {
            String text = parser.getText();
            if (text == null || text.isBlank()) {
                return null;
            }
            return Integer.valueOf(text.trim());
        }
        throw new IOException("Expecting integer or integer array for galleryId, got: " + token);
    }
}
