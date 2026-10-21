package com.tigshop.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import org.springframework.util.StringUtils;

/**
 * SKU 规格串 / skuData JSON 翻译工具。
 * <p>
 * 规格串形如 {@code 颜色:红|尺码:L}：按 {@code |} 分段，再按 {@code :} 拆 name/value，
 * 逐段用 dataType=SKU(8) 翻译后拼回。
 * <p>
 * skuData JSON 形如 {@code [{"name":"颜色","value":"红"}]}：翻译 name/value（及 attrPicThumb 图片）。
 */
public final class SkuValueTranslateUtils {

    private SkuValueTranslateUtils() {
    }

    /**
     * 翻译规格串；空值原样返回。
     *
     * @param skuValue         原始规格串
     * @param translatePackage 翻译入口
     * @return 翻译后的规格串
     */
    public static String translateSkuValue(String skuValue, TranslatePackage translatePackage) {
        if (!StringUtils.hasText(skuValue) || translatePackage == null) {
            return skuValue;
        }
        String[] segments = skuValue.split("\\|");
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (!StringUtils.hasText(segment)) {
                continue;
            }
            String[] parts = segment.split(":");
            for (int j = 0; j < parts.length; j++) {
                if (StringUtils.hasText(parts[j])) {
                    parts[j] = translatePackage.translate(parts[j], DataType.SKU.getCode());
                }
            }
            segments[i] = String.join(":", parts);
        }
        return String.join("|", segments);
    }

    /**
     * 翻译 skuData JSON 数组字符串中的 name/value（SKU=8）与 attrPicThumb（IMAGE=7）。
     * 解析失败时原样返回。
     */
    public static String translateSkuDataJson(String skuDataJson, TranslatePackage translatePackage) {
        if (!StringUtils.hasText(skuDataJson) || translatePackage == null) {
            return skuDataJson;
        }
        try {
            JSONArray arr = JSON.parseArray(skuDataJson);
            if (arr == null || arr.isEmpty()) {
                return skuDataJson;
            }
            for (int i = 0; i < arr.size(); i++) {
                Object item = arr.get(i);
                if (!(item instanceof JSONObject obj)) {
                    continue;
                }
                translateSkuDataObject(obj, translatePackage);
            }
            return arr.toJSONString();
        } catch (Exception e) {
            return skuDataJson;
        }
    }

    /**
     * 翻译单个 skuData JSONObject 的 name/value/attrPicThumb 字段。
     */
    public static void translateSkuDataObject(JSONObject obj, TranslatePackage translatePackage) {
        if (obj == null || translatePackage == null) {
            return;
        }
        for (String key : new String[]{"name", "value"}) {
            Object val = obj.get(key);
            if (val instanceof String strVal && StringUtils.hasText(strVal)) {
                obj.put(key, translatePackage.translate(strVal, DataType.SKU.getCode()));
            }
        }
        Object pic = obj.get("attrPicThumb");
        if (pic instanceof String picVal && StringUtils.hasText(picVal)) {
            obj.put("attrPicThumb", translatePackage.translate(picVal, DataType.IMAGE_TITLE.getCode()));
        }
    }
}
