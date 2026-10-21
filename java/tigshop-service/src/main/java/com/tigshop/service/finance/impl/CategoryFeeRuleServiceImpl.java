package com.tigshop.service.finance.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRuleConflictCheckDTO;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRulePageDTO;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRuleSaveDTO;
import com.tigshop.bean.dto.finance.feerule.CategoryFeeRuleUpdateStatusDTO;
import com.tigshop.bean.enums.finance.RuleScopeTypeEnum;
import com.tigshop.bean.enums.finance.RuleTargetTypeEnum;
import com.tigshop.bean.model.finance.CategoryServiceFeeRule;
import com.tigshop.bean.model.finance.CategoryServiceFeeRuleCategory;
import com.tigshop.bean.model.finance.CategoryServiceFeeRuleTarget;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleConflictVO;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleDetailVO;
import com.tigshop.bean.vo.finance.feerule.CategoryFeeRuleListVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.finance.CategoryServiceFeeRuleCategoryMapper;
import com.tigshop.mapper.finance.CategoryServiceFeeRuleMapper;
import com.tigshop.mapper.finance.CategoryServiceFeeRuleTargetMapper;
import com.tigshop.mapper.product.CategoryMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.CategoryFeeRuleMatchService;
import com.tigshop.service.finance.CategoryFeeRuleService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryFeeRuleServiceImpl
        extends BaseServiceImpl<CategoryServiceFeeRuleMapper, CategoryServiceFeeRule>
        implements CategoryFeeRuleService {

    private final CategoryServiceFeeRuleMapper ruleMapper;
    private final CategoryServiceFeeRuleCategoryMapper ruleCategoryMapper;
    private final CategoryServiceFeeRuleTargetMapper ruleTargetMapper;
    private final CategoryMapper categoryMapper;
    private final ShopMapper shopMapper;
    private final VendorMapper vendorMapper;
    private final CategoryFeeRuleMatchService categoryFeeRuleMatchService;

    @Override
    public Page<CategoryFeeRuleListVO> page(CategoryFeeRulePageDTO dto) {
        LambdaQueryWrapper<CategoryServiceFeeRule> w = new LambdaQueryWrapper<CategoryServiceFeeRule>()
                .eq(CategoryServiceFeeRule::getDeleted, 0)
                .eq(CategoryServiceFeeRule::getTargetType, dto.getTargetType())
                .eq(dto.getStatus() != null, CategoryServiceFeeRule::getStatus, dto.getStatus())
                .like(StringUtils.hasText(dto.getKeyword()), CategoryServiceFeeRule::getRuleName, dto.getKeyword());
        Page<CategoryServiceFeeRule> pageParam = new Page<>(dto.getPage(), dto.getSize());
        buildSortOrder(pageParam, dto.getSortField(), dto.getSortOrder());
        Page<CategoryServiceFeeRule> pg = ruleMapper.selectPage(pageParam, w);
        if (pg.getRecords().isEmpty()) {
            Page<CategoryFeeRuleListVO> empty = new Page<>(pg.getCurrent(), pg.getSize(), pg.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        List<Integer> ruleIds = pg.getRecords().stream().map(CategoryServiceFeeRule::getId).collect(Collectors.toList());
        Map<Integer, Long> catCnt = countByRuleIds(ruleIds, true);
        Map<Integer, Long> tgtCnt = countByRuleIds(ruleIds, false);
        List<CategoryFeeRuleListVO> vos = pg.getRecords().stream().map(rule -> toListVO(rule,
                catCnt.getOrDefault(rule.getId(), 0L).intValue(),
                tgtCnt.getOrDefault(rule.getId(), 0L).intValue())).collect(Collectors.toList());
        Page<CategoryFeeRuleListVO> out = new Page<>(pg.getCurrent(), pg.getSize(), pg.getTotal());
        out.setRecords(vos);
        return out;
    }

    private Map<Integer, Long> countByRuleIds(List<Integer> ruleIds, boolean category) {
        if (category) {
            List<CategoryServiceFeeRuleCategory> list = ruleCategoryMapper.selectList(
                    new LambdaQueryWrapper<CategoryServiceFeeRuleCategory>()
                            .in(CategoryServiceFeeRuleCategory::getRuleId, ruleIds)
                            .eq(CategoryServiceFeeRuleCategory::getDeleted, 0));
            return list.stream().collect(Collectors.groupingBy(CategoryServiceFeeRuleCategory::getRuleId, Collectors.counting()));
        }
        List<CategoryServiceFeeRuleTarget> list = ruleTargetMapper.selectList(
                new LambdaQueryWrapper<CategoryServiceFeeRuleTarget>()
                        .in(CategoryServiceFeeRuleTarget::getRuleId, ruleIds)
                        .eq(CategoryServiceFeeRuleTarget::getDeleted, 0));
        return list.stream().collect(Collectors.groupingBy(CategoryServiceFeeRuleTarget::getRuleId, Collectors.counting()));
    }

    private CategoryFeeRuleListVO toListVO(CategoryServiceFeeRule rule, int categoryCount, int targetCount) {
        CategoryFeeRuleListVO vo = new CategoryFeeRuleListVO();
        vo.setId(rule.getId());
        vo.setRuleName(rule.getRuleName());
        vo.setTargetType(rule.getTargetType());
        vo.setTargetTypeName(resolveTargetTypeName(rule.getTargetType()));
        vo.setScopeType(rule.getScopeType());
        vo.setScopeTypeName(resolveScopeTypeName(rule.getScopeType()));
        vo.setPrecisionLayer(rule.getPrecisionLayer());
        vo.setServiceFeeRate(rule.getServiceFeeRate());
        vo.setCategoryCount(categoryCount);
        vo.setTargetCount(targetCount);
        vo.setStatus(rule.getStatus());
        vo.setCreateTime(rule.getCreateTime());
        return vo;
    }

    private String resolveTargetTypeName(Integer t) {
        if (t == null) {
            return "";
        }
        for (RuleTargetTypeEnum e : RuleTargetTypeEnum.values()) {
            if (e.getCode() == t) {
                return e.getLabel();
            }
        }
        return "";
    }

    private String resolveScopeTypeName(Integer s) {
        if (s == null) {
            return "";
        }
        for (RuleScopeTypeEnum e : RuleScopeTypeEnum.values()) {
            if (e.getCode() == s) {
                return e.getLabel();
            }
        }
        return "";
    }

    @Override
    public CategoryFeeRuleDetailVO detail(Integer id) {
        CategoryServiceFeeRule rule = ruleMapper.selectOne(new LambdaQueryWrapper<CategoryServiceFeeRule>()
                .eq(CategoryServiceFeeRule::getId, id)
                .eq(CategoryServiceFeeRule::getDeleted, 0));
        if (rule == null) {
            throw new GlobalException("费率规则不存在");
        }
        long cc = ruleCategoryMapper.selectCount(new LambdaQueryWrapper<CategoryServiceFeeRuleCategory>()
                .eq(CategoryServiceFeeRuleCategory::getRuleId, id)
                .eq(CategoryServiceFeeRuleCategory::getDeleted, 0));
        long tc = ruleTargetMapper.selectCount(new LambdaQueryWrapper<CategoryServiceFeeRuleTarget>()
                .eq(CategoryServiceFeeRuleTarget::getRuleId, id)
                .eq(CategoryServiceFeeRuleTarget::getDeleted, 0));
        CategoryFeeRuleDetailVO vo = new CategoryFeeRuleDetailVO();
        CategoryFeeRuleListVO base = toListVO(rule, (int) cc, (int) tc);
        vo.setId(base.getId());
        vo.setRuleName(base.getRuleName());
        vo.setTargetType(base.getTargetType());
        vo.setTargetTypeName(base.getTargetTypeName());
        vo.setScopeType(base.getScopeType());
        vo.setScopeTypeName(base.getScopeTypeName());
        vo.setPrecisionLayer(base.getPrecisionLayer());
        vo.setServiceFeeRate(base.getServiceFeeRate());
        vo.setCategoryCount(base.getCategoryCount());
        vo.setTargetCount(base.getTargetCount());
        vo.setStatus(base.getStatus());
        vo.setCreateTime(base.getCreateTime());
        vo.setRemark(rule.getRemark());
        vo.setModifyTime(rule.getModifyTime());

        List<CategoryServiceFeeRuleCategory> cats = ruleCategoryMapper.selectList(
                new LambdaQueryWrapper<CategoryServiceFeeRuleCategory>()
                        .eq(CategoryServiceFeeRuleCategory::getRuleId, id)
                        .eq(CategoryServiceFeeRuleCategory::getDeleted, 0));
        vo.setCategories(cats.stream().map(c -> {
            CategoryFeeRuleDetailVO.CategoryItem i = new CategoryFeeRuleDetailVO.CategoryItem();
            i.setCategoryId(c.getCategoryId());
            i.setCategoryName(c.getCategoryName());
            i.setCategoryPath(c.getCategoryPath());
            return i;
        }).collect(Collectors.toList()));

        List<CategoryServiceFeeRuleTarget> tgts = ruleTargetMapper.selectList(
                new LambdaQueryWrapper<CategoryServiceFeeRuleTarget>()
                        .eq(CategoryServiceFeeRuleTarget::getRuleId, id)
                        .eq(CategoryServiceFeeRuleTarget::getDeleted, 0));
        List<CategoryFeeRuleDetailVO.TargetItem> targetItems = tgts.stream().map(t -> {
            CategoryFeeRuleDetailVO.TargetItem i = new CategoryFeeRuleDetailVO.TargetItem();
            i.setTargetId(t.getTargetId());
            return i;
        }).collect(Collectors.toList());
        vo.setTargets(targetItems);
        fillTargetNames(vo.getTargets(), rule.getTargetType());
        return vo;
    }

    private void fillTargetNames(List<CategoryFeeRuleDetailVO.TargetItem> targets, Integer targetType) {
        if (targets == null || targets.isEmpty()) {
            return;
        }
        if (Objects.equals(targetType, RuleTargetTypeEnum.SHOP.getCode())) {
            for (CategoryFeeRuleDetailVO.TargetItem item : targets) {
                Shop shop = shopMapper.selectById(item.getTargetId());
                item.setTargetName(shop != null && shop.getShopTitle() != null ? shop.getShopTitle() : "");
            }
        } else if (Objects.equals(targetType, RuleTargetTypeEnum.VENDOR.getCode())) {
            for (CategoryFeeRuleDetailVO.TargetItem item : targets) {
                Vendor v = vendorMapper.selectOnboardedById(item.getTargetId());
                item.setTargetName(v != null && v.getVendorName() != null ? v.getVendorName() : "");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> save(CategoryFeeRuleSaveDTO dto) {
        validateSaveDto(dto);
        if (existsByRuleName(dto.getTargetType(), dto.getRuleName(), null)) {
            throw new GlobalException("同类型下规则名称已存在");
        }
        assertNoConflict(toConflictDto(dto.getId(), dto));

        CategoryServiceFeeRule rule = new CategoryServiceFeeRule();
        rule.setRuleName(dto.getRuleName());
        rule.setTargetType(dto.getTargetType());
        rule.setScopeType(dto.getScopeType());
        rule.setServiceFeeRate(dto.getServiceFeeRate());
        rule.setRemark(dto.getRemark() != null ? dto.getRemark() : "");
        rule.setStatus(dto.getStatus());
        rule.setDeleted(0);
        rule.setCreateTime(LocalDateTime.now());
        rule.setModifyTime(LocalDateTime.now());
        ruleMapper.insert(rule);
        saveCategories(rule.getId(), dto.getCategoryIds());
        if (RuleScopeTypeEnum.requiresTargetIds(dto.getScopeType()) && dto.getTargetIds() != null && !dto.getTargetIds().isEmpty()) {
            saveTargets(rule.getId(), dto.getTargetIds());
        }
        categoryFeeRuleMatchService.evictCacheByTargetType(dto.getTargetType());
        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> update(CategoryFeeRuleSaveDTO dto) {
        if (dto.getId() == null) {
            throw new GlobalException("规则ID不能为空");
        }
        CategoryServiceFeeRule existing = ruleMapper.selectOne(new LambdaQueryWrapper<CategoryServiceFeeRule>()
                .eq(CategoryServiceFeeRule::getId, dto.getId())
                .eq(CategoryServiceFeeRule::getDeleted, 0));
        if (existing == null) {
            throw new GlobalException("费率规则不存在");
        }
        validateSaveDto(dto);
        if (existsByRuleName(dto.getTargetType(), dto.getRuleName(), dto.getId())) {
            throw new GlobalException("同类型下规则名称已存在");
        }
        assertNoConflict(toConflictDto(dto.getId(), dto));

        CategoryServiceFeeRule rule = new CategoryServiceFeeRule();
        rule.setId(dto.getId());
        rule.setRuleName(dto.getRuleName());
        rule.setTargetType(dto.getTargetType());
        rule.setScopeType(dto.getScopeType());
        rule.setServiceFeeRate(dto.getServiceFeeRate());
        rule.setRemark(dto.getRemark() != null ? dto.getRemark() : "");
        rule.setStatus(dto.getStatus());
        rule.setModifyTime(LocalDateTime.now());
        ruleMapper.updateById(rule);

        ruleCategoryMapper.delete(new LambdaQueryWrapper<CategoryServiceFeeRuleCategory>()
                .eq(CategoryServiceFeeRuleCategory::getRuleId, dto.getId()));
        ruleTargetMapper.delete(new LambdaQueryWrapper<CategoryServiceFeeRuleTarget>()
                .eq(CategoryServiceFeeRuleTarget::getRuleId, dto.getId()));
        saveCategories(dto.getId(), dto.getCategoryIds());
        if (RuleScopeTypeEnum.requiresTargetIds(dto.getScopeType()) && dto.getTargetIds() != null && !dto.getTargetIds().isEmpty()) {
            saveTargets(dto.getId(), dto.getTargetIds());
        }
        categoryFeeRuleMatchService.evictCacheByTargetType(dto.getTargetType());
        if (!Objects.equals(existing.getTargetType(), dto.getTargetType())) {
            categoryFeeRuleMatchService.evictCacheByTargetType(existing.getTargetType());
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> updateStatus(CategoryFeeRuleUpdateStatusDTO dto) {
        CategoryServiceFeeRule rule = ruleMapper.selectOne(new LambdaQueryWrapper<CategoryServiceFeeRule>()
                .eq(CategoryServiceFeeRule::getId, dto.getId())
                .eq(CategoryServiceFeeRule::getDeleted, 0));
        if (rule == null) {
            throw new GlobalException("费率规则不存在");
        }
        if (dto.getStatus() != null && dto.getStatus() == 1) {
            List<CategoryServiceFeeRuleCategory> cats = ruleCategoryMapper.selectList(
                    new LambdaQueryWrapper<CategoryServiceFeeRuleCategory>()
                            .eq(CategoryServiceFeeRuleCategory::getRuleId, dto.getId())
                            .eq(CategoryServiceFeeRuleCategory::getDeleted, 0));
            List<CategoryServiceFeeRuleTarget> tgts = ruleTargetMapper.selectList(
                    new LambdaQueryWrapper<CategoryServiceFeeRuleTarget>()
                            .eq(CategoryServiceFeeRuleTarget::getRuleId, dto.getId())
                            .eq(CategoryServiceFeeRuleTarget::getDeleted, 0));
            CategoryFeeRuleConflictCheckDTO chk = new CategoryFeeRuleConflictCheckDTO();
            chk.setRuleId(dto.getId());
            chk.setTargetType(rule.getTargetType());
            chk.setScopeType(rule.getScopeType());
            chk.setCategoryIds(cats.stream().map(CategoryServiceFeeRuleCategory::getCategoryId).collect(Collectors.toList()));
            chk.setTargetIds(tgts.stream().map(CategoryServiceFeeRuleTarget::getTargetId).collect(Collectors.toList()));
            assertNoConflict(chk);
        }
        CategoryServiceFeeRule upd = new CategoryServiceFeeRule();
        upd.setId(dto.getId());
        upd.setStatus(dto.getStatus());
        upd.setModifyTime(LocalDateTime.now());
        ruleMapper.updateById(upd);
        categoryFeeRuleMatchService.evictCacheByTargetType(rule.getTargetType());
        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<CategoryServiceFeeRule> rules = ruleMapper.selectList(new LambdaQueryWrapper<CategoryServiceFeeRule>()
                .in(CategoryServiceFeeRule::getId, ids));
        Set<Integer> targetTypes = rules.stream().map(CategoryServiceFeeRule::getTargetType).collect(Collectors.toSet());
        for (Integer rid : ids) {
            ruleCategoryMapper.delete(new LambdaQueryWrapper<CategoryServiceFeeRuleCategory>()
                    .eq(CategoryServiceFeeRuleCategory::getRuleId, rid));
            ruleTargetMapper.delete(new LambdaQueryWrapper<CategoryServiceFeeRuleTarget>()
                    .eq(CategoryServiceFeeRuleTarget::getRuleId, rid));
        }
        for (Integer rid : ids) {
            CategoryServiceFeeRule soft = new CategoryServiceFeeRule();
            soft.setId(rid);
            soft.setDeleted(1);
            soft.setModifyTime(LocalDateTime.now());
            ruleMapper.updateById(soft);
        }
        for (Integer tt : targetTypes) {
            categoryFeeRuleMatchService.evictCacheByTargetType(tt);
        }
    }

    @Override
    public CategoryFeeRuleConflictVO checkConflict(CategoryFeeRuleConflictCheckDTO dto) {
        CategoryFeeRuleConflictVO vo = new CategoryFeeRuleConflictVO();
        vo.setHasConflict(false);
        vo.setConflicts(new ArrayList<>());
        List<ConflictMatch> matches = findConflictingRules(dto);
        for (ConflictMatch m : matches) {
            for (CategoryServiceFeeRuleCategory cat : m.overlappingCategories) {
                CategoryFeeRuleConflictVO.ConflictItem item = new CategoryFeeRuleConflictVO.ConflictItem();
                item.setCategoryId(cat.getCategoryId());
                item.setCategoryName(cat.getCategoryName());
                item.setExistingRuleId(m.rule.getId());
                item.setExistingRuleName(m.rule.getRuleName());
                vo.getConflicts().add(item);
            }
        }
        vo.setHasConflict(!vo.getConflicts().isEmpty());
        return vo;
    }

    private void validateSaveDto(CategoryFeeRuleSaveDTO dto) {
        if (dto.getCategoryIds() == null || dto.getCategoryIds().isEmpty()) {
            throw new GlobalException("关联类目不能为空");
        }
        if (RuleScopeTypeEnum.SPECIFIED.getCode() == dto.getScopeType()
                && (dto.getTargetIds() == null || dto.getTargetIds().isEmpty())) {
            throw new GlobalException("指定范围时关联对象不能为空");
        }
        if (RuleScopeTypeEnum.EXCLUDE.getCode() == dto.getScopeType()
                && (dto.getTargetIds() == null || dto.getTargetIds().isEmpty())) {
            throw new GlobalException("不包含范围时排除对象不能为空");
        }
    }

    private boolean existsByRuleName(int targetType, String ruleName, Integer excludeId) {
        LambdaQueryWrapper<CategoryServiceFeeRule> w = new LambdaQueryWrapper<CategoryServiceFeeRule>()
                .eq(CategoryServiceFeeRule::getTargetType, targetType)
                .eq(CategoryServiceFeeRule::getRuleName, ruleName)
                .eq(CategoryServiceFeeRule::getDeleted, 0);
        if (excludeId != null) {
            w.ne(CategoryServiceFeeRule::getId, excludeId);
        }
        return ruleMapper.selectCount(w) > 0;
    }

    private CategoryFeeRuleConflictCheckDTO toConflictDto(Integer ruleId, CategoryFeeRuleSaveDTO dto) {
        CategoryFeeRuleConflictCheckDTO chk = new CategoryFeeRuleConflictCheckDTO();
        chk.setRuleId(ruleId);
        chk.setTargetType(dto.getTargetType());
        chk.setScopeType(dto.getScopeType());
        chk.setCategoryIds(dto.getCategoryIds());
        chk.setTargetIds(dto.getTargetIds());
        return chk;
    }

    /**
     * 冲突即报错：命中冲突时抛 GlobalException，文案中列出冲突规则名（最多 5 条，超出用"等共 N 条"收尾）。
     * 行为变更见 CR-20260514-141100-fee-rule-conflict-reject。
     */
    private void assertNoConflict(CategoryFeeRuleConflictCheckDTO dto) {
        List<ConflictMatch> matches = findConflictingRules(dto);
        if (matches.isEmpty()) {
            return;
        }
        List<String> distinctNames = matches.stream()
                .map(m -> m.rule.getRuleName())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        int total = distinctNames.size();
        String preview = distinctNames.stream().limit(MAX_CONFLICT_NAMES_IN_MESSAGE).collect(Collectors.joining("、"));
        String suffix = total > MAX_CONFLICT_NAMES_IN_MESSAGE ? "等共" + total + "条" : "";
        log.info("品类费率冲突，拒绝保存。冲突规则={}", distinctNames);
        throw new GlobalException("存在冲突规则，无法保存：" + preview + suffix);
    }

    private static final int MAX_CONFLICT_NAMES_IN_MESSAGE = 5;

    private List<ConflictMatch> findConflictingRules(CategoryFeeRuleConflictCheckDTO dto) {
        List<ConflictMatch> matches = new ArrayList<>();
        List<CategoryServiceFeeRule> candidates = ruleMapper.selectList(new LambdaQueryWrapper<CategoryServiceFeeRule>()
                .eq(CategoryServiceFeeRule::getTargetType, dto.getTargetType())
                .eq(CategoryServiceFeeRule::getStatus, 1)
                .eq(CategoryServiceFeeRule::getDeleted, 0)
                .ne(dto.getRuleId() != null, CategoryServiceFeeRule::getId, dto.getRuleId()));
        if (candidates.isEmpty()) {
            return matches;
        }
        List<Integer> ruleIds = candidates.stream().map(CategoryServiceFeeRule::getId).collect(Collectors.toList());
        List<CategoryServiceFeeRuleCategory> existingCategories = ruleCategoryMapper.selectList(
                new LambdaQueryWrapper<CategoryServiceFeeRuleCategory>()
                        .in(CategoryServiceFeeRuleCategory::getRuleId, ruleIds)
                        .eq(CategoryServiceFeeRuleCategory::getDeleted, 0));
        Map<Integer, List<CategoryServiceFeeRuleCategory>> categoriesByRule = existingCategories.stream()
                .collect(Collectors.groupingBy(CategoryServiceFeeRuleCategory::getRuleId));
        List<CategoryServiceFeeRuleTarget> existingTargets = ruleTargetMapper.selectList(
                new LambdaQueryWrapper<CategoryServiceFeeRuleTarget>()
                        .in(CategoryServiceFeeRuleTarget::getRuleId, ruleIds)
                        .eq(CategoryServiceFeeRuleTarget::getDeleted, 0));
        Map<Integer, List<CategoryServiceFeeRuleTarget>> targetsByRule = existingTargets.stream()
                .collect(Collectors.groupingBy(CategoryServiceFeeRuleTarget::getRuleId));

        Set<Integer> inputCategoryIds = new HashSet<>(dto.getCategoryIds());
        Set<Integer> inputTargetIds = dto.getTargetIds() != null ? new HashSet<>(dto.getTargetIds()) : Collections.emptySet();
        int inputScopeType = dto.getScopeType();

        for (CategoryServiceFeeRule existingRule : candidates) {
            List<CategoryServiceFeeRuleCategory> ruleCats = categoriesByRule.getOrDefault(existingRule.getId(), Collections.emptyList());
            if (ruleCats.isEmpty()) {
                continue;
            }
            List<CategoryServiceFeeRuleCategory> overlapping = ruleCats.stream()
                    .filter(c -> inputCategoryIds.contains(c.getCategoryId()))
                    .collect(Collectors.toList());
            if (overlapping.isEmpty()) {
                continue;
            }
            Set<Integer> existingTargetIds = targetsByRule.getOrDefault(existingRule.getId(), Collections.emptyList())
                    .stream().map(CategoryServiceFeeRuleTarget::getTargetId).collect(Collectors.toSet());
            if (!hasTargetOverlap(inputScopeType, inputTargetIds, existingRule.getScopeType(), existingTargetIds)) {
                continue;
            }
            matches.add(new ConflictMatch(existingRule, overlapping));
        }
        return matches;
    }

    private boolean hasTargetOverlap(int scopeA, Set<Integer> targetsA, int scopeB, Set<Integer> targetsB) {
        boolean aAll = RuleScopeTypeEnum.ALL.getCode() == scopeA;
        boolean bAll = RuleScopeTypeEnum.ALL.getCode() == scopeB;
        if (aAll || bAll) {
            return true;
        }
        boolean aSpecified = RuleScopeTypeEnum.SPECIFIED.getCode() == scopeA;
        boolean bSpecified = RuleScopeTypeEnum.SPECIFIED.getCode() == scopeB;
        if (aSpecified && bSpecified) {
            return targetsA.stream().anyMatch(targetsB::contains);
        }
        boolean aExclude = RuleScopeTypeEnum.EXCLUDE.getCode() == scopeA;
        boolean bExclude = RuleScopeTypeEnum.EXCLUDE.getCode() == scopeB;
        if (aExclude && bExclude) {
            return true;
        }
        if (aSpecified && bExclude) {
            return targetsA.stream().anyMatch(id -> !targetsB.contains(id));
        }
        if (aExclude && bSpecified) {
            return targetsB.stream().anyMatch(id -> !targetsA.contains(id));
        }
        return false;
    }

    private void saveCategories(int ruleId, List<Integer> categoryIds) {
        Map<Integer, Category> byId = loadCategoryClosure(categoryIds);
        for (Integer categoryId : categoryIds) {
            Category c = byId.get(categoryId);
            String name = c != null && c.getCategoryName() != null ? c.getCategoryName() : "";
            String path = buildPath(categoryId, byId);
            CategoryServiceFeeRuleCategory row = new CategoryServiceFeeRuleCategory();
            row.setRuleId(ruleId);
            row.setCategoryId(categoryId);
            row.setCategoryName(name);
            row.setCategoryPath(path);
            row.setDeleted(0);
            row.setCreateTime(LocalDateTime.now());
            row.setModifyTime(LocalDateTime.now());
            ruleCategoryMapper.insert(row);
        }
    }

    private Map<Integer, Category> loadCategoryClosure(List<Integer> categoryIds) {
        Set<Integer> need = new HashSet<>(categoryIds);
        List<Integer> frontier = new ArrayList<>(categoryIds);
        while (!frontier.isEmpty()) {
            List<Category> batch = categoryMapper.selectBatchIds(frontier);
            frontier.clear();
            for (Category c : batch) {
                if (c.getParentId() != null && c.getParentId() > 0 && need.add(c.getParentId())) {
                    frontier.add(c.getParentId());
                }
            }
        }
        if (need.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Category> all = categoryMapper.selectBatchIds(new ArrayList<>(need));
        Map<Integer, Category> map = new HashMap<>();
        for (Category c : all) {
            map.put(c.getCategoryId(), c);
        }
        return map;
    }

    private String buildPath(int categoryId, Map<Integer, Category> byId) {
        List<String> names = new ArrayList<>();
        Integer cur = categoryId;
        int guard = 0;
        while (cur != null && cur > 0 && guard++ < 32) {
            Category c = byId.get(cur);
            if (c == null) {
                break;
            }
            names.add(0, c.getCategoryName() != null ? c.getCategoryName() : "");
            cur = c.getParentId();
        }
        return String.join("/", names);
    }

    private void saveTargets(int ruleId, List<Integer> targetIds) {
        for (Integer targetId : targetIds) {
            CategoryServiceFeeRuleTarget t = new CategoryServiceFeeRuleTarget();
            t.setRuleId(ruleId);
            t.setTargetId(targetId);
            t.setDeleted(0);
            t.setCreateTime(LocalDateTime.now());
            t.setModifyTime(LocalDateTime.now());
            ruleTargetMapper.insert(t);
        }
    }

    private static class ConflictMatch {
        final CategoryServiceFeeRule rule;
        final List<CategoryServiceFeeRuleCategory> overlappingCategories;

        ConflictMatch(CategoryServiceFeeRule rule, List<CategoryServiceFeeRuleCategory> overlappingCategories) {
            this.rule = rule;
            this.overlappingCategories = overlappingCategories;
        }
    }
}
