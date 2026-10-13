package com.tigshop.common.utils;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品批量导入的规格文本与图片链接解析工具。
 * <p>
 * 平台侧（{@code ProductBatchServiceImpl}）与供应商侧（{@code VendorProductBatchServiceImpl}）共用，
 * 避免两处各自实现导致分隔符 / 前缀规则静默漂移。
 * <p>
 * Excel 侧格式约定：
 * <ul>
 *     <li>规格文本形如 {@code 颜色：红色；尺码：37}，组间 {@code ; ； , ，}，组内 {@code : ：}</li>
 *     <li>图片列形如 {@code 【图片链接】https://a.jpg；https://b.jpg}，多图分隔 {@code ; ； |}</li>
 * </ul>
 * 落库侧规格串统一为 {@code 颜色:红色|尺码:37}（与 {@code SkuValueTranslateUtils} 的解析格式一致）。
 */
public final class SpecImportUtils {

    /** 图片列允许的前缀，运营从后台复制链接时会带上。 */
    private static final String[] PIC_PREFIXES = {"【图片链接】", "【图片地址】"};

    private SpecImportUtils() {
    }

    /**
     * 拆分图片列为 URL 数组，自动去掉 {@code 【图片链接】} 一类前缀。空值返回空数组。
     */
    public static String[] splitPicUrls(String picUrl) {
        if (!StringUtils.hasText(picUrl)) {
            return new String[0];
        }
        return stripPicPrefix(picUrl.trim()).split("[;；|]");
    }

    /**
     * 去掉图片列的中文前缀；无前缀时原样返回。
     */
    public static String stripPicPrefix(String s) {
        String r = s;
        for (String pre : PIC_PREFIXES) {
            if (r.startsWith(pre)) {
                r = r.substring(pre.length()).trim();
            }
        }
        return r;
    }

    /**
     * 取图片列的第一个有效链接；一个规格只支持一张图，多填的静默忽略。无有效链接返回空串。
     */
    public static String firstPicUrl(String picUrl) {
        for (String url : splitPicUrls(picUrl)) {
            if (StringUtils.hasText(url)) {
                return url.trim();
            }
        }
        return "";
    }

    /**
     * 解析规格文本为 {@code [规格名, 规格值]} 列表，已 trim；名或值为空的组直接丢弃。
     */
    public static List<String[]> parseSpecPairs(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        List<String[]> out = new ArrayList<>();
        for (String part : raw.split("[;；,，]")) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            String[] nv = part.split("[:：]", 2);
            if (nv.length < 2) {
                continue;
            }
            String name = nv[0].trim();
            String value = nv[1].trim();
            if (!StringUtils.hasText(name) || !StringUtils.hasText(value)) {
                continue;
            }
            out.add(new String[]{name, value});
        }
        return out;
    }

    /**
     * 把规格对拼成落库用的标准规格串 {@code 颜色:红色|尺码:37}；空列表返回空串。
     */
    public static String buildSkuValue(List<String[]> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            return "";
        }
        return pairs.stream().map(p -> p[0] + ":" + p[1]).collect(Collectors.joining("|"));
    }

    /**
     * 把落库规格串 {@code 颜色:红色|尺码:37} 转回导入模板的规格文本 {@code 颜色:红色；尺码:37}。
     * <p>
     * 导出供再次导入时必须转换：{@link #parseSpecPairs} 按 {@code ; ； , ，} 分组，**不认 {@code |}**，
     * 直接把落库串写进 Excel 会让多组规格在重新导入时被解析成一组。
     */
    public static String toImportSpecText(String skuValue) {
        if (!StringUtils.hasText(skuValue)) {
            return "";
        }
        return skuValue.replace("|", "；");
    }
}
