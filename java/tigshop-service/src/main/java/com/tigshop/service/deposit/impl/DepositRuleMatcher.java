package com.tigshop.service.deposit.impl;

import com.tigshop.bean.enums.deposit.DepositOwnerType;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.mapper.deposit.ShopDepositMapper;
import com.tigshop.mapper.deposit.ShopDepositRuleMapper;
import com.tigshop.mapper.merchant.MerchantMapper;
import com.tigshop.mapper.product.CategoryMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 保证金应缴自动匹配器（A 方案，统一 owner 维度）。
 *
 * <p>应缴 R = owner (主体类型 × 持有者类型 × 在售商品类目三级就近 × 当前生效期) 命中规则，
 * 全部在售商品取最高额；无在售 / 无命中 = 0。仅在 {@code required_manual} 为 NULL 时调用。</p>
 *
 * <p>在售商品来源按 owner_type 分派：店铺取 {@code product.shop_id}（主体类型 = merchant.type）；
 * 供应商取 {@code vendor_product.vendor_id}（主体类型 = vendor.type）。规则匹配的"店铺类型"维度
 * 直接用 owner_type 同码值（1店铺 2供应商，见 {@code DepositRuleShopType}）。</p>
 *
 * @author Tigshop团队
 */
@Component
public class DepositRuleMatcher {

    /** 类目就近匹配的最大溯源层级（三级 > 二级 > 一级）。 */
    private static final int MAX_CATEGORY_LEVEL = 3;

    @Resource
    private ShopDepositMapper shopDepositMapper;

    @Resource
    private ShopDepositRuleMapper shopDepositRuleMapper;

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private VendorMapper vendorMapper;

    /**
     * 自动应缴：owner 全部在售商品按三级就近命中规则取最高额。无在售 / 无主体 / 无命中返回 0。
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为 shopId，供应商为 vendorId）
     */
    public BigDecimal maxRequired(Integer ownerType, Integer ownerId) {
        boolean supplier = DepositOwnerType.SUPPLIER.getCode().equals(ownerType);
        // 在售商品类目来源按 owner_type 分派
        List<Integer> leafCategoryIds = supplier
                ? shopDepositMapper.selectVendorOnSaleCategoryIds(ownerId)
                : shopDepositMapper.selectOnSaleCategoryIds(ownerId);
        if (leafCategoryIds == null || leafCategoryIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        // 主体类型来源按 owner_type 分派：店铺 merchant.type / 供应商 vendor.type
        Integer merchantType = supplier ? resolveVendorType(ownerId) : resolveMerchantType(ownerId);
        if (merchantType == null) {
            return BigDecimal.ZERO;
        }
        // 规则匹配的"店铺类型"维度直接用 owner_type 同码值（1店铺 2供应商）
        Integer shopType = ownerType;

        // 为每个在售商品类目构建就近溯源链（自身 -> 父 -> 祖父，最多三级），并汇总候选类目集合
        Map<Integer, List<Integer>> chains = new LinkedHashMap<>();
        Set<Integer> candidateCategoryIds = new HashSet<>();
        Map<Integer, Category> categoryCache = new HashMap<>();
        for (Integer leaf : leafCategoryIds) {
            if (leaf == null || leaf <= 0 || chains.containsKey(leaf)) {
                continue;
            }
            List<Integer> chain = buildCategoryChain(leaf, categoryCache);
            chains.put(leaf, chain);
            candidateCategoryIds.addAll(chain);
        }
        if (candidateCategoryIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long now = System.currentTimeMillis() / 1000L;
        List<Map<String, Object>> rows = shopDepositRuleMapper.matchMaxByCategories(
                merchantType, shopType, new ArrayList<>(candidateCategoryIds), now);
        if (rows == null || rows.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Map<Integer, BigDecimal> hitByCategory = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer categoryId = toInteger(row.get("categoryId"));
            BigDecimal amount = toBigDecimal(row.get("depositAmount"));
            if (categoryId != null && amount != null) {
                hitByCategory.put(categoryId, amount);
            }
        }
        if (hitByCategory.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 每个商品按就近链从近到远找首个命中类目，全店取最高
        BigDecimal max = null;
        for (List<Integer> chain : chains.values()) {
            for (Integer categoryId : chain) {
                BigDecimal amount = hitByCategory.get(categoryId);
                if (amount != null) {
                    if (max == null || amount.compareTo(max) > 0) {
                        max = amount;
                    }
                    break;
                }
            }
        }
        return max == null ? BigDecimal.ZERO : max;
    }

    /**
     * 店铺主体类型：shop -> merchant.type（1个人 2企业，复用 MerchantType）。无法解析返回 null。
     */
    private Integer resolveMerchantType(Integer shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getMerchantId() == null || shop.getMerchantId() <= 0) {
            return null;
        }
        Merchant merchant = merchantMapper.selectById(shop.getMerchantId());
        if (merchant == null) {
            return null;
        }
        return merchant.getType();
    }

    /**
     * 供应商主体类型：vendor.type（1个人 2企业，复用 MerchantType 同码）。无法解析返回 null。
     */
    private Integer resolveVendorType(Integer vendorId) {
        Vendor vendor = vendorMapper.selectOnboardedById(vendorId);
        if (vendor == null) {
            return null;
        }
        return vendor.getType();
    }

    /**
     * 构建类目就近溯源链：自身 -> 父 -> 祖父，最多 {@link #MAX_CATEGORY_LEVEL} 级。
     */
    private List<Integer> buildCategoryChain(Integer categoryId, Map<Integer, Category> categoryCache) {
        List<Integer> chain = new ArrayList<>(MAX_CATEGORY_LEVEL);
        Integer current = categoryId;
        for (int level = 0; level < MAX_CATEGORY_LEVEL && current != null && current > 0; level++) {
            chain.add(current);
            Category category = categoryCache.computeIfAbsent(current, categoryMapper::selectById);
            if (category == null) {
                break;
            }
            Integer parentId = category.getParentId();
            if (parentId == null || parentId <= 0 || chain.contains(parentId)) {
                break;
            }
            current = parentId;
        }
        return chain;
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(value.toString());
    }
}
