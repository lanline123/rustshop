// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.wholesale;

import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.bean.model.wholesale.ProductWholesaleSkuConfig;

import java.util.Map;

/**
 * 起批量 + 倍数购买纯函数校验器（design §4.3 / §4.4 / §4.5）。
 *
 * <p>本类零 DB / 零 Spring 依赖，便于单测覆盖。供以下场景共用：
 * <ul>
 *   <li>加车 P3（{@code CartServiceImpl.addProductToCart}）硬阻断：不达标抛 5320 / 5321。</li>
 *   <li>购物车列表（{@code CartWholesaleEnhancer.applyLine}）软提示：行级 / 阶梯凑单文案。</li>
 *   <li>下单（{@code OrderCheckServiceImpl}）二次硬校验（防止从加车到下单期间配置变更）。</li>
 * </ul>
 *
 * <p>核心约束（CR-20260604-0937：起批数统一字段 {@code config.moq}）：
 * <ul>
 *   <li>{@code moq_dimension=1}（SPU 维度）：同 product 所有批发行加总数量 &ge; {@code config.moq}。</li>
 *   <li>{@code moq_dimension=2}（SKU 维度）：每 SKU 数量 &ge; {@code config.moq}（所有 SKU 共用同一起批数）。</li>
 *   <li>{@code multiple}（倍数购买）：永远按 SKU 校验，与起批维度正交（C12）。
 *       {@code multiple ≤ 1} 时不校验，仍读 {@link ProductWholesaleSkuConfig}。</li>
 *   <li>所有方法返回 {@code null} 表示通过，否则返回行/组级展示文案。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
public final class WholesaleBuyRuleValidator {

    /** moq_dimension=1：SPU 维度起批量 */
    private static final int MOQ_DIMENSION_SPU = 1;
    /** moq_dimension=2：SKU 维度起批量 */
    private static final int MOQ_DIMENSION_SKU = 2;

    /** sku_id=0：SPU 兜底（单规格 / 全 SKU 通配），与批发价 resolver 同语义 */
    private static final int SKU_SPU_FALLBACK = 0;

    private WholesaleBuyRuleValidator() {
        // 工具类，禁止实例化
    }

    /**
     * C 端渠道可用性：该批发配置在当前浏览的店铺上下文中是否对买家生效。
     *
     * <p>按产品口径「门店新增的商品才有批发价，平台分配的没有」。O2O 下判定依据是
     * <b>商品归属与当前店铺上下文是否一致</b>（{@code config.shop_id} 冗余自
     * {@code product.shop_id}）：
     * <ul>
     *   <li>官方自营店（上下文 {@code shopId=0}）卖平台商品（归属 0）→ 一致，享批发价。
     *       自营店本身 shopId 即为 0，不能仅凭「归属=0」就判为平台分配。</li>
     *   <li>连锁门店（上下文 {@code shopId=66}）卖平台分配商品（归属 0）→ 不一致，不享。</li>
     *   <li>连锁门店卖自建商品（归属 66）→ 一致，享批发价。</li>
     * </ul>
     *
     * <p>非 O2O 版本（B2B2C / S2B2C 等）无「门店分配」概念，平台商品批发是本功能的原始
     * 主场景（CD-031），故一律放行、行为不变。取不到店铺上下文时同样放行，避免误伤。
     *
     * @param config         批发配置（{@code null} 表示未配置批发能力 → false）
     * @param o2oChannel     当前是否 O2O 形态（调用方读 {@code tigshopProperties.getIsO2o()}）
     * @param contextShopId  当前浏览的店铺/门店 ID（{@code null} = 无上下文，放行）
     * @return true=该配置在当前上下文对 C 端生效
     */
    public static boolean isClientEligible(ProductWholesaleConfig config, boolean o2oChannel,
                                           Integer contextShopId) {
        if (config == null) {
            return false;
        }
        if (!o2oChannel || contextShopId == null) {
            return true;
        }
        int owner = config.getShopId() == null ? 0 : config.getShopId();
        return owner == contextShopId;
    }

    /**
     * SKU 级配置命中（精确 SKU 优先，未命中回退 SPU 兜底 sku_id=0）。
     *
     * <p>背景：单规格商品加车时购物车行的 skuId 会被解析成该商品唯一 SKU 的真实 id（&gt;0，
     * 见 {@code CartServiceImpl#addProductToCart}），而后台保存倍数时单规格按 {@code sku_id=0}
     * 录入（SPU 兜底，与批发价 {@code SKU_SPU_FALLBACK} 一致）。因此命中需「精确 SKU → 0 兜底」
     * 两段查找，否则单规格倍数永远匹配不到。多规格按具体 SKU 录入时精确命中优先。
     *
     * @param skuConfigMap key 格式 {@code "{productId}:{skuId}"} 的配置表（可空）
     * @param productId    商品 ID（null 直接返回 null）
     * @param skuId        购物车行真实 SKU id
     * @return 命中的配置；都未命中返回 null
     */
    public static ProductWholesaleSkuConfig resolveSkuConfig(
            Map<String, ProductWholesaleSkuConfig> skuConfigMap, Integer productId, Integer skuId) {
        if (skuConfigMap == null || productId == null) {
            return null;
        }
        ProductWholesaleSkuConfig exact = skuConfigMap.get(productId + ":" + skuId);
        if (exact != null) {
            return exact;
        }
        return skuConfigMap.get(productId + ":" + SKU_SPU_FALLBACK);
    }

    /**
     * 单行倍数购买校验（{@code multiple>1} 时 quantity 必须整除 multiple，design C12）。
     *
     * <p>例如 multiple=6，quantity=5 → 返回 "需为 6 的倍数（当前 5）"；quantity=12 → 返回 null。
     * skuConfig 为 null 或 multiple 缺失 / ≤1 时一律通过（无倍数约束）。
     *
     * @param quantity  本 SKU 行数量
     * @param skuConfig SKU 级配置（含 multiple；null = 未配置）
     * @return null=通过，否则文案
     */
    public static String checkMultiple(int quantity, ProductWholesaleSkuConfig skuConfig) {
        if (skuConfig == null) {
            return null;
        }
        Integer multipleBoxed = skuConfig.getMultiple();
        if (multipleBoxed == null || multipleBoxed <= 1) {
            return null;
        }
        int m = multipleBoxed;
        if (quantity % m != 0) {
            return "需为 " + m + " 的倍数（当前 " + quantity + "）";
        }
        return null;
    }

    /**
     * 倍数购买向上取整对齐（CR-20260612-1622 决策3）。
     *
     * <p>{@code multiple>1} 时返回 {@code ⌈quantity/multiple⌉ × multiple}（向上取整到最近倍数）；
     * 否则原值返回。例如 multiple=3：quantity=2 → 3、quantity=4 → 6、quantity=6 → 6（已对齐不变）。
     * skuConfig 为 null 或 multiple 缺失 / ≤1 时返回原 quantity（无倍数约束）。
     *
     * <p><b>仅对齐倍数，不连带起批量</b>（CR-20260612-1622 决策1）：起批量是否达标由
     * {@link #checkMoqSku} / {@link #checkMoqSpu} 独立判定，本方法不参与。返回值恒 {@code ≥ quantity}。
     *
     * @param quantity  当前数量
     * @param skuConfig SKU 级配置（含 multiple；null = 未配置）
     * @return 对齐后数量（≥ quantity；无约束时原值返回）
     */
    public static int alignUpToMultiple(int quantity, ProductWholesaleSkuConfig skuConfig) {
        if (skuConfig == null || quantity <= 0) {
            return quantity;
        }
        Integer multipleBoxed = skuConfig.getMultiple();
        if (multipleBoxed == null || multipleBoxed <= 1) {
            return quantity;
        }
        int m = multipleBoxed;
        int remainder = quantity % m;
        if (remainder == 0) {
            return quantity;
        }
        return quantity + (m - remainder);
    }

    /**
     * SKU 维度起批量校验（{@code moq_dimension=2} 时每 SKU 独立判定）。
     *
     * <p>CR-20260604-0937：签名由原 {@code (qty, ProductWholesaleSkuConfig)} 改为
     * {@code (qty, ProductWholesaleConfig)}，读取统一的 {@code config.moq}。
     *
     * <p>{@code config} 为 null 或 {@code moq_dimension != 2} 时返回 null（非 SKU 维度由
     * {@link #checkMoqSpu} 负责）。{@code moq} 为 null 时按 1 处理（实际意味无限制）。
     *
     * @param quantity 本 SKU 行数量
     * @param config   商品级配置（含 moq_dimension / moq；null = 未配置批发）
     * @return null=通过，否则文案 "该规格还差 N 件凑齐起批量 M 件"
     */
    public static String checkMoqSku(int quantity, ProductWholesaleConfig config) {
        if (config == null) {
            return null;
        }
        Integer dim = config.getMoqDimension();
        if (dim == null || dim != MOQ_DIMENSION_SKU) {
            return null;
        }
        int moq = config.getMoq() == null ? 1 : config.getMoq();
        if (moq <= 1) {
            return null;
        }
        if (quantity < moq) {
            return "该规格还差 " + (moq - quantity) + " 件凑齐起批量 " + moq + " 件";
        }
        return null;
    }

    /**
     * SPU 维度起批量校验（{@code moq_dimension=1} 时同 product 批发行加总判定）。
     *
     * <p>{@code config} 为 null 或 {@code moq_dimension != 1} 时返回 null（非 SPU 维度由
     * {@link #checkMoqSku} 负责）。{@code moq} 为 null 时按 1 处理（实际意味无限制）。
     *
     * @param totalQtyInSpu 同 product 全部批发行加总数量
     * @param config        商品级配置（含 moq_dimension / moq；null = 未配置批发）
     * @return null=通过，否则文案 "该商品还差 N 件凑齐起批量 M 件"
     */
    public static String checkMoqSpu(int totalQtyInSpu, ProductWholesaleConfig config) {
        if (config == null) {
            return null;
        }
        Integer dim = config.getMoqDimension();
        if (dim == null || dim != MOQ_DIMENSION_SPU) {
            return null;
        }
        int moq = config.getMoq() == null ? 1 : config.getMoq();
        if (moq <= 1) {
            return null;
        }
        if (totalQtyInSpu < moq) {
            return "该商品还差 " + (moq - totalQtyInSpu) + " 件凑齐起批量 " + moq + " 件";
        }
        return null;
    }

    /**
     * 阶梯凑单提示（design §4.5）：命中阶梯但未达更高一档时输出 "还差 N 件凑齐..." 文案。
     *
     * <p>供列表软提示展示，加车 / 下单硬校验链路不调用本方法。
     *
     * <p>{@code nextTierMinQty} 为 null 或 &le; quantity 时返回 null（已达更高档或无更高档）。
     *
     * @param quantity       当前数量
     * @param nextTierMinQty 下一阶梯起量（null = 已到顶档或无阶梯）
     * @param benefitText    更高档收益文案（可选，例如 "享受 4 折"；null 不附加）
     * @return null=无凑单提示，否则文案
     */
    public static String tierHint(int quantity, Integer nextTierMinQty, String benefitText) {
        if (nextTierMinQty == null || nextTierMinQty <= quantity) {
            return null;
        }
        int gap = nextTierMinQty - quantity;
        StringBuilder sb = new StringBuilder();
        sb.append("还差 ").append(gap).append(" 件凑齐阶梯 ").append(nextTierMinQty).append(" 件");
        if (benefitText != null && !benefitText.isEmpty()) {
            sb.append("，").append(benefitText);
        }
        return sb.toString();
    }
}
