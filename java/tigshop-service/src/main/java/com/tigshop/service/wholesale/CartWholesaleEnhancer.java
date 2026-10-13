// **---------------------------------------------------------------------+
// ** 文件 -- CartWholesaleEnhancer
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+

package com.tigshop.service.wholesale;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.cart.Cart;
import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.bean.model.wholesale.ProductWholesalePrice;
import com.tigshop.bean.model.wholesale.ProductWholesaleSkuConfig;
import com.tigshop.bean.model.wholesale.Wholesaler;
import com.tigshop.bean.vo.cart.CartByStoreVO;
import com.tigshop.bean.vo.cart.CartListByStoreVO;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.mapper.cart.CartMapper;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.wholesale.resolver.WholesalePriceResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 购物车批发增强器（B2 批量壳，design §5）。
 *
 * <p>仅在 {@code isWholesaleBuyer=true} 时生效；非批发商场景 {@link #prefetch} 返回 EMPTY，
 * {@link #applyLine} 短路 → 零售零影响（CD-016：列表算价用当前等级 re-resolve，不读 cart 快照）。
 *
 * <p>列表场景执行链：
 * <ol>
 *   <li>循环外 {@link #prefetch}（≤3 DB：price IN + config IN + skuConfig IN；
 *       身份/等级走 Redis 缓存）— CD-017 口径下批发壳新增 DB ≤ 9。</li>
 *   <li>循环内 {@link #applyLine}（0 DB，纯内存调用 {@link WholesalePriceResolver}）。</li>
 * </ol>
 *
 * <p>加车场景 {@link #markAndValidateOnAdd}：在 saveBatch 前对入参 {@code addCarts} 逐行
 * 命中 → 写 {@code is_wholesale_line=1} + {@code wholesale_grade_id} 快照；起批量/倍数不达标
 * 抛 {@link HttpStatus#WHOLESALE_MOQ_NOT_MET 5320} / {@link HttpStatus#WHOLESALE_MULTIPLE_NOT_MET 5321}
 * 阻断加车（design §5.5 / §4.3 / §4.4）。
 *
 * <p>设计基准：
 * <ul>
 *   <li>CD-016：cart 不冻结，每次列表使用当前等级 re-resolve；{@code cart.wholesale_grade_id}
 *       仅作审计快照。</li>
 *   <li>CD-017：批发壳新增 DB ≤ 9 计数（零售既有 N+1 不计）。</li>
 *   <li>CD-018：一期不在本组件做缓存（列表 ≤3 DB 已达标，缓存留二期）。</li>
 *   <li>CD-015：折扣十分制（{@link WholesalePriceResolver} 内部处理）。</li>
 * </ul>
 *
 * <p><b>注意</b>：T-305b 在 {@link CartVO} 加 {@code isWholesaleLine/wholesalePriceMode/wholesaleHint}
 * 字段前，本组件 {@link #applyLine} 仅覆盖现有 {@code price/memberPrice/subtotal} 字段；
 * 行级提示文案先以 {@link #applyLine 软提示日志} 形式记录，待 T-305b 字段合并后再补回 setter
 * 调用（避免本任务编译失败 / 越界改动 CartVO）。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartWholesaleEnhancer {

    private final WholesalerService wholesalerService;
    private final WholesalePriceService priceService;
    private final WholesaleProductConfigService configService;
    private final WholesaleSkuConfigService skuConfigService;
    private final TigshopProperties tigshopProperties;
    /** CR-20260604-1430：仅混批加车（mode=1）查同商品其它 SKU 已有数量算 Q_total；数据层依赖不构成循环。 */
    private final CartMapper cartMapper;
    /** CR-20260612-1622：倍数向上取整后校验库存（ProductServiceImpl 不依赖购物车服务，无循环依赖）。 */
    private final ProductService productService;

    /** 当前是否 O2O 形态（门店渠道）。 */
    private boolean isO2oChannel() {
        return tigshopProperties != null && tigshopProperties.getIsO2o() != null
                && tigshopProperties.getIsO2o() == 1;
    }

    /** 本批加车行所属门店（同批同商品同店；取首个非空，取不到返回 null 表示无上下文）。 */
    private static Integer resolveAddShopId(List<Cart> addCarts) {
        if (addCarts == null) {
            return null;
        }
        for (Cart c : addCarts) {
            if (c != null && c.getShopId() != null) {
                return c.getShopId();
            }
        }
        return null;
    }

    // ============================================================
    // Public API
    // ============================================================

    /**
     * 列表预取上下文（不可变快照，循环内只读）。
     *
     * <p>{@link #empty()} 工厂返回 {@code active=false} 的空上下文，
     * 适用于：非批发商 / 空购物车 / 批发商记录缺失。
     */
    public static class CartWholesaleContext {

        /** 是否为批发商有效场景；false 时 {@link #applyLine} 直接短路。 */
        boolean active;

        /** 当前批发等级（CD-016：用当前等级，非 cart 快照）。 */
        Integer gradeId;

        /** key=productId → 候选 price 行（已按 grade ∈ {0, gradeId} 过滤）。 */
        Map<Integer, List<ProductWholesalePrice>> priceMap;

        /** key=productId → 商品级批发配置（仅含 status=1 的 active 行）。 */
        Map<Integer, ProductWholesaleConfig> configMap;

        /** key="{productId}:{skuId}" → SKU 级配置（起批量 + 倍数）。 */
        Map<String, ProductWholesaleSkuConfig> skuConfigMap;

        /**
         * key=productId → Q_total（CR-20260604-1430 混批）。
         *
         * <p>仅对 {@code multi_spec_price_mode=1}（商品级混批）的商品预聚合：
         * 该 product 下购物车中所有购买 SKU 的数量之和（D2=all_sku，含未配批发价的零售 SKU）。
         * mode=2 商品不入此表（{@link #applyLine} 取不到 → 回退单行 qty）。
         */
        Map<Integer, Integer> qtyTotalMap;

        public boolean isActive() {
            return active;
        }

        public Integer getGradeId() {
            return gradeId;
        }

        public Map<Integer, List<ProductWholesalePrice>> getPriceMap() {
            return priceMap;
        }

        public Map<Integer, ProductWholesaleConfig> getConfigMap() {
            return configMap;
        }

        public Map<String, ProductWholesaleSkuConfig> getSkuConfigMap() {
            return skuConfigMap;
        }

        public Map<Integer, Integer> getQtyTotalMap() {
            return qtyTotalMap;
        }

        static CartWholesaleContext empty() {
            CartWholesaleContext c = new CartWholesaleContext();
            c.active = false;
            c.priceMap = Collections.emptyMap();
            c.configMap = Collections.emptyMap();
            c.skuConfigMap = Collections.emptyMap();
            c.qtyTotalMap = Collections.emptyMap();
            return c;
        }
    }

    /** multi_spec_price_mode=1：商品级混批（按全规格总数 Q_total 查档）。 */
    private static final int MULTI_SPEC_MODE_MIX = 1;

    /**
     * 列表循环外预取（CD-017：≤3 DB；非批发商 / 空购物车 / 等级缺失短路返回 EMPTY）。
     *
     * <p>调用顺序：
     * <ol>
     *   <li>{@link WholesalerService#isWholesaleBuyer}（Redis 缓存命中 → 0 DB）</li>
     *   <li>{@link WholesalerService#getByUserId}（取 grade_id；批发商记录不存在 → EMPTY 防御）</li>
     *   <li>{@link WholesalePriceService#mapByProductIdsAndGrade}（1 DB：IN + grade ∈ {0, gradeId}）</li>
     *   <li>{@link WholesaleProductConfigService#mapByProductIds}（1 DB）</li>
     *   <li>{@link WholesaleSkuConfigService#mapByProductSkuKeys}（1 DB）</li>
     * </ol>
     *
     * @param cartList 当前用户购物车行（可空 → 空集时直接 EMPTY）
     * @param userId   当前登录用户 ID
     * @return 预取上下文；非批发商场景返回 active=false 的 EMPTY
     */
    public CartWholesaleContext prefetch(List<Cart> cartList, Integer userId) {
        if (cartList == null || cartList.isEmpty()) {
            return CartWholesaleContext.empty();
        }
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            return CartWholesaleContext.empty();
        }
        Wholesaler w = wholesalerService.getByUserId(userId);
        if (w == null || w.getGradeId() == null) {
            // 防御：isWholesaleBuyer 缓存与 wholesaler 表瞬时不一致 → 降级零售
            log.warn("[wholesale-cart] prefetch: getByUserId returned null/invalid grade for userId={}, fallback to retail",
                    userId);
            return CartWholesaleContext.empty();
        }
        List<Integer> productIds = cartList.stream()
                .map(Cart::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return CartWholesaleContext.empty();
        }

        CartWholesaleContext ctx = new CartWholesaleContext();
        ctx.active = true;
        ctx.gradeId = w.getGradeId();
        ctx.priceMap = priceService.mapByProductIdsAndGrade(productIds, ctx.gradeId);
        ctx.configMap = configService.mapByProductIds(productIds);
        ctx.skuConfigMap = skuConfigService.mapByProductSkuKeys(productIds);
        if (ctx.priceMap == null) {
            ctx.priceMap = Collections.emptyMap();
        }
        if (ctx.configMap == null) {
            ctx.configMap = Collections.emptyMap();
        }
        if (ctx.skuConfigMap == null) {
            ctx.skuConfigMap = Collections.emptyMap();
        }
        // CR-20260604-1430：mode=1 商品预聚合 Q_total（D2=all_sku：该 product 下购物车所有购买 SKU 数量之和）
        ctx.qtyTotalMap = buildQtyTotalMap(cartList, ctx.configMap);
        return ctx;
    }

    /**
     * 预聚合混批 Q_total（CR-20260604-1430）：仅对 {@code multi_spec_price_mode=1} 商品，
     * 加总该 product 下购物车所有行的购买数量（D2=all_sku，不区分该 SKU 是否配批发价）。
     *
     * <p>mode=2 商品不入表；{@link #applyLine} 取不到则回退单行 qty（行为不变）。
     */
    private static Map<Integer, Integer> buildQtyTotalMap(
            List<Cart> cartList, Map<Integer, ProductWholesaleConfig> configMap) {
        Map<Integer, Integer> totalMap = new HashMap<>();
        if (cartList == null || configMap == null || configMap.isEmpty()) {
            return totalMap;
        }
        for (Cart c : cartList) {
            if (c == null || c.getProductId() == null) {
                continue;
            }
            // 混批 Q_total 仅统计勾选行（对齐设计：「该商品在购物车里所有勾选规格的总件数」）；
            // 未勾选行不参与凑单，避免把数量推到更高阶梯档导致误命中。列表页查询含未勾选行，
            // 结算页查询本就只取勾选行，此过滤对结算路径为 no-op。
            Integer checked = c.getIsChecked();
            if (checked == null || checked != 1) {
                continue;
            }
            ProductWholesaleConfig config = configMap.get(c.getProductId());
            if (config == null || config.getMultiSpecPriceMode() == null
                    || config.getMultiSpecPriceMode() != MULTI_SPEC_MODE_MIX
                    || config.getMoqDimension() == null || config.getMoqDimension() != 1) {
                continue;
            }
            int qty = c.getQuantity() == null ? 0 : c.getQuantity();
            if (qty <= 0) {
                continue;
            }
            totalMap.merge(c.getProductId(), qty, Integer::sum);
        }
        return totalMap;
    }

    /**
     * 列表逐行覆盖（0 DB，纯内存）：命中批发价 → 覆盖 cartVO.price/memberPrice/subtotal；
     * 未命中（无配置 / 失效降级 §5.4）→ 不改 cartVO，保持零售价。
     *
     * <p>本任务范围（T-301）下，CartVO 尚未扩 {@code isWholesaleLine/wholesalePriceMode/wholesaleHint}
     * 字段（T-305b 才扩）；命中时仅覆盖现有价格字段，行级提示通过日志 + TODO 留白，
     * 待 T-305b 合并后由 build 阶段二次补回 setter（避免越界改动 CartVO）。
     *
     * @param cartVO 列表 VO（命中时被原地修改）
     * @param cart   对应 {@link Cart} 模型（保留参数以便 T-305b 后接入 cart 快照字段对照）
     * @param ctx    {@link #prefetch} 产物
     */
    public void applyLine(CartVO cartVO, Cart cart, CartWholesaleContext ctx) {
        if (cartVO == null || ctx == null || !ctx.active) {
            return;
        }
        // CR-20260610-162600 / CD-039：店铺商品批发已放开（原 CD-031 平台短路移除）。
        // 平台 + 店铺 + O2O 门店行统一按 productId 解析批发配置；未配置商品在下方 config==null 短路。
        Integer productId = cartVO.getProductId();
        if (productId == null) {
            return;
        }
        ProductWholesaleConfig config = ctx.configMap.get(productId);
        if (config == null) {
            // 未配置批发能力 → 保持零售
            return;
        }
        // O2O 渠道：仅当商品归属与本行所属门店一致才享批发（平台分配到门店的商品不享）
        if (!WholesaleBuyRuleValidator.isClientEligible(config, isO2oChannel(), cartVO.getShopId())) {
            return;
        }
        Integer quantity = cartVO.getQuantity();
        // CR-20260613-1720（续 CR-20260604-1015）：批发折扣基数统一用「实时零售价」startProductPrice
        // （有SKU=skuPrice / 无SKU=product.productPrice），而非 cart.originalPrice。
        // 原因：cart.originalPrice 仅在「有SKU」加车时被覆盖为 skuPrice，无SKU 商品停在会员折后价
        // （Cart.builder().originalPrice(productAvailability.getPrice())），导致无SKU批发商品折上折
        // （会员折扣 × 批发折扣）。startProductPrice 两种情况都等于零售价、且实时取（顺带纠正存量旧快照）。
        // 回退链：startProductPrice → cart.originalPrice → cartVO.getPrice()。
        BigDecimal retailPrice = cartVO.getStartProductPrice() != null
                ? cartVO.getStartProductPrice()
                : (cart != null && cart.getOriginalPrice() != null ? cart.getOriginalPrice() : cartVO.getPrice());
        if (quantity == null || quantity <= 0 || retailPrice == null) {
            return;
        }
        // CR-20260612-1520：批发买家 + 该商品有批发配置 → 整行进入「批发作用域」，无论是否命中阶梯价，
        // 都退出 C 端营销（会员价/券/满减/促销/积分），由营销闸门按 wholesaleScope 统一剔除。
        cartVO.setWholesaleScope(Boolean.TRUE);
        // CR-20260613-1711：批发作用域行的「划线价」统一对齐零售价（前端划线价读 cartVO.productPrice）。
        // CR-1520 已把 price 剔回零售但漏了 productPrice 仍停在会员折后价 → 划线价(productPrice) < 售价(price)。
        // 这里把 productPrice + originPrice 一并对齐到上面的零售基数 retailPrice（=实时零售价）。
        // 批发作用域行已在 CartServiceImpl 营销闸门(wholesaleScope)前过滤，改 productPrice 不影响券/促销计算。
        cartVO.setOriginPrice(retailPrice);
        cartVO.setProductPrice(retailPrice);
        // CR-20260612-1642：批发作用域行额外返回起批数量 / 维度 / 倍数（纯展示元数据，0 DB 复用 prefetch 上下文）。
        cartVO.setWholesaleMoq(config.getMoq());
        cartVO.setWholesaleMoqDimension(config.getMoqDimension());
        ProductWholesaleSkuConfig metaSkuConfig =
                WholesaleBuyRuleValidator.resolveSkuConfig(ctx.skuConfigMap, productId, cartVO.getSkuId());
        cartVO.setWholesaleMultiple(metaSkuConfig == null ? null : metaSkuConfig.getMultiple());
        // CR-20260604-1430：mode=1 混批用 Q_total 查档（取不到 → null → resolve 回退单行 qty）
        Integer tierMatchQty = ctx.qtyTotalMap == null ? null : ctx.qtyTotalMap.get(productId);
        WholesalePriceResolver.ResolveResult hit = WholesalePriceResolver.resolve(
                WholesalePriceResolver.ResolveInput.builder()
                        .productId(productId)
                        .skuId(cartVO.getSkuId())
                        .gradeId(ctx.gradeId)
                        .quantity(quantity)
                        .tierMatchQty(tierMatchQty)
                        .retailPrice(retailPrice)
                        .candidates(ctx.priceMap.getOrDefault(productId, Collections.emptyList()))
                        .build());
        if (hit == null) {
            // CR-20260612-1520：未命中任一阶梯 / §5.4 中途失效 → 按「原始零售价」结算（剔除会员等级折扣），
            // 营销由 wholesaleScope 闸门统一排除。原始零售价取加车存的 SKU 原始价（retailPrice），
            // 覆盖会员价装配阶段写入的 price/memberPrice/subtotal。
            cartVO.setPrice(retailPrice);
            cartVO.setMemberPrice(retailPrice);
            cartVO.setSubtotal(retailPrice
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP));
            // 若上次加车曾命中（cart.is_wholesale_line=1），写降级提示（5322 文案语义），便于 UI 展示
            if (cart != null && cart.getIsWholesaleLine() != null && cart.getIsWholesaleLine() == 1) {
                cartVO.setWholesaleHint("批发价已失效，按零售价结算");
                log.debug("[wholesale-cart] applyLine: price expired (§5.4), userId={}, productId={}, skuId={}",
                        cartVO.getUserId(), productId, cartVO.getSkuId());
            }
            return;
        }

        // 命中 → 覆盖价格字段（保留 originPrice 为零售基数，便于 UI 展示零售价划线）
        BigDecimal finalPrice = hit.getFinalPrice();
        cartVO.setPrice(finalPrice);
        cartVO.setMemberPrice(finalPrice);
        cartVO.setSubtotal(finalPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP));

        // T-305b：行级 wholesale 字段（@vort-if 包裹于 CartVO，B2C profile 自动剔除）
        cartVO.setIsWholesaleLine(Boolean.TRUE);
        cartVO.setWholesalePriceMode(hit.getPriceMode());
        // 起批量 / 倍数软提示（列表不阻断 §4.4；hint 为 null 表示无提示）
        String hint = buildLineHint(quantity, productId, cartVO.getSkuId(), config, ctx);
        cartVO.setWholesaleHint(hint);
        if (hint != null && log.isDebugEnabled()) {
            log.debug("[wholesale-cart] applyLine: hint userId={}, productId={}, skuId={}, hint={}",
                    cartVO.getUserId(), productId, cartVO.getSkuId(), hint);
        }
    }

    /**
     * 列表加载时倍数向上取整对齐 + 回写（CR-20260612-1622 决策3-b）。
     *
     * <p>针对历史遗留不对齐的批发命中行（典型场景：用户加车后管理员调大倍数），在购物车列表
     * 加载时自动把数量向上取整对齐到当前倍数，并：
     * <ul>
     *   <li>回写 DB（{@code cart.quantity}）使后续下单二次校验（{@code validateWholesaleSubmitRules}）通过；</li>
     *   <li>同步 {@code cartVO.quantity / subtotal}；</li>
     *   <li>写 {@code wholesaleHint} 提示「已自动调整为 N 件以满足 M 倍购买」。</li>
     * </ul>
     *
     * <p>口径约束：
     * <ul>
     *   <li><b>仅命中行</b>（{@code cartVO.isWholesaleLine=true}）参与对齐，与倍数硬校验作用域一致；
     *       未命中行不强制倍数，不对齐。</li>
     *   <li>已对齐行 no-op（幂等：二次加载不再写库）。</li>
     *   <li>仅对齐倍数，不连带起批量（决策1）。</li>
     *   <li>对齐后超库存（决策2）：不改数、不回写，仅写「库存不足」提示。</li>
     * </ul>
     *
     * <p>本方法应在 {@link #applyLine} 之后调用（依赖其写入的 {@code isWholesaleLine} 与批发价）。
     *
     * @param cartVO 列表 VO（{@link #applyLine} 已处理）
     * @param cart   对应 {@link Cart} 模型（回写目标，需为已加载的完整实体）
     * @param ctx    {@link #prefetch} 产物（含 skuConfigMap）
     * @param stock  当前可用库存（调用方已取，传入避免重复 DB；null 视为不限制）
     */
    public void alignLineQuantityOnList(CartVO cartVO, Cart cart, CartWholesaleContext ctx, Integer stock) {
        if (cartVO == null || cart == null || ctx == null || !ctx.active) {
            return;
        }
        // 仅命中行对齐（与倍数硬校验口径一致）；未命中行不强制倍数
        if (!Boolean.TRUE.equals(cartVO.getIsWholesaleLine())) {
            return;
        }
        Integer productId = cartVO.getProductId();
        Integer quantity = cartVO.getQuantity();
        if (productId == null || quantity == null || quantity <= 0) {
            return;
        }
        ProductWholesaleSkuConfig skuConf =
                WholesaleBuyRuleValidator.resolveSkuConfig(ctx.skuConfigMap, productId, cartVO.getSkuId());
        int aligned = WholesaleBuyRuleValidator.alignUpToMultiple(quantity, skuConf);
        if (aligned == quantity) {
            // 已对齐 → no-op（幂等）
            return;
        }
        Integer m = skuConf == null ? null : skuConf.getMultiple();
        if (stock != null && aligned > stock) {
            // 对齐后超库存：不改数，仅提示（决策2）
            cartVO.setWholesaleHint("库存不足，无法满足" + (m == null ? "" : m) + "倍购买");
            return;
        }
        // 对齐 + 回写 DB + 同步 VO
        cart.setQuantity(aligned);
        cartMapper.updateById(cart);
        cartVO.setQuantity(aligned);
        BigDecimal price = cartVO.getPrice();
        if (price != null) {
            cartVO.setSubtotal(price.multiply(BigDecimal.valueOf(aligned)).setScale(2, RoundingMode.HALF_UP));
        }
        cartVO.setWholesaleHint("已自动调整为" + aligned + "件以满足" + (m == null ? "" : m) + "倍购买");
        log.info("[wholesale-cart] alignLineQuantityOnList: userId={}, productId={}, skuId={}, {} -> {} (multiple={})",
                cartVO.getUserId(), productId, cartVO.getSkuId(), quantity, aligned, m);
    }

    /**
     * 店铺组级装配（design §5 / §6 / §4.3）：
     * 在 {@link CartByStoreVO}（单店铺组）上写入 {@code wholesaleConfigured}
     * 与 {@code wholesaleGroupHint}。
     *
     * <p>本方法 0 DB，纯内存读 {@link CartWholesaleContext} 已 prefetch 数据。
     *
     * <p>规则（CD-020 加车硬校验 / 列表软提示）：
     * <ul>
     *   <li>{@code wholesaleConfigured}：遍历 group.carts，只要任一行的 productId
     *       在 {@link CartWholesaleContext#configMap} 中存在 active 配置 → {@code true}。</li>
     *   <li>{@code wholesaleGroupHint}：仅当 {@code moq_dimension=1}（SPU 维度）时计算；
     *       按 productId 加总 group 内 quantity，若总和 &lt; {@code moq_spu} → 走
     *       {@link WholesaleBuyRuleValidator#checkMoqSpu} 输出 design §4.3 文案
     *       （"该商品还差 N 件凑齐起批量 M 件"）。SKU 维度（{@code moq_dimension=2}）
     *       由 {@link #applyLine} 逐行 hint 负责，本方法返回 null。</li>
     *   <li>多 product 混合（多个 SPU 维度都未达标）：取**首个未达标**（按 group.carts
     *       迭代顺序首次遇到的 productId）作为组级 hint。</li>
     * </ul>
     *
     * <p>早退出场景：{@code ctx==null || !ctx.active || group==null || group.carts==null}
     * → 不写字段。
     *
     * @param group 单店铺组（命中时被原地修改）
     * @param ctx   {@link #prefetch} 产物（含 configMap）
     */
    public void applyGroup(CartByStoreVO group, CartWholesaleContext ctx) {
        if (group == null || ctx == null || !ctx.active) {
            return;
        }
        List<CartVO> carts = group.getCarts();
        if (carts == null || carts.isEmpty()) {
            return;
        }

        // 1) wholesaleConfigured：扫描组内任一行 productId 是否命中 active configMap
        boolean configured = false;
        // 2) 同时按 productId 聚合 SPU 维度的数量，记录首个出现的 productId 顺序
        Map<Integer, Integer> spuTotalMap = new HashMap<>();
        List<Integer> productOrder = new java.util.ArrayList<>();
        for (CartVO line : carts) {
            if (line == null) {
                continue;
            }
            Integer productId = line.getProductId();
            if (productId == null) {
                continue;
            }
            ProductWholesaleConfig config = ctx.configMap.get(productId);
            if (config == null) {
                continue;
            }
            configured = true;

            Integer dim = config.getMoqDimension();
            if (dim == null || dim != 1) {
                // 非 SPU 维度 → 不参与组级 hint 聚合
                continue;
            }
            int qty = line.getQuantity() == null ? 0 : line.getQuantity();
            if (qty <= 0) {
                continue;
            }
            if (!spuTotalMap.containsKey(productId)) {
                productOrder.add(productId);
            }
            spuTotalMap.merge(productId, qty, Integer::sum);
        }

        // 3) 取首个 SPU 未达标 product 的 hint（design §4.3 文案；多 product 混合时按首次出现顺序）
        String hint = null;
        for (Integer pid : productOrder) {
            ProductWholesaleConfig config = ctx.configMap.get(pid);
            if (config == null) {
                continue;
            }
            int total = spuTotalMap.getOrDefault(pid, 0);
            String h = WholesaleBuyRuleValidator.checkMoqSpu(total, config);
            if (h != null) {
                hint = h;
                break;
            }
        }

        group.setWholesaleConfigured(configured);
        group.setWholesaleGroupHint(hint);
    }

    /**
     * 顶层装配（design §5）：在 {@link CartListByStoreVO} 上写入
     * {@code isWholesaleBuyer = ctx.isActive()}。
     *
     * <p>本方法 0 DB，仅读 ctx 标志位。**不在内部循环 cartList 调
     * {@link #applyGroup}**（保持单一职责；M1 装配点显式分别调用）。
     *
     * <p>早退出：{@code top==null || ctx==null} → 不写字段（不抛 NPE）。
     *
     * @param top 顶层 VO（命中时被原地修改）
     * @param ctx {@link #prefetch} 产物
     */
    public void applyTopLevel(CartListByStoreVO top, CartWholesaleContext ctx) {
        if (top == null || ctx == null) {
            return;
        }
        top.setIsWholesaleBuyer(ctx.isActive());
    }

    /**
     * 加车标记 + 起批量/倍数硬校验（design §5.5）。
     *
     * <p>方法签名按 step doc §T-303 调整为 {@code (List<Cart> addCarts, Integer userId, Integer productId)}：
     * 不耦合 Product / ProductDetailDTO。CartServiceImpl 只需传 productId。
     *
     * <p>规则：
     * <ul>
     *   <li>非批发商 / 商品未配批发 / 未命中批发价 → 不改 cart 字段（零售直通）。</li>
     *   <li>命中 → 写 {@code is_wholesale_line=1} + {@code wholesale_grade_id=当前等级} 快照。</li>
     *   <li>起批量不达标 → 抛 {@link HttpStatus#WHOLESALE_MOQ_NOT_MET 5320}（含文案）。</li>
     *   <li>倍数不达标 → <b>向上取整对齐到新倍数</b>（CR-20260612-1622 决策3-a；不再抛 5321）；
     *       对齐后超库存则抛库存不足（决策2）。</li>
     *   <li>处理顺序：先倍数对齐 → 再起批量（与 design §4.3 / §4.4 一致）。</li>
     * </ul>
     *
     * @param addCarts  本批待入库 Cart 行（CartServiceImpl.addProductToCart 内 saveBatch 前调用）
     * @param userId    当前登录用户
     * @param productId 该批 Cart 行所属商品 ID（同批同商品）
     */
    public void markAndValidateOnAdd(List<Cart> addCarts, Integer userId, Integer productId) {
        if (addCarts == null || addCarts.isEmpty() || productId == null) {
            return;
        }
        // CR-20260610-162600 / CD-039：店铺商品批发已放开（原 CD-031 平台短路移除）。
        // O2O 下门店购买总部商品时 cart.shopId=门店 ID（>0），原短路会让 O2O 批发整体失效，本次一并解决。
        if (!wholesalerService.isWholesaleBuyer(userId)) {
            return;
        }
        Wholesaler w = wholesalerService.getByUserId(userId);
        if (w == null || w.getGradeId() == null) {
            return;
        }
        Integer gradeId = w.getGradeId();

        ProductWholesaleConfig config = configService.getActiveByProductId(productId);
        if (config == null) {
            // 商品未配置批发能力 → 纯零售加车
            return;
        }
        // O2O 渠道：商品归属与所加门店不一致（平台分配商品）→ 不参与批发，也不受起批量/倍数约束
        if (!WholesaleBuyRuleValidator.isClientEligible(config, isO2oChannel(), resolveAddShopId(addCarts))) {
            return;
        }

        List<ProductWholesalePrice> rows = priceService.listByProductId(productId);
        Map<String, ProductWholesaleSkuConfig> skuConfigMap =
                skuConfigService.mapByProductSkuKeys(Collections.singletonList(productId));

        // CR-20260604-1430：mode=1 混批先算 Q_total（本批次全 SKU 数量 + 同商品其它 SKU 已有数量），
        // 命中判定改用 Q_total 查档；mode=2 / null 不变（tierMatchQty=null → resolve 回退单行 qty）。
        boolean mixMode = config.getMultiSpecPriceMode() != null
                && config.getMultiSpecPriceMode() == MULTI_SPEC_MODE_MIX
                && config.getMoqDimension() != null && config.getMoqDimension() == 1;
        Integer mixQtyTotal = mixMode ? computeAddMixQtyTotal(addCarts, userId, productId) : null;

        // SPU 维度起批量需先加总（仅命中批发的行计入加总，未命中的零售行排除 §4.3）
        // 第一遍：识别命中行 + 累加 SPU 总数
        int spuTotalQty = 0;
        WholesalePriceResolver.ResolveResult[] hits = new WholesalePriceResolver.ResolveResult[addCarts.size()];
        for (int i = 0; i < addCarts.size(); i++) {
            Cart c = addCarts.get(i);
            if (c == null) {
                continue;
            }
            int qty = c.getQuantity() == null ? 0 : c.getQuantity();
            if (qty <= 0) {
                continue;
            }
            BigDecimal retail = c.getOriginalPrice();
            WholesalePriceResolver.ResolveResult hit = WholesalePriceResolver.resolve(
                    WholesalePriceResolver.ResolveInput.builder()
                            .productId(productId)
                            .skuId(c.getSkuId())
                            .gradeId(gradeId)
                            .quantity(qty)
                            .tierMatchQty(mixQtyTotal)
                            .retailPrice(retail)
                            .candidates(rows)
                            .build());
            hits[i] = hit;
            if (hit != null) {
                spuTotalQty += qty;
            }
        }

        // 第二遍：逐行倍数 / 起批量校验 + 写快照
        boolean spuDimension = config.getMoqDimension() != null && config.getMoqDimension() == 1;
        for (int i = 0; i < addCarts.size(); i++) {
            Cart c = addCarts.get(i);
            WholesalePriceResolver.ResolveResult hit = hits[i];
            if (c == null || hit == null) {
                // 未命中批发 → 该行保持零售，不写快照
                continue;
            }
            int qty = c.getQuantity() == null ? 0 : c.getQuantity();
            // 精确 SKU 优先，未命中回退 SPU 兜底 sku_id=0（单规格倍数）
            ProductWholesaleSkuConfig skuConf =
                    WholesaleBuyRuleValidator.resolveSkuConfig(skuConfigMap, productId, c.getSkuId());

            // CR-20260612-1622：倍数不达标由「硬抛 5321」改为「向上取整对齐到新倍数」（决策3-a）。
            // 仅对齐倍数，不连带起批量（决策1）；对齐后超库存则硬阻断、不静默改数（决策2）。
            int aligned = WholesaleBuyRuleValidator.alignUpToMultiple(qty, skuConf);
            if (aligned != qty) {
                int stock = productService.getProductStock(
                        c.getProductId(), Optional.ofNullable(c.getSkuId()).orElse(0), true, c.getShopId());
                if (aligned > stock) {
                    Integer m = skuConf == null ? null : skuConf.getMultiple();
                    throw new GlobalException("库存不足，无法满足" + (m == null ? "" : m) + "倍购买");
                }
                c.setQuantity(aligned);
                qty = aligned;
            }

            // 起批量按 moq_dimension 路由（CR-20260604-0937：SKU 维度统一读 config.moq）
            String moqErr = spuDimension
                    ? WholesaleBuyRuleValidator.checkMoqSpu(spuTotalQty, config)
                    : WholesaleBuyRuleValidator.checkMoqSku(qty, config);
            if (moqErr != null) {
                throw new GlobalException(moqErr, HttpStatus.WHOLESALE_MOQ_NOT_MET);
            }

            c.setIsWholesaleLine(1);
            c.setWholesaleGradeId(gradeId);
        }
    }

    // ============================================================
    // Internals
    // ============================================================

    /**
     * 计算混批加车的 Q_total（CR-20260604-1430，D4=merge_existing）。
     *
     * <p>= 本批次 {@code addCarts} 全部 SKU 数量之和 +
     * 同 user / product / type / shop 下购物车中<b>不在本批次</b>的其它 SKU 已有数量之和。
     *
     * <p>说明：本批次 SKU 的"已有数量"已在 {@code CartServiceImpl} 建行时合并进 {@code addCarts}
     * 并删除旧行，故按 {@code skuId NOT IN 本批次} 查询其它 SKU，避免重复计数。
     * 仅 mode=1 调用 → +1 DB 严格限定在混批加车（CD-017 预算友好）。
     */
    private int computeAddMixQtyTotal(List<Cart> addCarts, Integer userId, Integer productId) {
        Set<Integer> batchSkuIds = new HashSet<>();
        int batchTotal = 0;
        Integer type = null;
        Integer shopId = null;
        for (Cart c : addCarts) {
            if (c == null) {
                continue;
            }
            if (c.getSkuId() != null) {
                batchSkuIds.add(c.getSkuId());
            }
            int qty = c.getQuantity() == null ? 0 : c.getQuantity();
            if (qty > 0) {
                batchTotal += qty;
            }
            if (type == null && c.getType() != null) {
                type = c.getType();
            }
            if (shopId == null && c.getShopId() != null) {
                shopId = c.getShopId();
            }
        }

        if (userId == null) {
            return batchTotal;
        }
        LambdaQueryWrapper<Cart> qw = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId);
        if (type != null) {
            qw.eq(Cart::getType, type);
        }
        if (shopId != null) {
            qw.eq(Cart::getShopId, shopId);
        }
        if (!batchSkuIds.isEmpty()) {
            qw.notIn(Cart::getSkuId, new ArrayList<>(batchSkuIds));
        }
        List<Cart> others = cartMapper.selectList(qw);
        int otherTotal = 0;
        if (others != null) {
            for (Cart c : others) {
                if (c != null && c.getQuantity() != null && c.getQuantity() > 0) {
                    otherTotal += c.getQuantity();
                }
            }
        }
        return batchTotal + otherTotal;
    }

    /**
     * 列表行级软提示组装（倍数优先于 SKU 起批量；T-305b 后写入 cartVO.wholesaleHint）。
     *
     * <p>SPU 维度的"加总差额"提示属于组级（cartGroups[].wholesaleGroupHint，落 CartByStoreVO 的
     * T-305b 字段），本方法只负责行级文案。
     */
    private String buildLineHint(int quantity, Integer productId, Integer skuId,
                                 ProductWholesaleConfig config,
                                 CartWholesaleContext ctx) {
        if (config == null || ctx == null || ctx.skuConfigMap == null) {
            return null;
        }
        // 倍数仍按 SKU 配置（CR-20260604-0937 不动）；精确 SKU 优先，回退 SPU 兜底 sku_id=0
        ProductWholesaleSkuConfig skuConf =
                WholesaleBuyRuleValidator.resolveSkuConfig(ctx.skuConfigMap, productId, skuId);
        String mHint = WholesaleBuyRuleValidator.checkMultiple(quantity, skuConf);
        if (mHint != null) {
            return mHint;
        }
        // CR-20260604-0937：SKU 维度起批数也读 config.moq（不再读 skuConf.moqSku）
        boolean skuDimension = config.getMoqDimension() != null && config.getMoqDimension() == 2;
        if (skuDimension) {
            return WholesaleBuyRuleValidator.checkMoqSku(quantity, config);
        }
        return null;
    }
}
