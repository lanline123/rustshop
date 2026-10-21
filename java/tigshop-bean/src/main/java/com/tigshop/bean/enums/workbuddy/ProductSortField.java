package com.tigshop.bean.enums.workbuddy;

import java.util.Arrays;
import java.util.Locale;

/**
 * 商品列表允许的排序字段。
 *
 * <p>底层 {@code BaseServiceImpl.buildSortOrder} 对无法识别的字段**静默回退到主键**，
 * 不报错。调用方按"价格从低到高"请求却拿到按 ID 排的结果，自己无从察觉，
 * 转述给用户就是一份错误的榜单。故在此白名单化，非法取值直接拒绝。
 */
public enum ProductSortField {

    /** 商品 ID，近似等于添加先后。 */
    ADDED("添加时间", "productId"),

    PRICE("价格", "productPrice"),

    STOCK("库存", "productStock"),

    SALES("销量", "virtualSales"),

    /** 后台列表的人工排序值。 */
    SORT_ORDER("排序值", "sortOrder");

    private final String label;
    private final String property;

    ProductSortField(String label, String property) {
        this.label = label;
        this.property = property;
    }

    public String getLabel() {
        return label;
    }

    /** 实体属性名；底层再据此转成下划线列名。 */
    public String getProperty() {
        return property;
    }

    /**
     * @return 传入为空时返回 {@code null}（表示不指定排序，沿用底层默认）；
     *         无法识别时抛给调用方处理，故同样返回 {@code null} 前需先判空
     */
    public static ProductSortField parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return Arrays.stream(values())
                .filter(field -> field.name().equals(normalized))
                .findFirst()
                .orElse(null);
    }

    public static String supportedValues() {
        return Arrays.stream(values())
                .map(field -> field.name().toLowerCase(Locale.ROOT) + "（" + field.label + "）")
                .reduce((a, b) -> a + "、" + b)
                .orElse("");
    }
}
