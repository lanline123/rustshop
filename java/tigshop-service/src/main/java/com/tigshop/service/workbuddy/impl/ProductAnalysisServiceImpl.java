package com.tigshop.service.workbuddy.impl;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.dto.workbuddy.ProductSearchDTO;
import com.tigshop.bean.dto.workbuddy.ProductAnalysisDTO;
import com.tigshop.bean.vo.workbuddy.ProductAnalysisVO;
import com.tigshop.bean.vo.workbuddy.ProductSearchVO;
import com.tigshop.bean.vo.workbuddy.ProductSummaryVO;
import com.tigshop.service.workbuddy.ProductAnalysisService;
import com.tigshop.service.workbuddy.ProductManageService;
import com.tigshop.service.workbuddy.WorkBuddyApiException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/** 基于 Tigshop 内部商品列表的只读分析实现。 */
@Service
public class ProductAnalysisServiceImpl implements ProductAnalysisService {
    private static final Set<String> OPERATIONS = Set.of("overview", "top_sellers", "slow_movers",
            "inventory_risk", "price_bands", "category_performance", "recommend");
    private final ProductManageService productManageService;

    public ProductAnalysisServiceImpl(ProductManageService productManageService) {
        this.productManageService = productManageService;
    }

    @Override
    public ProductAnalysisVO analyze(ProductAnalysisDTO dto, Integer shopId) {
        String operation = dto == null ? null : dto.getOperation();
        String op = StrUtil.isBlank(operation) ? "overview" : operation.trim().toLowerCase(Locale.ROOT);
        if (!OPERATIONS.contains(op)) {
            throw new WorkBuddyApiException("INVALID_OPERATION", "不支持的分析操作: " + operation, 400, false);
        }
        Integer sampleSize = dto == null ? null : dto.getSampleSize();
        int size = sampleSize == null ? 100 : sampleSize;
        if (size < 1 || size > 100) throw new WorkBuddyApiException("INVALID_SAMPLE_SIZE", "sampleSize 必须在 1 到 100 之间", 400, false);
        ProductSearchDTO query = new ProductSearchDTO();
        query.setKeyword(StrUtil.trimToNull(dto == null ? null : dto.getKeyword())); query.setCategoryId(dto == null ? null : dto.getCategoryId());
        if (dto != null && dto.getStartTime() != null && dto.getEndTime() != null
                && dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new WorkBuddyApiException("INVALID_TIME_RANGE", "startTime 不能晚于 endTime", 400, false);
        }
        query.setAddedStartTime(dto == null ? null : dto.getStartTime());
        query.setAddedEndTime(dto == null ? null : dto.getEndTime());
        query.setScope("all"); query.setSize(size); query.setPage(dto == null || dto.getPage() == null ? 1 : Math.max(1, dto.getPage()));
        ProductSearchVO result = productManageService.search(query, shopId);
        List<ProductSummaryVO> items = result == null || result.getItems() == null ? List.of() : result.getItems();

        ProductAnalysisVO out = new ProductAnalysisVO();
        out.setDataScope("Tigshop内部商品数据"); out.setSampleSize(items.size()); out.setGeneratedAt(java.time.LocalDateTime.now());
        out.setDataStartTime(dto == null ? null : dto.getStartTime()); out.setDataEndTime(dto == null ? null : dto.getEndTime());
        out.setConfidence(items.isEmpty() ? 0d : (items.size() < 5 ? 0.5d : 0.9d));
        out.setMetrics(metrics(items, result)); out.setRankings(rankings(op, items)); out.setRecommendations(recommendations(op, items));
        if (items.isEmpty()) out.setFallbackMessage("无足够内部数据，仅可提供通用经验");
        return out;
    }

    private Map<String, Object> metrics(List<ProductSummaryVO> items, ProductSearchVO result) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productCount", result != null && result.getTotal() != null ? result.getTotal().intValue() : items.size());
        m.put("sales", items.stream().map(ProductSummaryVO::getSales).filter(Objects::nonNull).mapToInt(Integer::intValue).sum());
        m.put("stock", items.stream().map(ProductSummaryVO::getStock).filter(Objects::nonNull).mapToInt(Integer::intValue).sum());
        List<BigDecimal> prices = items.stream().map(ProductSummaryVO::getPrice).filter(Objects::nonNull).toList();
        BigDecimal avg = prices.isEmpty() ? null : normalizeNumber(prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP));
        m.put("averagePrice", avg); m.put("priceMin", prices.stream().min(Comparator.naturalOrder()).orElse(null));
        m.put("priceMax", prices.stream().max(Comparator.naturalOrder()).orElse(null));
        m.put("categoryDistribution", distribution(items, ProductAnalysisServiceImpl::categoryOf, "categoryName"));
        m.put("priceDistribution", distribution(items, v -> priceBand(v.getPrice()), "band"));
        return m;
    }

    private static BigDecimal normalizeNumber(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        return normalized.scale() < 0 ? normalized.setScale(0) : normalized;
    }

    private List<Map<String, Object>> rankings(String op, List<ProductSummaryVO> items) {
        if (op.equals("category_performance")) {
            return distribution(items, ProductAnalysisServiceImpl::categoryOf, "categoryName").stream()
                    .sorted((a, b) -> Integer.compare((Integer) b.get("sales"), (Integer) a.get("sales"))).toList();
        }
        if (op.equals("price_bands")) {
            return distribution(items, v -> priceBand(v.getPrice()), "band");
        }
        if (!(op.equals("top_sellers") || op.equals("slow_movers") || op.equals("inventory_risk"))) return List.of();
        if (op.equals("inventory_risk")) {
            return items.stream().map(this::inventoryRisk).sorted(Comparator
                    .comparing((Map<String, Object> v) -> (Integer) v.get("riskScore"), Comparator.reverseOrder())
                    .thenComparing(v -> (Integer) v.get("stock"), Comparator.nullsLast(Comparator.naturalOrder())))
                    .limit(20).toList();
        }
        Comparator<ProductSummaryVO> c = Comparator.comparing(v -> Optional.ofNullable(v.getSales()).orElse(0));
        if (!op.equals("slow_movers")) c = c.reversed();
        return items.stream().sorted(c).limit(20).map(this::summary).toList();
    }

    private static String categoryOf(ProductSummaryVO product) {
        return Optional.ofNullable(product.getCategoryName()).filter(StrUtil::isNotBlank).orElse("未分类");
    }

    /**
     * 按商品当前样本聚合分布。所有比例均为 0~1 的 BigDecimal，避免浮点误差让客户端展示出 100.0000001%。
     */
    private List<Map<String, Object>> distribution(List<ProductSummaryVO> items,
                                                    java.util.function.Function<ProductSummaryVO, String> keyFn,
                                                    String keyName) {
        int totalSales = items.stream().map(ProductSummaryVO::getSales).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        int totalStock = items.stream().map(ProductSummaryVO::getStock).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        Map<String, List<ProductSummaryVO>> groups = items.stream().collect(Collectors.groupingBy(keyFn, LinkedHashMap::new, Collectors.toList()));
        return groups.entrySet().stream().map(entry -> {
            List<ProductSummaryVO> group = entry.getValue();
            int sales = group.stream().map(ProductSummaryVO::getSales).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
            int stock = group.stream().map(ProductSummaryVO::getStock).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
            List<BigDecimal> prices = group.stream().map(ProductSummaryVO::getPrice).filter(Objects::nonNull).toList();
            Map<String, Object> row = new LinkedHashMap<>();
            String key = entry.getKey();
            row.put(keyName, key);
            row.put("productCount", group.size()); row.put("sales", sales); row.put("stock", stock);
            row.put("salesShare", ratio(sales, totalSales));
            row.put("stockShare", ratio(stock, totalStock));
            row.put("averagePrice", prices.isEmpty() ? null : normalizeNumber(prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP)));
            return row;
        }).toList();
    }

    private static BigDecimal ratio(int numerator, int denominator) {
        if (denominator <= 0) return BigDecimal.ZERO.setScale(2);
        return normalizeNumber(BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP));
    }

    private Map<String, Object> inventoryRisk(ProductSummaryVO product) {
        int stock = Optional.ofNullable(product.getStock()).orElse(0);
        int sales = Optional.ofNullable(product.getSales()).orElse(0);
        BigDecimal stockToSales = BigDecimal.valueOf(stock)
                .divide(BigDecimal.valueOf(Math.max(sales, 1)), 2, RoundingMode.HALF_UP);
        List<String> reasons = new ArrayList<>();
        int score = 0;
        if (stock <= 10) { score += 2; reasons.add("低库存"); }
        if (sales <= 0) { score++; reasons.add("无销量"); }
        else if (stockToSales.compareTo(BigDecimal.valueOf(30)) >= 0) { score++; reasons.add("周转缓慢"); }
        Map<String, Object> row = summary(product);
        row.put("stockToSalesRatio", normalizeNumber(stockToSales));
        row.put("riskScore", score);
        row.put("riskLevel", score >= 2 ? "HIGH" : score == 1 ? "MEDIUM" : "LOW");
        row.put("riskReasons", reasons);
        return row;
    }

    private static String priceBand(BigDecimal price) {
        if (price == null) return "未知";
        if (price.compareTo(BigDecimal.valueOf(50)) < 0) return "0-50";
        if (price.compareTo(BigDecimal.valueOf(200)) < 0) return "50-200";
        if (price.compareTo(BigDecimal.valueOf(500)) < 0) return "200-500";
        return "500以上";
    }

    private List<Map<String, Object>> recommendations(String op, List<ProductSummaryVO> items) {
        if (!op.equals("recommend") || items.isEmpty()) return List.of();
        Map<String, Object> aggregate = metrics(items, null);
        BigDecimal avg = (BigDecimal) aggregate.get("averagePrice");
        List<Map<String, Object>> result = new ArrayList<>();
        items.stream().filter(v -> Optional.ofNullable(v.getStock()).orElse(0) <= 10)
                .filter(v -> Optional.ofNullable(v.getSales()).orElse(0) > 0).findFirst()
                .ifPresent(v -> result.add(recommendation("replenish", "HIGH", v,
                        "优先补货，避免热销商品断货", "销量" + value(v.getSales()) + "、库存" + value(v.getStock()) + "，当前库存偏低")));
        items.stream().filter(v -> Optional.ofNullable(v.getStock()).orElse(0) > 10)
                .filter(v -> Optional.ofNullable(v.getSales()).orElse(0) <= 5).findFirst()
                .ifPresent(v -> result.add(recommendation("clearance", "MEDIUM", v,
                        "考虑促销或降低备货，释放积压库存", "销量" + value(v.getSales()) + "、库存" + value(v.getStock()) + "，库存销售比偏高")));
        if (avg != null) {
            Map<String, Object> pricing = new LinkedHashMap<>();
            pricing.put("type", "pricing"); pricing.put("priority", "MEDIUM"); pricing.put("value", avg);
            pricing.put("suggestion", "以样本均价作为新商品定价参考");
            pricing.put("reason", "当前样本价格分布的中心值可作为初始定价锚点");
            pricing.put("evidence", "样本均价=" + avg + "，覆盖" + items.size() + "个商品");
            pricing.put("source", "Tigshop内部商品数据"); result.add(pricing);
        }
        distribution(items, ProductAnalysisServiceImpl::categoryOf, "categoryName").stream().max(Comparator.comparing(v -> (Integer) v.get("sales")))
                .ifPresent(v -> {
                    Map<String, Object> category = new LinkedHashMap<>(); category.put("type", "category"); category.put("priority", "MEDIUM");
                    category.put("value", v.get("categoryName")); category.put("suggestion", "优先关注该品类并扩充相近商品");
                    category.put("reason", "该品类在当前样本中贡献最高销量");
                    category.put("evidence", "品类=" + v.get("categoryName") + "，销量=" + v.get("sales") + "，销量占比=" + v.get("salesShare"));
                    category.put("source", "Tigshop内部商品数据"); result.add(category);
                });
        return result;
    }

    private Map<String, Object> recommendation(String type, String priority, ProductSummaryVO p, String suggestion, String evidence) {
        Map<String, Object> row = new LinkedHashMap<>(); row.put("type", type); row.put("priority", priority);
        row.put("productId", p.getProductId()); row.put("productName", p.getProductName()); row.put("suggestion", suggestion);
        row.put("reason", suggestion); row.put("evidence", evidence); row.put("source", "Tigshop内部商品数据"); return row;
    }

    private static String value(Object value) { return value == null ? "0" : value.toString(); }

    private Map<String, Object> summary(ProductSummaryVO p) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("productId", p.getProductId()); s.put("productName", p.getProductName());
        s.put("sales", p.getSales()); s.put("stock", p.getStock()); s.put("price", p.getPrice());
        s.put("categoryName", p.getCategoryName()); return s;
    }
}
