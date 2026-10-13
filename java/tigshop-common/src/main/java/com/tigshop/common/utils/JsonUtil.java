// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson 工具类，用于处理 JSON 字符串和对象的转换
 *
 * @author Tigshop团队
 * @create 2024年12月09日 10:19
 */
@Slf4j
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        // 反序列化时，忽略 JSON 中多余的字段
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 序列化时，忽略空 Bean 转 JSON 异常
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 使 JSON 美观打印
        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param object 需要转换的对象
     * @return JSON 字符串
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("对象转 JSON 失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将对象转换为格式化的 JSON 字符串
     *
     * @param object 需要转换的对象
     * @return 美化的 JSON 字符串
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("对象转美化 JSON 失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串转换为对象
     *
     * @param json  JSON 字符串
     * @param clazz 对象类型
     * @param <T>   泛型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        String trimmed = json.trim();

        // 先做基本 JSON 格式判断（对象、数组、null、true、false、数字、字符串）
        boolean looksLikeJson = trimmed.startsWith("{")
                || trimmed.startsWith("[")
                || trimmed.startsWith("\"")
                || "null".equals(trimmed)
                || "true".equalsIgnoreCase(trimmed)
                || "false".equalsIgnoreCase(trimmed)
                // 数字开头
                || Character.isDigit(trimmed.charAt(0))
                // 负数
                || trimmed.startsWith("-");

        if (!looksLikeJson) {
            log.warn("输入内容不是有效的 JSON 格式: {}", json);
            return null;
        }

        try {
            return MAPPER.readValue(trimmed, clazz);
        } catch (IOException e) {
            log.error("JSON 转对象失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串转换为复杂对象（如 List、Map）
     *
     * @param json          JSON 字符串
     * @param typeReference 目标类型
     * @param <T>           泛型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            log.error("JSON 转对象失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串解析为 JsonNode (树形结构)
     *
     * @param json JSON 字符串
     * @return JsonNode 对象
     */
    public static JsonNode parse(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (IOException e) {
            log.error("JSON 解析为 JsonNode 失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 JSON 转换为 Map
     *
     * @param json JSON 字符串
     * @return Map 对象
     */
    public static Map<String, Object> jsonToMap(String json) {
        return fromJson(json, new TypeReference<>() {
        });
    }

    /**
     * 将 JSON 转换为 List
     *
     * @param json  JSON 字符串
     * @param clazz List 内部的元素类型
     * @param <T>   泛型
     * @return List 对象
     */
    public static <T> List<T> jsonToList(String json, Class<T> clazz) {
        return fromJson(json, new TypeReference<>() {
        });
    }

    /**
     * 将 JSONNode 转换为驼峰命名的 JSONNode （可处理 对象和数组）
     * @param jsonNode
     * @return
     */
    public static JsonNode convertKeysToCamelCase(JsonNode jsonNode) {
        if (jsonNode.isObject()) {
            return convertObjectNodeToCamelCase(jsonNode);
        } else if (jsonNode.isArray()) {
            List<JsonNode> resultList = new ArrayList<>();
            jsonNode.forEach(item -> {
                if (item.isObject()) {
                    resultList.add(convertObjectNodeToCamelCase(item));
                } else {
                    resultList.add(item); // 非对象，直接添加
                }
            });
            return MAPPER.valueToTree(resultList);
        } else {
            // 既不是对象也不是数组，原样返回
            return jsonNode;
        }
    }

    /**
     * 将 ObjectNode 转换为驼峰命名的 ObjectNode
     * @param objectNode
     * @return
     */
    private static JsonNode convertObjectNodeToCamelCase(JsonNode objectNode) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        objectNode.fields().forEachRemaining(entry -> {
            String camelKey = StrUtil.toCamelCase(entry.getKey());
            JsonNode value = entry.getValue();
            if (value.isObject()) {
                resultMap.put(camelKey, convertObjectNodeToCamelCase(value));
            } else if (value.isArray()) {
                List<Object> list = new ArrayList<>();
                value.forEach(item -> {
                    if (item.isObject()) {
                        list.add(convertObjectNodeToCamelCase(item));
                    } else {
                        list.add(item.asText());
                    }
                });
                resultMap.put(camelKey, list);
            } else {
                resultMap.put(camelKey, value.asText());
            }
        });
        return MAPPER.valueToTree(resultMap);
    }


    public static JSONArray checkJsonType(String jsonStr) {
        if (StrUtil.isBlank(jsonStr)) {
            return JSONUtil.parseArray("[]");
        }
        try {
            boolean isArray = JSONUtil.isTypeJSONArray(jsonStr);

            if (isArray) {
                return JSONUtil.parseArray(jsonStr);
            } else {
                HtmlData htmlData = new HtmlData("text", jsonStr);
                JSONArray array = new JSONArray();
                array.add(htmlData);
                return array;
            }
        } catch (Exception e) {
            HtmlData htmlData = new HtmlData("text", jsonStr);
            JSONArray array = new JSONArray();
            array.add(htmlData);
            return array;
        }
    }

    @Data
    @Schema(description = "字符串类型数据")
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HtmlData {
        @Schema(description = "类型")
        private String type;

        @Schema(description = "数据")
        private String html;
    }


}
