package com.tigshop.utils;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.product.ProductType;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.product.Product;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tigshop.common.utils.HeaderUtils.getAdminType;
import static com.tigshop.common.utils.HeaderUtils.getShopId;

/**
 * 工具类
 *
 * @author kidd
 * @since 2025/8/25 14:38
 */
public class WrapperConditionUtils {

    private static final Pattern CONDITION_PATTERN =
            Pattern.compile("(\\w+)\\.(\\w+)\\s*(IN|NOT IN)\\s*\\(([^)]*)\\)|" +
                    "(\\w+)\\.(\\w+)\\s*(=|LIKE)\\s*\\?");

    // StoreProduct 可用字段白名单
    private static final Set<String> STORE_PRODUCT_COLUMNS = Set.of(
            "shop_id", "product_status", "product_name", "is_delete", "category_id", "shop_category_id",
            "is_best", "is_hot", "is_new", "brand_id", "check_status", "product_id", "product_type",
            "product_sn", "product_tsn"
    );

    public static <A> void copyConditionsWithOr(MPJLambdaWrapper<A> wrapper) {
        copyConditionsWithOr(wrapper, null, null);
    }

    public static <A> void copyConditionsWithOr(MPJLambdaWrapper<A> wrapper, Integer forceShopId) {
        copyConditionsWithOr(wrapper, forceShopId, null);
    }

    /**
     * 复制条件到 store_product 表并用 OR 连接
     * @param wrapper 查询条件
     * @param forceShopId 强制的 shop_id 条件，会加在 OR 分支内部
     * @param keyword 关键词；非空时在 store_product 的 OR 分支内以独立 OR 组同时匹配
     *                平台商品名(t.product_name)、门店商品名(sp2.product_name)、搜索关键词(keywords)、商品编码(product_sn)、TSN(product_tsn)
     */
    public static <A> void copyConditionsWithOr(MPJLambdaWrapper<A> wrapper, Integer forceShopId, String keyword) {
        wrapper.leftJoin(StoreProduct.class, "sp2", StoreProduct::getProductId, Product::getProductId);
        if (AdminTypeEnum.POS.getCode().equals(getAdminType())) {
            wrapper.isNotNull(getShopId() != null && getShopId() == 0, "sp2.product_id");
            wrapper.eq(getShopId() != null && getShopId() == 0, "sp2.shop_id", getShopId());
        }
        String sqlSegment = wrapper.getTargetSql();
        Map<String, Object> params = wrapper.getParamNameValuePairs();

        // 收集 SQL 中所有 ? 的全局位置
        List<Integer> qPosList = new ArrayList<>();
        for (int p = sqlSegment.indexOf('?'); p != -1; p = sqlSegment.indexOf('?', p + 1)) {
            qPosList.add(p);
        }

        Matcher matcher = CONDITION_PATTERN.matcher(sqlSegment);

        boolean hasKeyword = keyword != null && !keyword.isEmpty();

        wrapper.or(w -> {
            // 收银台且有强制 shopId 时，在 OR 分支内部加上 sp2.shop_id 条件
            if (forceShopId != null && forceShopId > 0) {
                w.eq(StoreProduct::getShopId, forceShopId);
                w.eq(Product::getProductId,forceShopId);
            }

            // 关键词：在 store_product OR 分支内以独立 OR 组统一处理，
            // 同时匹配平台商品名(t.product_name)、门店商品名(sp2.product_name)、搜索关键词(keywords)、商品编码(product_sn)、TSN(product_tsn)。
            // 避免被下方正则逐列拷贝时错误地拍平成 AND，导致平台商品(shop_id=0)在 searchShop 场景下搜不到。
            if (hasKeyword) {
                w.and(k -> k.like(StoreProduct::getProductName, keyword)
                        .or().like(Product::getProductName, keyword)
                        .or().like(Product::getKeywords, keyword)
                        .or().like(Product::getProductSn, keyword)
                        .or().like(Product::getProductTsn, keyword));
            }

            Set<String> columnSet = new HashSet<>();
            while (matcher.find()) {

                // ---- 处理 IN / NOT IN (...) ----
                if (matcher.group(1) != null) {
                    String operator = matcher.group(3).trim().toUpperCase(); // IN 或 NOT IN
                    String column = matcher.group(2);
                    String inParams = matcher.group(4);

                    if (!STORE_PRODUCT_COLUMNS.contains(column)) {
                        continue; // 跳过 Product 独有字段
                    }

                    // 计算 ? 的个数并取对应值
                    int startPos = matcher.start(4);
                    int countQ = inParams.split(",").length;
                    List<Object> values = new ArrayList<>();
                    for (int k = 0; k < countQ; k++) {
                        int qPos = sqlSegment.indexOf("?", startPos);
                        int idx = qPosList.indexOf(qPos); // 0-based
                        String paramKey = "MPGENVAL" + (idx + 1);
                        values.add(params.get(paramKey));
                        startPos = qPos + 1;
                    }

                    // 根据操作符选择 in 或 notIn
                    if ("IN".equals(operator)) {
                        if (ObjectUtil.equals(column, "shop_category_id")) {
                            w.in("sp2." + column, values);
                        } else {
                            w.in("t." + column, values);
                        }
                    } else if ("NOT IN".equals(operator)) {
                        // product_type 只存在于 product 表，不存在于 store_product 表
                        if ("product_type".equals(column)) {
                            w.notIn("t." + column, values);
                        } else {
                            w.notIn("sp2." + column, values);
                        }
                    }

                    columnSet.add(column);
                }

                // ---- 处理 = ? / LIKE ? ----
                if (matcher.group(5) != null) {
                    String column = matcher.group(6);
                    if (column == null) {
                        continue;
                    }
                    String operator = matcher.group(7);

                    if (!STORE_PRODUCT_COLUMNS.contains(column)) {
                        continue; // 跳过 Product 独有字段
                    }

                    // 关键词列已在上方独立 OR 组统一处理，这里跳过，避免重复及被拍平成 AND
                    if (hasKeyword && ("product_name".equals(column)
                            || "product_sn".equals(column)
                            || "product_tsn".equals(column))) {
                        continue;
                    }

                    int qPos = sqlSegment.indexOf("?", matcher.start(7));
                    int idx = qPosList.indexOf(qPos); // 0-based
                    String paramKey = "MPGENVAL" + (idx + 1);
                    Object value = params.get(paramKey);

                    switch (column) {
                        case "shop_id" -> w.eq(StoreProduct::getShopId, value);
                        case "product_status" ->
                                w.apply("(CASE WHEN t.product_status = 1 AND sp2.product_status = 1 THEN 1 ELSE 0 END) = {0}", value);
                        case "product_name" -> w.like(StoreProduct::getProductName, value);
                        case "is_delete" -> w.eq(StoreProduct::getIsDelete, value);
                        case "category_id" -> w.eq(Product::getCategoryId, value);
                        case "shop_category_id" -> w.eq(StoreProduct::getShopCategoryId, value);
                        case "is_best" -> w.eq(Product::getIsBest, value);
                        case "is_hot" -> w.eq(Product::getIsHot, value);
                        case "is_new" -> w.eq(Product::getIsNew, value);
                        case "brand_id" -> w.eq(Product::getBrandId, value);
                        case "check_status" -> w.eq(Product::getCheckStatus, value);
                        case "product_sn" -> w.like(Product::getProductSn, value);
                        case "product_tsn" -> w.like(Product::getProductTsn, value);
                        default -> {
                            if ("LIKE".equalsIgnoreCase(operator)) {
                                w.like(column, value);
                            } else {
                                w.eq(column, value);
                            }
                        }
                    }
                    columnSet.add(column);
                }
            }
        });
    }


    public static <A, B> Map<SFunction<A, ?>, SFunction<B, ?>> fieldMap() {
        return new HashMap<>();
    }

}
