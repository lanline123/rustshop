package com.tigshop.service.finance.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.enums.finance.RateSourceEnum;
import com.tigshop.bean.enums.finance.RuleScopeTypeEnum;
import com.tigshop.bean.enums.finance.RuleTargetTypeEnum;
import com.tigshop.bean.model.finance.CategoryServiceFeeRule;
import com.tigshop.bean.model.finance.CategoryServiceFeeRuleCategory;
import com.tigshop.bean.model.finance.CategoryServiceFeeRuleTarget;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleMatchVO;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleTargetVO;
import com.tigshop.mapper.finance.CategoryServiceFeeRuleCategoryMapper;
import com.tigshop.mapper.finance.CategoryServiceFeeRuleMapper;
import com.tigshop.mapper.finance.CategoryServiceFeeRuleTargetMapper;
import com.tigshop.service.finance.CategoryFeeRuleMatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 类目费率规则匹配（对齐微服务 InternalCategoryFeeRuleServiceImpl）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryFeeRuleMatchServiceImpl implements CategoryFeeRuleMatchService {

    private static final String MATCH_PREFIX = "tigshop:feeRule:match:";
    private static final String TARGET_LIST_PREFIX = "tigshop:feeRule:target:";

    private final CategoryServiceFeeRuleMapper ruleMapper;
    private final CategoryServiceFeeRuleCategoryMapper ruleCategoryMapper;
    private final CategoryServiceFeeRuleTargetMapper ruleTargetMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(readOnly = true)
    public CategoryFeeRuleMatchVO match(int targetType, int targetId, int categoryId) {
        String cacheKey = MATCH_PREFIX + targetType + ":" + targetId + ":" + categoryId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return parseCached(cached);
            }
        } catch (Exception e) {
            log.warn("读取费率规则匹配缓存失败 key={}", cacheKey, e);
        }
        CategoryFeeRuleMatchVO result = doMatch(targetType, targetId, categoryId);
        writeCache(cacheKey, result);
        return result;
    }

    /** Hutool 2.x 使用 toBean(String, Class) */
    private CategoryFeeRuleMatchVO parseCached(String cached) {
        return JSONUtil.toBean(cached, CategoryFeeRuleMatchVO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, CategoryFeeRuleMatchVO> batchMatch(int targetType, int targetId, Collection<Integer> categoryIds) {
        Map<Integer, CategoryFeeRuleMatchVO> out = new HashMap<>();
        if (categoryIds == null || categoryIds.isEmpty()) {
            return out;
        }
        List<Integer> uncached = new ArrayList<>();
        for (Integer categoryId : categoryIds) {
            String cacheKey = MATCH_PREFIX + targetType + ":" + targetId + ":" + categoryId;
            try {
                String cached = stringRedisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    out.put(categoryId, parseCached(cached));
                    continue;
                }
            } catch (Exception e) {
                log.warn("读取费率规则匹配缓存失败 key={}", cacheKey, e);
            }
            uncached.add(categoryId);
        }
        if (!uncached.isEmpty()) {
            Map<Integer, CategoryFeeRuleMatchVO> computed = doBatchMatch(targetType, targetId, uncached);
            for (Map.Entry<Integer, CategoryFeeRuleMatchVO> e : computed.entrySet()) {
                writeCache(MATCH_PREFIX + targetType + ":" + targetId + ":" + e.getKey(), e.getValue());
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryFeeRuleTargetVO> listByTarget(int targetType, int targetId) {
        String cacheKey = TARGET_LIST_PREFIX + targetType + ":" + targetId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return JSONUtil.toList(cached, CategoryFeeRuleTargetVO.class);
            }
        } catch (Exception e) {
            log.warn("读取费率规则目标缓存失败 key={}", cacheKey, e);
        }
        List<CategoryServiceFeeRule> enabledRules = listEnabledRules(targetType);
        if (enabledRules.isEmpty()) {
            writeCache(cacheKey, Collections.emptyList());
            return Collections.emptyList();
        }
        List<Integer> ruleIds = enabledRules.stream().map(CategoryServiceFeeRule::getId).collect(Collectors.toList());
        List<CategoryServiceFeeRuleCategory> allCategories = listCategoriesByRuleIds(ruleIds);
        List<CategoryServiceFeeRuleTarget> allTargets = listTargetsByRuleIds(ruleIds);
        Map<Integer, List<CategoryServiceFeeRuleCategory>> categoriesByRule = allCategories.stream()
                .collect(Collectors.groupingBy(CategoryServiceFeeRuleCategory::getRuleId));
        Map<Integer, List<CategoryServiceFeeRuleTarget>> targetsByRule = allTargets.stream()
                .collect(Collectors.groupingBy(CategoryServiceFeeRuleTarget::getRuleId));

        List<CategoryFeeRuleTargetVO> result = new ArrayList<>();
        for (CategoryServiceFeeRule rule : enabledRules) {
            boolean covers;
            if (RuleScopeTypeEnum.SPECIFIED.getCode() == rule.getScopeType()) {
                List<CategoryServiceFeeRuleTarget> ruleTargets = targetsByRule.getOrDefault(rule.getId(), Collections.emptyList());
                covers = ruleTargets.stream().anyMatch(t -> t.getTargetId().equals(targetId));
            } else if (RuleScopeTypeEnum.EXCLUDE.getCode() == rule.getScopeType()) {
                List<CategoryServiceFeeRuleTarget> ruleTargets = targetsByRule.getOrDefault(rule.getId(), Collections.emptyList());
                covers = ruleTargets.stream().noneMatch(t -> t.getTargetId().equals(targetId));
            } else {
                covers = true;
            }
            if (covers) {
                CategoryFeeRuleTargetVO vo = new CategoryFeeRuleTargetVO();
                vo.setRuleId(rule.getId());
                vo.setRuleName(rule.getRuleName());
                vo.setPrecisionLayer(rule.getPrecisionLayer());
                vo.setServiceFeeRate(rule.getServiceFeeRate());
                List<CategoryServiceFeeRuleCategory> ruleCats = categoriesByRule.getOrDefault(rule.getId(), Collections.emptyList());
                vo.setCategoryNames(ruleCats.stream()
                        .map(c -> c.getCategoryName() != null ? c.getCategoryName() : "")
                        .collect(Collectors.toList()));
                result.add(vo);
            }
        }
        writeCache(cacheKey, result);
        return result;
    }

    @Override
    public void evictCacheByTargetType(int targetType) {
        try {
            deleteByPattern(MATCH_PREFIX + targetType + ":*");
            deleteByPattern(TARGET_LIST_PREFIX + targetType + ":*");
        } catch (Exception e) {
            log.warn("清除费率规则缓存失败 targetType={}", targetType, e);
        }
    }

    private void deleteByPattern(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
        List<String> keys = new ArrayList<>();
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    private List<CategoryServiceFeeRule> listEnabledRules(int targetType) {
        return ruleMapper.selectList(new LambdaQueryWrapper<CategoryServiceFeeRule>()
                .eq(CategoryServiceFeeRule::getTargetType, targetType)
                .eq(CategoryServiceFeeRule::getStatus, 1)
                .eq(CategoryServiceFeeRule::getDeleted, 0));
    }

    private List<CategoryServiceFeeRuleCategory> listCategoriesByRuleIds(List<Integer> ruleIds) {
        if (ruleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return ruleCategoryMapper.selectList(new LambdaQueryWrapper<CategoryServiceFeeRuleCategory>()
                .in(CategoryServiceFeeRuleCategory::getRuleId, ruleIds)
                .eq(CategoryServiceFeeRuleCategory::getDeleted, 0));
    }

    private List<CategoryServiceFeeRuleTarget> listTargetsByRuleIds(List<Integer> ruleIds) {
        if (ruleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return ruleTargetMapper.selectList(new LambdaQueryWrapper<CategoryServiceFeeRuleTarget>()
                .in(CategoryServiceFeeRuleTarget::getRuleId, ruleIds)
                .eq(CategoryServiceFeeRuleTarget::getDeleted, 0));
    }

    private CategoryFeeRuleMatchVO doMatch(int targetType, int targetId, int categoryId) {
        List<CategoryServiceFeeRule> enabledRules = listEnabledRules(targetType);
        if (enabledRules.isEmpty()) {
            return unmatched();
        }
        List<Integer> ruleIds = enabledRules.stream().map(CategoryServiceFeeRule::getId).collect(Collectors.toList());
        List<CategoryServiceFeeRuleCategory> allCategories = listCategoriesByRuleIds(ruleIds);
        List<CategoryServiceFeeRuleTarget> allTargets = listTargetsByRuleIds(ruleIds);
        Map<Integer, List<CategoryServiceFeeRuleCategory>> categoriesByRule = allCategories.stream()
                .collect(Collectors.groupingBy(CategoryServiceFeeRuleCategory::getRuleId));
        Map<Integer, List<CategoryServiceFeeRuleTarget>> targetsByRule = allTargets.stream()
                .collect(Collectors.groupingBy(CategoryServiceFeeRuleTarget::getRuleId));

        List<CategoryServiceFeeRule> specified = enabledRules.stream()
                .filter(r -> RuleScopeTypeEnum.SPECIFIED.getCode() == r.getScopeType()).collect(Collectors.toList());
        List<CategoryServiceFeeRule> exclude = enabledRules.stream()
                .filter(r -> RuleScopeTypeEnum.EXCLUDE.getCode() == r.getScopeType()).collect(Collectors.toList());
        List<CategoryServiceFeeRule> allScope = enabledRules.stream()
                .filter(r -> RuleScopeTypeEnum.ALL.getCode() == r.getScopeType()).collect(Collectors.toList());

        return doMatchWithContext(targetType, targetId, categoryId, specified, exclude, allScope, categoriesByRule, targetsByRule);
    }

    private Map<Integer, CategoryFeeRuleMatchVO> doBatchMatch(int targetType, int targetId, List<Integer> categoryIds) {
        List<CategoryServiceFeeRule> enabledRules = listEnabledRules(targetType);
        Map<Integer, CategoryFeeRuleMatchVO> results = new HashMap<>();
        if (enabledRules.isEmpty()) {
            for (Integer cid : categoryIds) {
                results.put(cid, unmatched());
            }
            return results;
        }
        List<Integer> ruleIds = enabledRules.stream().map(CategoryServiceFeeRule::getId).collect(Collectors.toList());
        List<CategoryServiceFeeRuleCategory> allCategories = listCategoriesByRuleIds(ruleIds);
        List<CategoryServiceFeeRuleTarget> allTargets = listTargetsByRuleIds(ruleIds);
        Map<Integer, List<CategoryServiceFeeRuleCategory>> categoriesByRule = allCategories.stream()
                .collect(Collectors.groupingBy(CategoryServiceFeeRuleCategory::getRuleId));
        Map<Integer, List<CategoryServiceFeeRuleTarget>> targetsByRule = allTargets.stream()
                .collect(Collectors.groupingBy(CategoryServiceFeeRuleTarget::getRuleId));

        List<CategoryServiceFeeRule> specified = enabledRules.stream()
                .filter(r -> RuleScopeTypeEnum.SPECIFIED.getCode() == r.getScopeType()).collect(Collectors.toList());
        List<CategoryServiceFeeRule> exclude = enabledRules.stream()
                .filter(r -> RuleScopeTypeEnum.EXCLUDE.getCode() == r.getScopeType()).collect(Collectors.toList());
        List<CategoryServiceFeeRule> allScope = enabledRules.stream()
                .filter(r -> RuleScopeTypeEnum.ALL.getCode() == r.getScopeType()).collect(Collectors.toList());

        for (Integer cid : categoryIds) {
            results.put(cid, doMatchWithContext(targetType, targetId, cid, specified, exclude, allScope, categoriesByRule, targetsByRule));
        }
        return results;
    }

    private CategoryFeeRuleMatchVO doMatchWithContext(
            int targetType,
            int targetId,
            int categoryId,
            List<CategoryServiceFeeRule> specifiedRules,
            List<CategoryServiceFeeRule> excludeRules,
            List<CategoryServiceFeeRule> allScopeRules,
            Map<Integer, List<CategoryServiceFeeRuleCategory>> categoriesByRule,
            Map<Integer, List<CategoryServiceFeeRuleTarget>> targetsByRule) {

        for (CategoryServiceFeeRule rule : specifiedRules) {
            List<CategoryServiceFeeRuleTarget> ruleTargets = targetsByRule.getOrDefault(rule.getId(), Collections.emptyList());
            boolean targetMatch = ruleTargets.stream().anyMatch(t -> t.getTargetId().equals(targetId));
            if (!targetMatch) {
                continue;
            }
            List<CategoryServiceFeeRuleCategory> ruleCategories = categoriesByRule.getOrDefault(rule.getId(), Collections.emptyList());
            if (ruleCategories.stream().noneMatch(c -> c.getCategoryId().equals(categoryId))) {
                continue;
            }
            return fillMatch(rule, targetType);
        }
        for (CategoryServiceFeeRule rule : excludeRules) {
            List<CategoryServiceFeeRuleTarget> ruleTargets = targetsByRule.getOrDefault(rule.getId(), Collections.emptyList());
            if (ruleTargets.stream().anyMatch(t -> t.getTargetId().equals(targetId))) {
                continue;
            }
            List<CategoryServiceFeeRuleCategory> ruleCategories = categoriesByRule.getOrDefault(rule.getId(), Collections.emptyList());
            if (ruleCategories.stream().noneMatch(c -> c.getCategoryId().equals(categoryId))) {
                continue;
            }
            return fillMatch(rule, targetType);
        }
        for (CategoryServiceFeeRule rule : allScopeRules) {
            List<CategoryServiceFeeRuleCategory> ruleCategories = categoriesByRule.getOrDefault(rule.getId(), Collections.emptyList());
            if (ruleCategories.stream().anyMatch(c -> c.getCategoryId().equals(categoryId))) {
                return fillMatch(rule, targetType);
            }
        }
        return unmatched();
    }

    private CategoryFeeRuleMatchVO fillMatch(CategoryServiceFeeRule rule, int targetType) {
        CategoryFeeRuleMatchVO vo = new CategoryFeeRuleMatchVO();
        vo.setMatched(true);
        vo.setRuleId(rule.getId());
        vo.setRuleName(rule.getRuleName());
        vo.setPrecisionLayer(rule.getPrecisionLayer());
        vo.setServiceFeeRate(rule.getServiceFeeRate());
        vo.setScopeType(rule.getScopeType());
        vo.setRateSource(resolveRateSource(targetType, rule.getScopeType()));
        return vo;
    }

    private String resolveRateSource(int targetType, Integer scopeType) {
        boolean shop = targetType == RuleTargetTypeEnum.SHOP.getCode();
        if (scopeType == null) {
            return RateSourceEnum.GLOBAL_DEFAULT.getValue();
        }
        if (scopeType == RuleScopeTypeEnum.SPECIFIED.getCode()) {
            return shop ? RateSourceEnum.RULE_SHOP_LAYER1.getValue() : RateSourceEnum.RULE_VENDOR_LAYER1.getValue();
        }
        if (scopeType == RuleScopeTypeEnum.EXCLUDE.getCode()) {
            return shop ? RateSourceEnum.RULE_SHOP_LAYER2.getValue() : RateSourceEnum.RULE_VENDOR_LAYER2.getValue();
        }
        return shop ? RateSourceEnum.RULE_SHOP_LAYER3.getValue() : RateSourceEnum.RULE_VENDOR_LAYER3.getValue();
    }

    private CategoryFeeRuleMatchVO unmatched() {
        CategoryFeeRuleMatchVO vo = new CategoryFeeRuleMatchVO();
        vo.setMatched(false);
        return vo;
    }

    private void writeCache(String key, Object value) {
        try {
            long ttl = 30 * 60L + ThreadLocalRandom.current().nextInt(300);
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入费率规则缓存失败 key={}", key, e);
        }
    }
}
