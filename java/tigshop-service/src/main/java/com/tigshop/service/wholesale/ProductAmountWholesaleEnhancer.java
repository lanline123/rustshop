// **---------------------------------------------------------------------+
// ** 文件 -- ProductAmountWholesaleEnhancer
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+

package com.tigshop.service.wholesale;

import com.tigshop.bean.dto.product.ProductAmountDTO;
import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.bean.model.wholesale.ProductWholesalePrice;
import com.tigshop.bean.model.wholesale.ProductWholesaleSkuConfig;
import com.tigshop.bean.model.wholesale.Wholesaler;
import com.tigshop.bean.vo.product.ProductAmountVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.service.wholesale.resolver.WholesalePriceResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 商品金额预览批发增强器（CR-20260604-1015）。
 *
 * <p>专属 {@code POST /api/product/product/getProductAmount} 接口：用户调整数量时实时拿
 * {@code total}（购物车 / 下单成交价口径）。
 *
 * <p>设计原则：
 * <ul>
 *   <li><b>旁路非破坏</b>：Controller 已用零售路径算好 {@code retailTotal}；本组件仅在批发场景
 *       覆写 {@link ProductAmountVO#setTotal} + 追加 3 个 @vort-if 字段。</li>
 *   <li><b>静默回退</b>：未登录 / 非批发商 / 商品未配置批发（CR-20260610-162600 起店铺商品已放开）
 *       → 不改 vo（{@code wholesale=null / wholesaleSubtotal=null / hint=null}），与现状一致。</li>
 *   <li><b>软提示语义</b>：起批量 / 倍数未达不抛 5320/5321，仅写 {@link ProductAmountVO#setHint};
 *       真正成交校验仍在 {@link CartWholesaleEnhancer#markAndValidateOnAdd}
 *       与 {@code OrderCheckServiceImpl.validateWholesaleSubmitRules}。</li>
 *   <li><b>价格口径</b>：逐 SKU 行调 {@link WholesalePriceResolver#resolve}（与购物车 / 下单完全一致）；
 *       命中 → 用 tier 价 × num，未命中 → 用零售价 × num。</li>
 * </ul>
 *
 * <p>DB 成本（单 product 单次调用）：
 * <ol>
 *   <li>{@link WholesalerService#isWholesaleBuyer}（Redis 缓存命中 → 0 DB）</li>
 *   <li>{@link WholesalerService#getByUserId}（取 grade_id）</li>
 *   <li>{@link WholesaleProductConfigService#getActiveByProductId}（1 DB）</li>
 *   <li>{@link WholesalePriceService#listByProductId}（1 DB）</li>
 *   <li>{@link WholesaleSkuConfigService#mapByProductSkuKeys}（1 DB；倍数提示用）</li>
 * </ol>
 *
 * @author Tigshop团队
 * @create 2026-06-04
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAmountWholesaleEnhancer {

    /** moq_dimension：1=SPU 起批 */
    private static final int MOQ_DIM_SPU = 1;
    /** multi_spec_price_mode=1：商品级混批（按全规格总数 Q_total 查档，CR-20260604-1430）。 */
    private static final int MULTI_SPEC_MODE_MIX = 1;

    private final WholesalerService wholesalerService;
    private final WholesalePriceService priceService;
    private final WholesaleProductConfigService configService;
    private final WholesaleSkuConfigService skuConfigService;
    private final TigshopProperties tigshopProperties;

    /** 当前是否 O2O 形态（门店渠道）。 */
    private boolean isO2oChannel() {
        return tigshopProperties != null && tigshopProperties.getIsO2o() != null
                && tigshopProperties.getIsO2o() == 1;
    }

    /**
     * 批发增强 {@link ProductAmountVO}（旁路覆写：仅在批发场景生效）。
     *
     * <p>调用方约定：
     * <ol>
     *   <li>{@code vo} 已由 Controller 用零售路径算好 {@code total / count}；本方法可能覆写 {@code total}。</li>
     *   <li>{@code retailPriceBySkuId} 必须与 {@code dto.skuItem[].skuId} 一一对应，
     *       value 是 {@code productService.getProductSkuDetail(...).getPrice()}（零售路径最终价）。</li>
     *   <li>{@code userId} 由调用方通过 {@code SecurityUtils.getCurrentUserId()} 获取；
     *       {@code null} 或 {@code <=0} 视为未登录 → 静默回退。</li>
     * </ol>
     *
     * @param vo                  待增强的 VO（命中时被原地修改）
     * @param dto                 入参（取 {@code id / shopId / skuItem[]}）
     * @param retailPriceBySkuId  零售价映射：skuId → 零售路径最终价（含会员等级折扣，用于未命中行的零售 fallback）
     * @param userId              当前登录用户 ID
     */
    public void enhance(ProductAmountVO vo,
                        ProductAmountDTO dto,
                        Map<Integer, BigDecimal> retailPriceBySkuId,
                        Integer userId) {
        enhance(vo, dto, retailPriceBySkuId, null, userId);
    }

    /**
     * 批发增强（带批发折扣基数映射）。
     *
     * <p>CR-20260604-1015 修复（折上折）：批发折扣 mode=1 的基数必须是<b>原始 SKU 零售价</b>
     * （与加车 {@code Cart.originalPrice = sku.skuPrice} / 详情阶梯 {@code item.productPrice} 同源），
     * 而不是 {@code retailPriceBySkuId} 里已含会员等级折扣的价（否则会员折扣与批发折扣叠加，预览价低于真实成交价）。
     *
     * <ul>
     *   <li>命中批发行：折扣基数取 {@code wholesaleBasePriceBySkuId}；缺失该 SKU 时回退 {@code retailPriceBySkuId}。</li>
     *   <li>未命中行（零售）：仍用 {@code retailPriceBySkuId}（会员价），与购物车口径一致。</li>
     * </ul>
     *
     * @param wholesaleBasePriceBySkuId 批发折扣基数映射：skuId → 原始 SKU 零售价（{@code null} 时整体回退 retail）
     */
    public void enhance(ProductAmountVO vo,
                        ProductAmountDTO dto,
                        Map<Integer, BigDecimal> retailPriceBySkuId,
                        Map<Integer, BigDecimal> wholesaleBasePriceBySkuId,
                        Integer userId) {
        // ---------------- 守卫 ----------------
        if (vo == null || dto == null || dto.getId() == null) {
            return;
        }
        if (userId == null || userId <= 0) {
            return;
        }
        List<ProductAmountDTO.SkuItem> items = dto.getSkuItem();
        if (items == null || items.isEmpty()) {
            return;
        }
        // CR-20260610-162600 / CD-039：店铺商品批发已放开（原 CD-031 平台守卫移除）。
        // 平台 + 店铺 + O2O 门店上下文统一按 productId 解析；未配置批发的商品在下方 config==null 静默回退。
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            return;
        }
        Wholesaler buyer = wholesalerService.getByUserId(userId);
        if (buyer == null || buyer.getGradeId() == null || buyer.getGradeId() <= 0) {
            log.debug("[wholesale-amount] enhance: invalid wholesaler grade for userId={}, fallback to retail", userId);
            return;
        }
        Integer productId = dto.getId();
        ProductWholesaleConfig config = configService.getActiveByProductId(productId);
        if (config == null) {
            // 商品未配置批发能力 → 保持零售
            return;
        }
        // O2O 渠道：仅当商品归属与当前门店上下文一致才享批发（平台分配到门店的商品不享）
        if (!WholesaleBuyRuleValidator.isClientEligible(config, isO2oChannel(), dto.getShopId())) {
            return;
        }

        // ---------------- 数据预取 ----------------
        Integer gradeId = buyer.getGradeId();
        List<ProductWholesalePrice> candidates = priceService.listByProductId(productId);
        Map<String, ProductWholesaleSkuConfig> skuConfigMap =
                skuConfigService.mapByProductSkuKeys(Collections.singletonList(productId));
        if (skuConfigMap == null) {
            skuConfigMap = Collections.emptyMap();
        }
        if (retailPriceBySkuId == null) {
            retailPriceBySkuId = Collections.emptyMap();
        }
        if (wholesaleBasePriceBySkuId == null) {
            wholesaleBasePriceBySkuId = Collections.emptyMap();
        }

        // ---------------- 逐行 resolve + 聚合 ----------------
        BigDecimal newTotal = BigDecimal.ZERO;
        BigDecimal wholesaleSubtotal = BigDecimal.ZERO;
        int spuTotalQty = 0;
        boolean anyHit = false;
        // CR-20260612-1520：是否有有效计价行（任一行进入 newTotal）；用于替代 anyHit 决定是否覆写预览 total
        boolean anyPriced = false;

        // CR-20260604-1430：mode=1 混批先预聚合 Q_total（D2=all_sku：所有购买 SKU 数量之和，含零售 SKU），
        // 再用 Q_total 作为各行阶梯匹配数量；mode=2 不预聚合（tierMatchQty=null → resolve 回退单行 qty）
        boolean mixMode = config.getMultiSpecPriceMode() != null
                && config.getMultiSpecPriceMode() == MULTI_SPEC_MODE_MIX
                && config.getMoqDimension() != null && config.getMoqDimension() == MOQ_DIM_SPU;
        Integer mixQtyTotal = null;
        if (mixMode) {
            int total = 0;
            for (ProductAmountDTO.SkuItem item : items) {
                if (item == null || item.getNum() == null || item.getNum() <= 0) {
                    continue;
                }
                total += item.getNum();
            }
            mixQtyTotal = total > 0 ? total : null;
        }

        // 第一遍：命中判定 + SPU 加总
        WholesalePriceResolver.ResolveResult[] hits = new WholesalePriceResolver.ResolveResult[items.size()];
        for (int i = 0; i < items.size(); i++) {
            ProductAmountDTO.SkuItem item = items.get(i);
            if (item == null || item.getNum() == null || item.getNum() <= 0 || item.getSkuId() == null) {
                continue;
            }
            BigDecimal retailPrice = retailPriceBySkuId.get(item.getSkuId());
            if (retailPrice == null) {
                continue;
            }
            // CR-20260604-1015 修复：批发折扣基数用原始 SKU 价（缺失则回退会员价），避免折上折
            BigDecimal wholesaleBase = wholesaleBasePriceBySkuId.getOrDefault(item.getSkuId(), retailPrice);
            WholesalePriceResolver.ResolveResult hit = WholesalePriceResolver.resolve(
                    WholesalePriceResolver.ResolveInput.builder()
                            .productId(productId)
                            .skuId(item.getSkuId())
                            .gradeId(gradeId)
                            .quantity(item.getNum())
                            .tierMatchQty(mixQtyTotal)
                            .retailPrice(wholesaleBase)
                            .candidates(candidates)
                            .build());
            hits[i] = hit;
            if (hit != null) {
                anyHit = true;
                spuTotalQty += item.getNum();
            }
        }

        // 第二遍：合计 total + 收集软提示
        LinkedHashSet<String> hintSet = new LinkedHashSet<>();
        boolean spuDimension = config.getMoqDimension() != null && config.getMoqDimension() == MOQ_DIM_SPU;
        for (int i = 0; i < items.size(); i++) {
            ProductAmountDTO.SkuItem item = items.get(i);
            if (item == null || item.getNum() == null || item.getNum() <= 0 || item.getSkuId() == null) {
                continue;
            }
            BigDecimal retailPrice = retailPriceBySkuId.get(item.getSkuId());
            if (retailPrice == null) {
                continue;
            }
            int qty = item.getNum();
            WholesalePriceResolver.ResolveResult hit = hits[i];
            // 达到阶梯起量即享该行批发价，SPU / SKU 两种起批维度一致：起批量只写 hint，
            // 硬拦截由 cart/add 与 order/submit 负责，与购物车 applyLine 同口径。
            BigDecimal unit;
            if (hit != null) {
                unit = hit.getFinalPrice();
                BigDecimal lineSubtotal = unit.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
                wholesaleSubtotal = wholesaleSubtotal.add(lineSubtotal);
                newTotal = newTotal.add(lineSubtotal);
            } else {
                // 未命中阶梯的批发作用域行按原始零售价回退，与购物车口径一致
                unit = wholesaleBasePriceBySkuId.getOrDefault(item.getSkuId(), retailPrice);
                newTotal = newTotal.add(unit.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP));
            }
            anyPriced = true;
            // 软提示（仅命中行参与；未命中行属于零售，无批发约束）
            if (hit != null) {
                ProductWholesaleSkuConfig skuConf = skuConfigMap.get(productId + ":" + item.getSkuId());
                // 倍数（永远按 SKU；与起批维度正交）
                String mHint = WholesaleBuyRuleValidator.checkMultiple(qty, skuConf);
                if (mHint != null) {
                    hintSet.add(mHint);
                }
                // 起批量：SKU 维度逐行判；SPU 维度等下统一判（避免重复加入）
                if (!spuDimension) {
                    String skuMoqHint = WholesaleBuyRuleValidator.checkMoqSku(qty, config);
                    if (skuMoqHint != null) {
                        hintSet.add(skuMoqHint);
                    }
                }
            }
        }
        // SPU 维度起批量按 product 加总判（design §4.3）
        if (spuDimension && anyHit) {
            String spuMoqHint = WholesaleBuyRuleValidator.checkMoqSpu(spuTotalQty, config);
            if (spuMoqHint != null) {
                hintSet.add(spuMoqHint);
            }
        }

        // ---------------- 写 VO ----------------
        // CR-20260612-1520：批发买家 + 批发商品（已过 config!=null 守卫）= 批发作用域，无论是否命中阶梯价，
        // 预览 total 都按批发作用域口径覆写（命中行=阶梯价，未命中行=原始零售价，均不含会员折扣），与购物车/下单一致；
        // wholesaleSubtotal 仅累计命中阶梯价部分。无任何有效计价行时不改 vo（保持 Controller 零售口径）。
        if (!anyPriced) {
            return;
        }
        vo.setTotal(newTotal);
        vo.setWholesale(Boolean.TRUE);
        vo.setWholesaleSubtotal(wholesaleSubtotal);
        if (!hintSet.isEmpty()) {
            vo.setHint(String.join("；", hintSet));
        }

        if (log.isDebugEnabled()) {
            log.debug("[wholesale-amount] enhance: userId={}, productId={}, gradeId={}, hits={}, hint={}",
                    userId, productId, gradeId, countNonNull(hits), vo.getHint());
        }
    }

    private static int countNonNull(WholesalePriceResolver.ResolveResult[] arr) {
        int n = 0;
        for (WholesalePriceResolver.ResolveResult r : arr) {
            if (r != null) {
                n++;
            }
        }
        return n;
    }
}
