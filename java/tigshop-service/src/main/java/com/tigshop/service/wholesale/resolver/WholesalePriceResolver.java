// **---------------------------------------------------------------------+
// ** 文件 -- WholesalePriceResolver
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+

package com.tigshop.service.wholesale.resolver;

import com.tigshop.bean.model.wholesale.ProductWholesalePrice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 批发价命中纯函数（design §4.1 / api-contract §B5 / step §TASK-WS-203）。
 *
 * <p>设计原则：纯函数，无 DB / 无 Spring / 无 Mapper 依赖。
 * 输入完整数据集（productId / skuId / gradeId / quantity / retailPrice / 全部候选 price 行），
 * 输出命中结果或 null。便于：
 * <ol>
 *   <li>单元测试覆盖笛卡尔积场景（SKU × Grade × Tier × Mode）</li>
 *   <li>购物车 B2 批量壳 W3 直接复用，零 DB 调用</li>
 * </ol>
 *
 * <p><b>等级锁定（类型售价 &gt; 通用售价）</b>：当前等级对该 SKU（含 SPU 兜底）只要存在<b>配了可用价格</b>
 * 的行，就只认本等级的行，通用价行全部剔除。即使数量未达该等级最低阶梯也<b>不回退通用价</b>，
 * 而是整体未命中、由上层按原始零售价结算——与后台「已配置类型售价时可不填通用售价」的口径一致。
 * 该等级完全没配、或只留下价格为 0 / 空的空壳行时，才使用通用价。
 *
 * <p>命中优先级（高 → 低，等级锁定后的同等级内部）：
 * <pre>
 *   特定SKU + 特定等级 + 满足 tier  >  特定SKU + 通用等级 + 满足 tier
 *   SPU兜底 + 特定等级 + 满足 tier  >  SPU兜底 + 通用等级 + 满足 tier
 * </pre>
 * 同一级内：tier_min_qty 越大优先级越高（取所有 qty ≥ tier_min_qty 的最大 tier）。
 *
 * <p>价格计算：
 * <ul>
 *   <li>{@code price_mode=1} 折扣：{@code finalPrice = retailPrice × discount / 10}（十分制，HALF_UP scale=2）</li>
 *   <li>{@code price_mode=2} 固定：{@code finalPrice = wholesale_price}（直接读，不二次换算）</li>
 * </ul>
 */
public class WholesalePriceResolver {

    private WholesalePriceResolver() {
        // 工具类，禁止实例化
    }

    /**
     * 解析入参。
     *
     * <p>调用方需保证 candidates 已按 productId 预过滤；本函数会再按 status=1 / is_delete=0
     * 兜底过滤一次，避免上游传入未过滤数据导致误命中。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolveInput {
        /** 商品 ID */
        private Integer productId;
        /** SKU ID（单规格商品传具体 SKU id；多规格商品传当前 SKU id） */
        private Integer skuId;
        /** 批发商等级 ID（0 或 null = 非批发商，将永远返回 null） */
        private Integer gradeId;
        /** 数量（≥1） */
        private Integer quantity;
        /** SKU 零售价（mode=1 折扣换算的基数） */
        private BigDecimal retailPrice;
        /** 候选 price 行（必须已按 productId 预过滤；本函数会按 status=1 / is_delete=0 兜底过滤一次） */
        private List<ProductWholesalePrice> candidates;
        /**
         * 阶梯匹配专用数量（CR-20260604-1430 混批语义）。
         *
         * <p>非空时用于阶梯档位过滤（{@code effectiveQty < tier_min_qty → skip}），
         * 否则回退用 {@link #quantity}。
         *
         * <ul>
         *   <li>SKU 级（multi_spec_price_mode=2）：调用方不传（或传 = quantity）→ 行为与历史 100% 一致</li>
         *   <li>商品级混批（multi_spec_price_mode=1）：调用方传 Q_total（同 product 下所有购买 SKU 数量之和），
         *       使各 SKU 用「全规格总数」命中各自阶梯档，但最终单价仍按本行自己的零售价 + 命中行折扣计算</li>
         * </ul>
         */
        private Integer tierMatchQty;
    }

    /**
     * 解析结果。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolveResult {
        /** 命中行 ID（便于审计 / 加车快照） */
        private Integer priceId;
        /** 最终单价（已按 mode 计算） */
        private BigDecimal finalPrice;
        /** 1=折扣 / 2=固定 */
        private Integer priceMode;
        /** 命中行的 discount（mode=1 时有意义） */
        private BigDecimal discount;
        /** 命中的阶梯起量 */
        private Integer tierMinQty;
        /** 命中等级（0=通用价） */
        private Integer hitGradeId;
        /** 命中 SKU（0=SPU 兜底） */
        private Integer hitSkuId;
    }

    /**
     * 纯函数解析批发价。未命中返回 null；回退到零售价的决策由上层业务决定。
     */
    public static ResolveResult resolve(ResolveInput in) {
        if (in == null) {
            return null;
        }
        if (in.candidates == null || in.candidates.isEmpty()) {
            return null;
        }
        if (in.quantity == null || in.quantity <= 0) {
            return null;
        }
        if (in.gradeId == null || in.gradeId <= 0) {
            return null;
        }
        if (in.retailPrice == null) {
            return null;
        }

        // 阶梯匹配数量：混批（CR-20260604-1430）传 tierMatchQty=Q_total；否则回退 quantity（SKU 级语义不变）
        int effectiveQty = (in.tierMatchQty != null && in.tierMatchQty > 0)
                ? in.tierMatchQty
                : in.quantity;

        // 1. 等级锁定：当前等级对该 SKU（含 SPU 兜底）存在有效配置时，只认本等级的行。
        // 即使当前数量未达该等级最低阶梯，也不跨等级回退通用价——「已配类型售价则不再看通用售价」，
        // 未命中由上层按原始零售价结算（product_wholesale_price 后台提示：类型售价 > 通用售价）。
        // 注意：价格为 0 / 空的等级行视为「未配置」，不参与锁定，否则会把通用价一并挡掉。
        boolean hasGradeSpecific = in.candidates.stream().anyMatch(p -> p != null
                && Objects.equals(p.getProductId(), in.productId)
                && (p.getSkuId() == null || p.getSkuId() == 0 || Objects.equals(p.getSkuId(), in.skuId))
                && p.getStatus() != null && p.getStatus() == 1
                && p.getIsDelete() != null && p.getIsDelete() == 0
                && isExactGrade(p.getGradeId(), in.gradeId)
                && hasUsablePrice(p));

        // 2. 过滤候选行
        List<ProductWholesalePrice> valid = new ArrayList<>();
        for (ProductWholesalePrice p : in.candidates) {
            if (p == null) {
                continue;
            }
            if (p.getProductId() == null || !p.getProductId().equals(in.productId)) {
                continue;
            }
            if (p.getStatus() == null || p.getStatus() != 1) {
                continue;
            }
            if (p.getIsDelete() == null || p.getIsDelete() != 0) {
                continue;
            }
            if (!matchesSku(p.getSkuId(), in.skuId)) {
                continue;
            }
            if (!matchesGrade(p.getGradeId(), in.gradeId)) {
                continue;
            }
            if (hasGradeSpecific && !isExactGrade(p.getGradeId(), in.gradeId)) {
                continue;
            }
            if (p.getTierMinQty() == null || p.getTierMinQty() <= 0) {
                continue;
            }
            if (effectiveQty < p.getTierMinQty()) {
                continue;
            }
            if (p.getPriceMode() == null || (p.getPriceMode() != 1 && p.getPriceMode() != 2)) {
                continue;
            }
            // 价格为 0 / 空的脏行不可命中（否则 mode=2 会算出 0 元、或 finalPrice 为 null 触发 NPE）
            if (!hasUsablePrice(p)) {
                continue;
            }
            valid.add(p);
        }

        if (valid.isEmpty()) {
            return null;
        }

        // 3. 排序：SKU 命中 > SPU 兜底；Grade 命中 > 通用；tier 大 > tier 小；同优先级取 id 小者
        valid.sort(buildPriorityComparator(in.skuId, in.gradeId));

        ProductWholesalePrice hit = valid.get(0);

        // 3. 计算 finalPrice
        BigDecimal finalPrice = computeFinalPrice(hit, in.retailPrice);

        return ResolveResult.builder()
                .priceId(hit.getId())
                .finalPrice(finalPrice)
                .priceMode(hit.getPriceMode())
                .discount(hit.getDiscount())
                .tierMinQty(hit.getTierMinQty())
                .hitGradeId(hit.getGradeId())
                .hitSkuId(hit.getSkuId())
                .build();
    }

    /**
     * sku 维度匹配：row.sku_id=0 视为 SPU 兜底，匹配任何 inSkuId；
     * row.sku_id=null 也视为 SPU 兜底（DB 列 NOT NULL，但防御）；
     * row.sku_id>0 时必须严格等于 inSkuId。
     */
    private static boolean matchesSku(Integer rowSkuId, Integer inSkuId) {
        if (rowSkuId == null || rowSkuId == 0) {
            return true;
        }
        return rowSkuId.equals(inSkuId);
    }

    /**
     * 等级维度匹配：row.grade_id=0 视为通用价，匹配任何 inGradeId；
     * row.grade_id=null 视为通用价（DB 列 NOT NULL，但防御）；
     * row.grade_id>0 时必须严格等于 inGradeId。
     */
    private static boolean matchesGrade(Integer rowGradeId, Integer inGradeId) {
        if (rowGradeId == null || rowGradeId == 0) {
            return true;
        }
        return rowGradeId.equals(inGradeId);
    }

    /**
     * 构造命中优先级 Comparator（升序，第一个 = 最优）。
     */
    private static Comparator<ProductWholesalePrice> buildPriorityComparator(
            final Integer inSkuId, final Integer inGradeId) {
        return (a, b) -> {
            // SKU 命中（=inSkuId）= 2，SPU 兜底（0 或 null）= 1，越大越优
            int aSku = isExactSku(a.getSkuId(), inSkuId) ? 2 : 1;
            int bSku = isExactSku(b.getSkuId(), inSkuId) ? 2 : 1;
            if (aSku != bSku) {
                return bSku - aSku;
            }
            // Grade 命中 = 2，通用 = 1
            int aGrade = isExactGrade(a.getGradeId(), inGradeId) ? 2 : 1;
            int bGrade = isExactGrade(b.getGradeId(), inGradeId) ? 2 : 1;
            if (aGrade != bGrade) {
                return bGrade - aGrade;
            }
            // tier 越大越优（满足越高门槛通常折扣越大）
            int tierCmp = Integer.compare(b.getTierMinQty(), a.getTierMinQty());
            if (tierCmp != 0) {
                return tierCmp;
            }
            // 同优先级（理论不出现，唯一键约束保护）→ id 小者胜，避免不确定性
            Integer aId = a.getId() != null ? a.getId() : Integer.MAX_VALUE;
            Integer bId = b.getId() != null ? b.getId() : Integer.MAX_VALUE;
            return Integer.compare(aId, bId);
        };
    }

    private static boolean isExactSku(Integer rowSkuId, Integer inSkuId) {
        return rowSkuId != null && rowSkuId > 0 && Objects.equals(rowSkuId, inSkuId);
    }

    private static boolean isExactGrade(Integer rowGradeId, Integer inGradeId) {
        return rowGradeId != null && rowGradeId > 0 && Objects.equals(rowGradeId, inGradeId);
    }

    /**
     * 该行是否配了可用价格：折扣模式看 {@code discount}，固定模式看 {@code wholesale_price}，
     * 均要求非空且大于 0。
     *
     * <p>后台允许只填类型售价或只填通用售价，存量数据里等级行可能留着 0 / NULL 的空壳。
     * 这类行既不参与等级锁定（否则会把通用价一并挡掉），也不可被命中（mode=2 会算出 0 元成交，
     * 或 {@code finalPrice} 为 null 在上层乘算时 NPE）。
     *
     * <p>公开供 admin 展示侧（{@code wholesalePriceDisplay} 价格区间）复用，
     * 确保「什么算配了价」在成交与展示两侧口径一致。
     */
    public static boolean hasUsablePrice(ProductWholesalePrice p) {
        if (p.getPriceMode() != null && p.getPriceMode() == 2) {
            return p.getWholesalePrice() != null
                    && p.getWholesalePrice().compareTo(BigDecimal.ZERO) > 0;
        }
        return p.getDiscount() != null && p.getDiscount().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 价格换算：
     * mode=1：retailPrice × discount / 10，HALF_UP scale=2（十分制，design §4 line 320）；
     * mode=2：直接返回 wholesalePrice（DB 列 DECIMAL(10,2)，无需再次 scale）。
     */
    private static BigDecimal computeFinalPrice(ProductWholesalePrice hit, BigDecimal retailPrice) {
        if (hit.getPriceMode() != null && hit.getPriceMode() == 2) {
            return hit.getWholesalePrice();
        }
        // mode=1 折扣
        BigDecimal d = hit.getDiscount() != null ? hit.getDiscount() : BigDecimal.ZERO;
        return retailPrice
                .multiply(d)
                .divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
    }
}
