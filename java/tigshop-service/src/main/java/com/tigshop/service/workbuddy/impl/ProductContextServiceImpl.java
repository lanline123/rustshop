package com.tigshop.service.workbuddy.impl;

import cn.hutool.core.util.StrUtil;
import com.tigshop.bean.dto.workbuddy.ProductSearchDTO;
import com.tigshop.bean.vo.workbuddy.ProductContextVO;
import com.tigshop.bean.vo.workbuddy.ProductSearchVO;
import com.tigshop.bean.vo.workbuddy.ProductSummaryVO;
import com.tigshop.service.workbuddy.ProductContextService;
import com.tigshop.service.workbuddy.ProductManageService;
import com.tigshop.service.workbuddy.WorkBuddyApiException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProductContextServiceImpl implements ProductContextService {
    private static final double DOMINANT_ENCODING_THRESHOLD = 0.6d;
    private static final int MIN_DOMINANT_ENCODING_SAMPLES = 3;
    private final ProductManageService productManageService;
    public ProductContextServiceImpl(ProductManageService productManageService) { this.productManageService = productManageService; }

    @Override
    public ProductContextVO context(String keyword, Integer categoryId, Integer sampleSize, Integer shopId) {
        if (StrUtil.isBlank(keyword) && categoryId == null) throw new WorkBuddyApiException("MISSING_FILTER", "keyword 或 categoryId 至少提供一个", 400, false);
        if (categoryId != null && categoryId <= 0) throw new WorkBuddyApiException("INVALID_CATEGORY_ID", "categoryId 必须为正整数", 400, false);
        int size = sampleSize == null ? 20 : sampleSize;
        if (size < 1 || size > 100) throw new WorkBuddyApiException("INVALID_SAMPLE_SIZE", "sampleSize 必须在 1 到 100 之间", 400, false);
        ProductSearchDTO q = new ProductSearchDTO(); q.setKeyword(StrUtil.trimToNull(keyword)); q.setCategoryId(categoryId); q.setSize(size); q.setScope("all");
        ProductSearchVO result = productManageService.search(q, shopId);
        List<ProductSummaryVO> all = result == null || result.getItems() == null ? List.of() : result.getItems();
        ProductContextVO out = new ProductContextVO();
        out.setSampleSize(all.size());
        out.setHasData(!all.isEmpty());
        List<ProductSummaryVO> samples = new ArrayList<>(all.subList(0, Math.min(20, all.size()))); out.setSamples(samples);
        ProductContextVO.Metrics m = new ProductContextVO.Metrics(); m.setProductCount(result != null && result.getTotal() != null ? result.getTotal().intValue() : all.size()); out.setMetrics(m);
        out.setEncoding(inferEncoding(all, shopId));
        if (all.isEmpty()) { out.setFallback(new ProductContextVO.Fallback("NO_MATCHING_DATA", "没有匹配数据，可先依据通用经验完善商品信息")); return out; }
        List<BigDecimal> prices = all.stream().map(ProductSummaryVO::getPrice).filter(Objects::nonNull).sorted().toList();
        List<Integer> stocks = all.stream().map(ProductSummaryVO::getStock).filter(Objects::nonNull).toList();
        List<Integer> sales = all.stream().map(ProductSummaryVO::getSales).filter(Objects::nonNull).toList();
        m.setPriceMin(prices.isEmpty() ? null : prices.get(0)); m.setPriceMax(prices.isEmpty() ? null : prices.get(prices.size()-1)); m.setPriceMedian(priceMedian(prices));
        m.setStockAvg(avg(stocks)); m.setSalesAvg(avg(sales)); m.setSalesMedian(median(sales));
        m.setStockMedian(median(stocks));
        out.setDefaults(defaults(m));
        ProductSummaryVO first = all.get(0); out.setCategoryId(categoryId); out.setCategoryName(first.getCategoryName());
        java.util.Map<String,Integer> frequencies = new java.util.HashMap<>();
        for (ProductSummaryVO p : all) {
            if (p.getProductName() == null) continue;
            for (String token : p.getProductName().split("[^\\p{L}\\p{Nd}]+")) {
                if (token.length() >= 2 && !java.util.Set.of("商品", "新款", "正品").contains(token)) frequencies.merge(token.toLowerCase(), 1, Integer::sum);
            }
        }
        List<java.util.Map<String,Object>> patterns = frequencies.entrySet().stream().sorted(java.util.Map.Entry.<String,Integer>comparingByValue().reversed()).limit(10)
                .map(e -> { java.util.Map<String,Object> patternMap = new java.util.LinkedHashMap<>(); patternMap.put("type", "title_keyword_frequency"); patternMap.put("keyword", e.getKey()); patternMap.put("count", e.getValue()); return patternMap; }).toList();
        out.setPatterns(patterns);
        return out;
    }

    private static Map<String, Object> defaults(ProductContextVO.Metrics metrics) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (metrics.getPriceMedian() != null) {
            values.put("productPrice", suggestion(metrics.getPriceMedian(), "ai_recommended", 0.55d));
            values.put("marketPrice", suggestion(metrics.getPriceMedian().multiply(new BigDecimal("1.2")).setScale(2, RoundingMode.HALF_UP), "ai_recommended", 0.45d));
        }
        if (metrics.getStockMedian() != null) values.put("productStock", suggestion(Math.max(0, Math.round(metrics.getStockMedian())), "ai_recommended", 0.45d));
        return values;
    }

    private static Map<String, Object> suggestion(Object value, String source, double confidence) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("value", value); v.put("source", source); v.put("confidence", confidence);
        return v;
    }

    private ProductContextVO.EncodingInference inferEncoding(List<ProductSummaryVO> products, Integer shopId) {
        List<String> sns = products.stream().map(ProductSummaryVO::getProductSn).filter(StrUtil::isNotBlank).map(String::trim).distinct().toList();
        ProductContextVO.EncodingInference out = new ProductContextVO.EncodingInference();
        out.setSamples(sns.stream().limit(5).toList());
        if (sns.size() < 2) {
            out.setConfidence(0d); out.setNeedsConfirmation(true); out.setReason("可用商品编码样本不足，无法可靠推断格式"); return out;
        }
        Map<String, List<BigInteger>> groups = new HashMap<>();
        int matchedCount = 0;
        for (String sn : sns) {
            Matcher matcher = Pattern.compile("^(.*?)(\\d+)$").matcher(sn);
            if (!matcher.matches()) continue;
            String prefix = matcher.group(1);
            String digits = matcher.group(2);
            String key = prefix + "#" + digits.length();
            try {
                groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(new BigInteger(digits));
                matchedCount++;
            } catch (NumberFormatException ignored) {
                // Ignore an invalid numeric suffix and let the confidence calculation ask for confirmation.
            }
        }
        if (groups.isEmpty()) {
            out.setConfidence(0.2d); out.setNeedsConfirmation(true); out.setReason("当前商品编码格式不统一，请确认本次使用哪种格式"); return out;
        }
        List<Map.Entry<String, List<BigInteger>>> ranked = groups.entrySet().stream()
                .sorted((left, right) -> {
                    int byCount = Integer.compare(right.getValue().size(), left.getValue().size());
                    return byCount != 0 ? byCount : left.getKey().compareTo(right.getKey());
                })
                .toList();
        Map.Entry<String, List<BigInteger>> entry = ranked.get(0);
        int dominantCount = entry.getValue().size();
        int secondCount = ranked.size() > 1 ? ranked.get(1).getValue().size() : 0;
        double confidence = sns.isEmpty() ? 0d : (double) dominantCount / sns.size();
        boolean decisive = matchedCount == sns.size()
                && dominantCount >= MIN_DOMINANT_ENCODING_SAMPLES
                && confidence >= DOMINANT_ENCODING_THRESHOLD
                && (secondCount == 0 || dominantCount >= secondCount * 2);
        if (!decisive) {
            out.setConfidence(0.2d); out.setNeedsConfirmation(true); out.setReason("当前商品编码格式不统一，请确认本次使用哪种格式"); return out;
        }
        String[] parts = entry.getKey().split("#", 2);
        String prefix = parts[0]; int length = Integer.parseInt(parts[1]);
        List<BigInteger> numbers = entry.getValue();
        BigInteger next = numbers.stream().max(BigInteger::compareTo).orElse(BigInteger.ZERO).add(BigInteger.ONE);
        BigInteger max = BigInteger.TEN.pow(length).subtract(BigInteger.ONE);
        while (next.compareTo(max) <= 0) {
            String candidate = formatCandidate(prefix, length, next);
            if (!productManageService.isProductSnOccupied(candidate, shopId)) {
                break;
            }
            next = next.add(BigInteger.ONE);
        }
        if (next.toString().length() > length) {
            out.setConfidence(0.35d); out.setNeedsConfirmation(true); out.setReason("编码序号已超过历史位数，请确认新的编码规则"); return out;
        }
        String candidate = formatCandidate(prefix, length, next);
        out.setPrefix(prefix); out.setNumberLength(length); out.setNextCandidate(candidate);
        out.setPattern(prefix + "{" + length + "位数字}"); out.setConfidence(Math.min(0.99d, confidence)); out.setNeedsConfirmation(false);
        out.setReason(confidence >= 0.999d
                ? "多个商品使用同一前缀和数字位数，按最大序号顺延"
                : "历史商品中该编码格式占明显多数，按该主流格式的最大序号顺延");
        return out;
    }
    private static String formatCandidate(String prefix, int length, BigInteger number) {
        return prefix + "0".repeat(Math.max(0, length - number.toString().length())) + number;
    }
    private static Double avg(List<Integer> v) { return v.isEmpty() ? null : v.stream().mapToDouble(Integer::doubleValue).average().orElse(0); }
    private static Double median(List<Integer> v) { if (v.isEmpty()) return null; List<Integer> s = v.stream().sorted().toList(); int n=s.size(); return n%2==1?s.get(n/2).doubleValue():(s.get(n/2-1)+s.get(n/2))/2d; }
    private static BigDecimal priceMedian(List<BigDecimal> v) { if (v.isEmpty()) return null; int n=v.size(); return n%2==1?v.get(n/2):v.get(n/2-1).add(v.get(n/2)).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP).stripTrailingZeros(); }
}
