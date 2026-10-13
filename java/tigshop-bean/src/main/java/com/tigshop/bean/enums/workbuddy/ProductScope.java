package com.tigshop.bean.enums.workbuddy;

import java.util.Arrays;
import java.util.Locale;

/**
 * 商品查询范围，对应后台商品列表的页签。
 *
 * <p>后台的每个页签本质是 {@code productStatus}、{@code checkStatus}、{@code isDelete}
 * 三个参数的固定组合。若把这三个参数原样暴露给调用方自行拼装，最易出错的是回收站——
 * 拼成 {@code isDelete=1} 却遗留 {@code productStatus=1}，查出的结果静默偏少且无人察觉。
 * 故收敛成一个取值为商家口语说法的枚举，由服务端负责翻译。
 *
 * <p>各组合取自后台 {@code List.vue} 的 onTabChange，与后台页签行为保持一致。
 */
public enum ProductScope {

    /** 出售中：已上架且审核通过。 */
    ON_SALE("出售中", 1, 1, 0),

    /** 已下架：审核通过但未上架。 */
    OFF_SALE("已下架", 0, 1, 0),

    /** 待审核：不限上下架状态。 */
    PENDING_AUDIT("待审核", null, 0, 0),

    /** 审核未通过：不限上下架状态。 */
    AUDIT_FAILED("审核未通过", null, 2, 0),

    /**
     * 回收站：只按删除标记筛，不带上下架与审核条件。
     *
     * <p>带上了会漏——回收站里既有原本在售的，也有原本下架的。
     */
    RECYCLED("回收站", null, null, 1),

    /** 全部：不限上下架与审核状态，但不含回收站。商家说"所有商品"时不包括已删除的。 */
    ALL("全部", null, null, 0);

    private final String label;
    private final Integer productStatus;
    private final Integer checkStatus;
    private final Integer isDelete;

    ProductScope(String label, Integer productStatus, Integer checkStatus, Integer isDelete) {
        this.label = label;
        this.productStatus = productStatus;
        this.checkStatus = checkStatus;
        this.isDelete = isDelete;
    }

    /** 中文名，用于摘要回显查询范围，避免把 scope 原样念给用户听。 */
    public String getLabel() {
        return label;
    }

    /** 为 null 表示不加该条件。 */
    public Integer getProductStatus() {
        return productStatus;
    }

    public Integer getCheckStatus() {
        return checkStatus;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    /** 未指定时按"出售中"，与后台列表页的默认页签一致。 */
    public static ProductScope DEFAULT() {
        return ON_SALE;
    }

    /**
     * 宽松解析：大小写与中划线都接受。
     *
     * @return 传入为空时返回默认范围；无法识别时返回 {@code null}，由调用方给出可读错误
     */
    public static ProductScope parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT();
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return Arrays.stream(values())
                .filter(scope -> scope.name().equals(normalized))
                .findFirst()
                .orElse(null);
    }

    /** 供错误提示列出合法取值，不必在调用处手写一份易过期的清单。 */
    public static String supportedValues() {
        return Arrays.stream(values())
                .map(scope -> scope.name().toLowerCase(Locale.ROOT) + "（" + scope.label + "）")
                .reduce((a, b) -> a + "、" + b)
                .orElse("");
    }
}
