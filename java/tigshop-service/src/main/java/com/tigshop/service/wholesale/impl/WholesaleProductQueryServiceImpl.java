package com.tigshop.service.wholesale.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.bean.model.wholesale.ProductWholesalePrice;
import com.tigshop.bean.model.wholesale.ProductWholesaleSkuConfig;
import com.tigshop.bean.query.wholesale.WholesaleProductPageQuery;
import com.tigshop.bean.query.wholesale.WholesaleSelectableProductPageQuery;
import com.tigshop.bean.vo.wholesale.WholesaleProductBuyRuleVO;
import com.tigshop.bean.vo.wholesale.WholesaleProductVO;
import com.tigshop.bean.vo.wholesale.WholesaleSkuRuleVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.mapper.wholesale.ProductWholesaleConfigMapper;
import com.tigshop.service.product.CategoryService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.wholesale.WholesalePriceService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.wholesale.WholesaleProductConfigService;
import com.tigshop.service.wholesale.WholesaleProductQueryService;
import com.tigshop.service.wholesale.WholesaleSkuConfigService;
import com.tigshop.service.wholesale.resolver.WholesalePriceResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 批发商品列表查询 Service 实现（读侧聚合）。
 *
 * <p>设计依据：{@code .ai-os/specs/wholesaler-tasks-w2-steps.md § TASK-WS-204}、
 * {@code wholesaler-api-contract.md §2.B1}。
 *
 * <p><b>聚合路线（先配置后回填）</b>：以 {@code product_wholesale_config} 为分页主表（数据量受限于
 * 已配置批发的商品集合），分页拿到当前页 productId 后，再批量回填 {@code product} 基础信息、
 * {@code product_wholesale_sku_config}（起批/倍数）与 {@code product_wholesale_price}（批发价范围）。
 * keyword 命中时先在 {@code product} 表按 productName/productSn 模糊解析候选 productId，再以
 * {@code IN} 约束收敛分页，保证分页 total 正确（不在内存里二次过滤分页结果）。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Service
@RequiredArgsConstructor
public class WholesaleProductQueryServiceImpl implements WholesaleProductQueryService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int MOQ_DIMENSION_SPU = 1;
    private static final int PRICE_MODE_DISCOUNT = 1;
    /** grade_id=0：通用价（列表范围预览优先取等级价，无等级价时才取通用价） */
    private static final int GRADE_UNIVERSAL = 0;
    private static final String EMPTY_DISPLAY = "—";
    /** 商品上架状态：1=上架（selectablePage 默认值） */
    private static final int PRODUCT_STATUS_ON_SHELF = 1;
    /** product_type=1：普通（实物）商品，仅此类型可加入批发 */
    private static final int PRODUCT_TYPE_NORMAL = 1;
    /** sale_mode=0：现货（非预售），仅此销售模式可加入批发 */
    private static final int SALE_MODE_IN_STOCK = 0;
    /** delivery_mode=1：单次交付，周期购（2）不支持加入批发 */
    private static final int DELIVERY_MODE_SINGLE = 1;

    private final ProductWholesaleConfigMapper configMapper;
    private final WholesaleProductConfigService configService;
    private final WholesaleSkuConfigService skuConfigService;
    private final WholesalePriceService priceService;
    private final ProductService productService;
    private final ProductSkuService productSkuService;
    private final CategoryService categoryService;
    /** 商城设置（货币符号），用于批发价范围预览文案 */
    private final ConfigService settingConfigService;

    @Override
    public IPage<WholesaleProductVO> page(WholesaleProductPageQuery query) {
        WholesaleProductPageQuery q = query != null ? query : new WholesaleProductPageQuery();

        Page<ProductWholesaleConfig> page = new Page<>(
                Optional.ofNullable(q.getPage()).filter(p -> p > 0).orElse(DEFAULT_PAGE),
                Math.min(Optional.ofNullable(q.getSize()).filter(s -> s > 0).orElse(DEFAULT_SIZE), MAX_SIZE));

        LambdaQueryWrapper<ProductWholesaleConfig> qw = new LambdaQueryWrapper<>();
        // is_delete 由 @TableLogic 自动过滤
        // 多租户隔离（CR-20260610-162600 / CD-039）：平台 admin 看平台商品配置（shop_id=0），
        // 商家看自己店铺的；操作者身份取请求头（单测/无上下文按 0 处理）
        qw.eq(ProductWholesaleConfig::getShopId, currentOperatorShopId());
        if (q.getMoqDimension() != null) {
            qw.eq(ProductWholesaleConfig::getMoqDimension, q.getMoqDimension());
        }

        // keyword：先按商品名/货号解析候选 productId，再以 IN 收敛分页（保证 total 正确）
        if (StringUtils.hasText(q.getKeyword())) {
            List<Integer> matchedIds = matchProductIdsByKeyword(q.getKeyword().trim());
            if (matchedIds.isEmpty()) {
                return emptyPage(page);
            }
            qw.in(ProductWholesaleConfig::getProductId, matchedIds);
        }
        qw.orderByDesc(ProductWholesaleConfig::getCreateTime);

        IPage<ProductWholesaleConfig> rawPage = configMapper.selectPage(page, qw);
        List<ProductWholesaleConfig> configs = rawPage.getRecords();
        if (configs.isEmpty()) {
            return rawPage.convert(c -> new WholesaleProductVO());
        }

        List<Integer> productIds = configs.stream()
                .map(ProductWholesaleConfig::getProductId)
                .collect(Collectors.toList());

        // 批量回填：product 基础信息
        Map<Integer, Product> productMap = productService.getProductByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p, (a, b) -> a));

        // 批量回填：SKU（零售价范围 + SKU 维度起批 min）
        Map<Integer, List<ProductSkuDTO>> skuByProduct = productSkuService
                .getProductSkusByProductIds(productIds).stream()
                .filter(s -> s.getProductId() != null)
                .collect(Collectors.groupingBy(ProductSkuDTO::getProductId));

        // 批量回填：SKU 级配置（起批/倍数）key=productId:skuId
        Map<String, ProductWholesaleSkuConfig> skuConfigMap =
                skuConfigService.mapByProductSkuKeys(productIds);

        return rawPage.convert(c -> assemble(c, productMap.get(c.getProductId()),
                skuByProduct.getOrDefault(c.getProductId(), Collections.emptyList()),
                skuConfigMap));
    }

    @Override
    public IPage<ProductListResDTO> selectablePage(WholesaleSelectableProductPageQuery query) {
        WholesaleSelectableProductPageQuery q = query != null ? query : new WholesaleSelectableProductPageQuery();

        Page<Product> page = new Page<>(
                Optional.ofNullable(q.getPage()).filter(p -> p > 0).orElse(DEFAULT_PAGE),
                Math.min(Optional.ofNullable(q.getSize()).filter(s -> s > 0).orElse(DEFAULT_SIZE), MAX_SIZE));

        LambdaQueryWrapper<Product> pw = new LambdaQueryWrapper<>();
        pw.eq(Product::getIsDelete, 0)
                // 归属商品（CR-20260610-162600 / CD-039 / CD-034 修订）：平台 admin 选平台商品，
                // 商家选自己店铺商品（原固定 shop_id=0 已废）
                .eq(Product::getShopId, currentOperatorShopId())
                // 默认仅上架；显式传 0 时查下架；为 null 视为默认上架
                .eq(Product::getProductStatus, Optional.ofNullable(q.getProductStatus())
                        .orElse(PRODUCT_STATUS_ON_SHELF));
        // 仅普通（实物，product_type=1，null 视为普通）且非预售（sale_mode 0/null）商品可加入批发
        // （2026-06-11 用户决策；与 WholesaleProductConfigServiceImpl.create 5315 硬校验同口径）
        pw.and(w -> w.isNull(Product::getProductType).or().eq(Product::getProductType, PRODUCT_TYPE_NORMAL));
        pw.and(w -> w.isNull(Product::getSaleMode).or().eq(Product::getSaleMode, SALE_MODE_IN_STOCK));
        pw.and(w -> w.isNull(Product::getDeliveryMode).or().eq(Product::getDeliveryMode, DELIVERY_MODE_SINGLE));

        // Bug-20260604-1031：原 eq 严格匹配，传父分类时拿不到子分类商品；
        // 与 ProductServiceImpl.productList 同款写法 —— 展开整棵子树后 IN（含自身）
        if (q.getCategoryId() != null && q.getCategoryId() > 0) {
            List<Integer> categoryIds = categoryService.getCategoryAllChildIds(q.getCategoryId());
            if (CollUtil.isNotEmpty(categoryIds)) {
                pw.in(Product::getCategoryId, categoryIds);
            }
        }
        if (q.getBrandId() != null && q.getBrandId() > 0) {
            pw.eq(Product::getBrandId, q.getBrandId());
        }
        if (StringUtils.hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            pw.and(w -> w.like(Product::getProductName, kw)
                    .or().like(Product::getProductSn, kw)
                    .or().like(Product::getProductTsn, kw));
        }

        // 剔除「已配置批发」的 product_id（含 is_delete=1 历史残留），口径与 existsAnyByProductId
        // 一致；Bug-20260603-1640 物理删后正常态 configuredIds 仅含 active，但为防御
        // 历史 / 迁移脏行仍按全表去重计算。
        List<Integer> configuredIds = configMapper.selectAllConfiguredProductIds();
        if (!configuredIds.isEmpty()) {
            pw.notIn(Product::getProductId, configuredIds);
        }

        pw.orderByDesc(Product::getProductId);
        IPage<Product> rawPage = productService.page(page, pw);
        return rawPage.convert(this::toSelectableResDTO);
    }

    /**
     * 装配「可选批发商品」VO：只填本端点声明的最小字段集（基础 + 图片 + 价格 + 状态 + 分类品牌）。
     * <p>使用 hutool {@link BeanUtil#copyProperties} 同名字段拷贝，与 {@link ProductListResDTO}
     * 既有 {@code Product} 构造器风格一致；下游通过 {@code @JsonTranslate} 自动处理 CDN 前缀。
     */
    /**
     * 当前操作者 shopId（CD-039）：平台 admin=0，商家=自己 shopId；
     * 无请求头上下文（单测/内部调用）按 0（平台）处理，与 {@code WholesaleProductConfigServiceImpl} 同口径。
     */
    private static int currentOperatorShopId() {
        Integer shopId = HeaderUtils.getShopId();
        return shopId == null ? 0 : shopId;
    }

    private ProductListResDTO toSelectableResDTO(Product p) {
        ProductListResDTO dto = new ProductListResDTO();
        BeanUtil.copyProperties(p, dto);
        return dto;
    }

    private WholesaleProductVO assemble(ProductWholesaleConfig config, Product product,
            List<ProductSkuDTO> skus, Map<String, ProductWholesaleSkuConfig> skuConfigMap) {
        WholesaleProductVO vo = new WholesaleProductVO();
        Integer productId = config.getProductId();
        vo.setProductId(productId);
        vo.setMoqDimension(config.getMoqDimension());
        vo.setMoqDimensionName(config.getMoqDimension() != null && config.getMoqDimension() == MOQ_DIMENSION_SPU
                ? "SPU" : "SKU");
        vo.setConfigStatus(config.getStatus());

        if (product != null) {
            vo.setProductName(product.getProductName());
            vo.setProductSn(product.getProductSn());
            // productNo 对应商品内部编号（product_tsn），与货号 productSn 并列展示
            vo.setProductNo(product.getProductTsn());
            vo.setPicUrl(product.getPicUrl());
            vo.setPicThumb(product.getPicThumb());
            vo.setStock(product.getProductStock());
            // shelf_status 复用既有商品状态字段 product.product_status（1=上架 0=下架）
            vo.setShelfStatus(product.getProductStatus());
        }

        vo.setRetailPriceRange(formatRetailRange(skus, product));

        // 该商品的 SKU 级配置（仅承载 multiple；CR-20260604-0937 简化）
        List<ProductWholesaleSkuConfig> skuConfigs = skuConfigsOf(productId, skuConfigMap);
        vo.setMultipleSet(skuConfigs.stream()
                .anyMatch(sc -> sc.getMultiple() != null && sc.getMultiple() > 1));
        // CR-20260604-0937：moqDisplay 直接来自 config.moq（不论 SPU/SKU 维度统一字段）
        vo.setMoqDisplay(config.getMoq());

        vo.setWholesalePriceDisplay(formatPriceRange(priceService.listByProductId(productId)));
        return vo;
    }

    private List<ProductWholesaleSkuConfig> skuConfigsOf(Integer productId,
            Map<String, ProductWholesaleSkuConfig> skuConfigMap) {
        String prefix = productId + ":";
        return skuConfigMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    /** 按商品名 / 货号模糊解析候选 productId（含 is_delete=0 过滤）。 */
    private List<Integer> matchProductIdsByKeyword(String keyword) {
        LambdaQueryWrapper<Product> pw = new LambdaQueryWrapper<>();
        pw.select(Product::getProductId)
                .eq(Product::getIsDelete, 0)
                .and(w -> w.like(Product::getProductName, keyword)
                        .or().like(Product::getProductSn, keyword));
        return productService.list(pw).stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());
    }

    /** 零售价范围：取 SKU 价格 min/max；无 SKU 时退回 product.product_price。 */
    private String formatRetailRange(List<ProductSkuDTO> skus, Product product) {
        List<BigDecimal> prices = new ArrayList<>();
        if (skus != null) {
            for (ProductSkuDTO s : skus) {
                if (StringUtils.hasText(s.getSkuPrice())) {
                    try {
                        prices.add(new BigDecimal(s.getSkuPrice()));
                    } catch (NumberFormatException ignore) {
                        // 跳过非法价格
                    }
                }
            }
        }
        if (prices.isEmpty() && product != null && product.getProductPrice() != null) {
            prices.add(product.getProductPrice());
        }
        if (prices.isEmpty()) {
            return EMPTY_DISPLAY;
        }
        BigDecimal min = Collections.min(prices);
        BigDecimal max = Collections.max(prices);
        return min.compareTo(max) == 0
                ? money(min)
                : money(min) + "-" + money(max);
    }

    /**
     * 批发价范围预览：<b>逐规格判定「有类型售价取类型售价、没有取通用售价」，各规格结果再汇总取
     * min/max</b>，与 C 端成交价的等级锁定同粒度（{@code WholesalePriceResolver} 按 skuId 探测：
     * 该 SKU 配了类型售价就不再看通用售价）。
     *
     * <p>例：SKU-A 配了一级批发商价 → 取 A 的等级价；SKU-B 只配了通用价 → 取 B 的通用价；
     * 两者一起参与区间。同一规格下存在多个等级时，各等级价一并参与。
     *
     * <p>价格为 0 / 空的空壳行按 {@link WholesalePriceResolver#hasUsablePrice} 一律剔除，
     * 因此「等级行只剩空壳」等价于未配等级价，会退回通用价。
     *
     * <p><b>折扣行直接取折率 min/max，不换算成金额</b>（多规格下各 SKU 零售价不同，换算出的
     * 金额区间无法体现「打几折」这一配置本身）；固定价行取金额 min/max。
     * 折扣段格式 {@code 4.00折-8.00折}；固定金额段格式 {@code ¥60.99-¥600.99}；
     * 两者混存时以 {@code  / } 连接两段。
     */
    private String formatPriceRange(List<ProductWholesalePrice> rows) {
        if (rows == null || rows.isEmpty()) {
            return EMPTY_DISPLAY;
        }
        List<ProductWholesalePrice> usable = rows.stream()
                .filter(r -> r != null && WholesalePriceResolver.hasUsablePrice(r))
                .collect(Collectors.toList());
        if (usable.isEmpty()) {
            return EMPTY_DISPLAY;
        }
        // 逐规格判定：该 SKU 配了类型售价就只取它的类型售价，没配才取它的通用售价；
        // 各规格选出的行再汇总取 min/max（与 C 端 resolver 按 skuId 探测等级锁定同粒度）
        Map<Integer, List<ProductWholesalePrice>> bySku = usable.stream()
                .collect(Collectors.groupingBy(r -> r.getSkuId() == null ? GRADE_UNIVERSAL : r.getSkuId()));
        List<ProductWholesalePrice> source = new ArrayList<>();
        for (List<ProductWholesalePrice> rowsOfSku : bySku.values()) {
            List<ProductWholesalePrice> graded = rowsOfSku.stream()
                    .filter(r -> r.getGradeId() != null && r.getGradeId() > GRADE_UNIVERSAL)
                    .collect(Collectors.toList());
            source.addAll(graded.isEmpty()
                    ? rowsOfSku.stream()
                            .filter(r -> r.getGradeId() == null || r.getGradeId() == GRADE_UNIVERSAL)
                            .collect(Collectors.toList())
                    : graded);
        }
        if (source.isEmpty()) {
            return EMPTY_DISPLAY;
        }

        List<BigDecimal> discounts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        for (ProductWholesalePrice r : source) {
            if (r.getPriceMode() != null && r.getPriceMode() == PRICE_MODE_DISCOUNT) {
                if (r.getDiscount() != null) {
                    discounts.add(r.getDiscount());
                }
            } else if (r.getWholesalePrice() != null) {
                amounts.add(r.getWholesalePrice());
            }
        }

        List<String> segments = new ArrayList<>();
        if (!discounts.isEmpty()) {
            BigDecimal min = Collections.min(discounts);
            BigDecimal max = Collections.max(discounts);
            segments.add(min.compareTo(max) == 0
                    ? discount(min)
                    : discount(min) + "-" + discount(max));
        }
        if (!amounts.isEmpty()) {
            BigDecimal min = Collections.min(amounts);
            BigDecimal max = Collections.max(amounts);
            segments.add(min.compareTo(max) == 0
                    ? money(min)
                    : money(min) + "-" + money(max));
        }
        return segments.isEmpty() ? EMPTY_DISPLAY : String.join(" / ", segments);
    }

    private String money(BigDecimal v) {
        return settingConfigService.getCurrencySymbol() + v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String discount(BigDecimal v) {
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + "折";
    }

    private IPage<WholesaleProductVO> emptyPage(Page<ProductWholesaleConfig> page) {
        Page<WholesaleProductVO> empty = new Page<>(page.getCurrent(), page.getSize(), 0);
        empty.setRecords(Collections.emptyList());
        return empty;
    }

    // ====================== TASK-WS-208 商品起批规则（C 端 / 预览） ======================

    @Override
    public WholesaleProductBuyRuleVO buildBuyRule(Integer productId) {
        WholesaleProductBuyRuleVO vo = new WholesaleProductBuyRuleVO();
        vo.setProductId(productId);

        ProductWholesaleConfig config = configService.getActiveByProductId(productId);
        if (config == null) {
            vo.setConfigured(false);
            vo.setSkuRules(Collections.emptyList());
            return vo;
        }
        vo.setConfigured(true);
        vo.setDimension(config.getMoqDimension());
        vo.setMoq(config.getMoq());

        List<ProductWholesaleSkuConfig> skuConfigs = skuConfigService.listByProductId(productId);
        List<WholesaleSkuRuleVO> skuRules = skuConfigs == null
                ? Collections.emptyList()
                : skuConfigs.stream()
                        .map(sc -> {
                            WholesaleSkuRuleVO r = new WholesaleSkuRuleVO();
                            r.setSkuId(sc.getSkuId());
                            r.setMultiple(sc.getMultiple());
                            return r;
                        })
                        .collect(Collectors.toList());
        vo.setSkuRules(skuRules);
        vo.setHint(buildBuyRuleHint(config, skuRules));
        return vo;
    }

    /**
     * 组装起批规则展示文案（CR-20260604-0937）。dimension=1 为 SPU 加总，dimension=2 为每 SKU 起订；
     * 倍数取所有 SKU 中 {@code multiple>1} 的最小值（与 admin 端 buildHint 一致，保证 C/B 端文案一致）。
     */
    private String buildBuyRuleHint(ProductWholesaleConfig config, List<WholesaleSkuRuleVO> skuRules) {
        boolean spu = config.getMoqDimension() != null && config.getMoqDimension() == MOQ_DIMENSION_SPU;
        int moq = config.getMoq() != null ? config.getMoq() : 1;
        StringBuilder sb = new StringBuilder();
        if (spu) {
            sb.append("批发起订 ").append(moq).append(" 件");
        } else {
            sb.append("按 SKU 起订，每规格 ").append(moq).append(" 件起");
        }
        Integer multiple = skuRules.stream()
                .map(WholesaleSkuRuleVO::getMultiple)
                .filter(m -> m != null && m > 1)
                .min(Integer::compareTo)
                .orElse(null);
        if (multiple != null) {
            sb.append("，需按 ").append(multiple).append(" 的倍数购买");
        }
        return sb.toString();
    }
}
