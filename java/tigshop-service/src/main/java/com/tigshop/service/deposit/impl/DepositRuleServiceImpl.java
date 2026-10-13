package com.tigshop.service.deposit.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositRuleDTO;
import com.tigshop.bean.dto.deposit.DepositRuleListDTO;
import com.tigshop.bean.dto.deposit.DepositRuleStatusDTO;
import com.tigshop.bean.enums.deposit.DepositRuleShopType;
import com.tigshop.bean.enums.merchant.MerchantType;
import com.tigshop.bean.model.deposit.ShopDepositRule;
import com.tigshop.bean.model.deposit.ShopDepositRuleCategory;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.vo.deposit.DepositRuleVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.deposit.ShopDepositRuleCategoryMapper;
import com.tigshop.mapper.deposit.ShopDepositRuleMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.deposit.DepositLifecycleService;
import com.tigshop.service.deposit.DepositRuleService;
import com.tigshop.service.product.CategoryService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 店铺保证金规则服务实现。
 *
 * @author Tigshop团队
 */
@Service
@RequiredArgsConstructor
public class DepositRuleServiceImpl extends BaseServiceImpl<ShopDepositRuleMapper, ShopDepositRule>
        implements DepositRuleService {

    private final ShopDepositRuleCategoryMapper ruleCategoryMapper;
    private final CategoryService categoryService;

    /** 规则调整后对现有账户对账并启动补缴缓冲；@Lazy 避免潜在循环依赖，字段注入不入 @RequiredArgsConstructor。 */
    @Resource
    @Lazy
    private DepositLifecycleService depositLifecycleService;

    @Override
    public Page<DepositRuleVO> list(DepositRuleListDTO listDTO) {
        Page<ShopDepositRule> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        LambdaQueryWrapper<ShopDepositRule> wrapper = Wrappers.<ShopDepositRule>lambdaQuery()
                .eq(ShopDepositRule::getIsDelete, 0)
                .like(StrUtil.isNotBlank(listDTO.getRuleName()), ShopDepositRule::getRuleName, listDTO.getRuleName())
                .eq(listDTO.getStatus() != null, ShopDepositRule::getStatus, listDTO.getStatus())
                // 生效时间区间筛选：返回与查询窗口 [startTime, endTime] 有交集的规则（endTime=0 表示长期有效=+∞）
                // 交集条件：rule.endTime >= filter.startTime（或 rule.endTime=0）且 rule.startTime <= filter.endTime
                .and(listDTO.getStartTime() != null, w -> w.eq(ShopDepositRule::getEndTime, 0L)
                        .or().ge(ShopDepositRule::getEndTime, listDTO.getStartTime()))
                .le(listDTO.getEndTime() != null, ShopDepositRule::getStartTime, listDTO.getEndTime())
                .orderByDesc(ShopDepositRule::getShopDepositRuleId);

        Page<ShopDepositRule> rulePage = this.page(page, wrapper);
        List<ShopDepositRule> records = rulePage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageUtil.convertPage(rulePage, new ArrayList<>());
        }

        // 批量加载类目关联，避免 N+1
        List<Integer> ruleIds = records.stream().map(ShopDepositRule::getShopDepositRuleId).toList();
        Map<Integer, List<Integer>> ruleCategoryMap = loadRuleCategoryMap(ruleIds);
        Map<Integer, String> categoryNameMap = loadCategoryNameMap(ruleCategoryMap.values().stream()
                .flatMap(List::stream).collect(Collectors.toSet()));

        List<DepositRuleVO> voList = records.stream()
                .map(rule -> toVO(rule, ruleCategoryMap.getOrDefault(rule.getShopDepositRuleId(), List.of()), categoryNameMap))
                .toList();
        return PageUtil.convertPage(rulePage, voList);
    }

    @Override
    public DepositRuleVO detail(Integer id) {
        ShopDepositRule rule = this.getById(id);
        if (rule == null || (rule.getIsDelete() != null && rule.getIsDelete() == 1)) {
            throw new GlobalException("规则不存在");
        }
        List<Integer> categoryIds = listCategoryIds(id);
        Map<Integer, String> categoryNameMap = loadCategoryNameMap(new HashSet<>(categoryIds));
        return toVO(rule, categoryIds, categoryNameMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DepositRuleDTO dto) {
        validateBase(dto);
        Set<Integer> merchantTypes = new LinkedHashSet<>(dto.getMerchantTypes());
        Set<Integer> shopTypes = new LinkedHashSet<>(dto.getShopTypes());
        Set<Integer> categoryIds = new LinkedHashSet<>(dto.getCategoryIds());
        checkTimeOverlap(null, merchantTypes, shopTypes, categoryIds, dto.getStartTime(), dto.getEndTime());

        long now = nowSeconds();
        ShopDepositRule rule = new ShopDepositRule();
        rule.setRuleName(dto.getRuleName());
        rule.setMerchantTypes(joinCodes(merchantTypes));
        rule.setShopTypes(joinCodes(shopTypes));
        rule.setDepositAmount(dto.getDepositAmount());
        rule.setStartTime(dto.getStartTime());
        rule.setEndTime(dto.getEndTime() == null ? 0L : dto.getEndTime());
        rule.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        rule.setIsDelete(0);
        rule.setAddTime(now);
        rule.setUpdateTime(now);
        this.save(rule);

        saveCategories(rule.getShopDepositRuleId(), categoryIds);
        // 规则新建可能上调应缴：提交后对现有账户对账并启动补缴缓冲
        reconcileAccountsAfterCommit();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DepositRuleDTO dto) {
        if (dto.getShopDepositRuleId() == null) {
            throw new GlobalException("规则ID不能为空");
        }
        ShopDepositRule exist = this.getById(dto.getShopDepositRuleId());
        if (exist == null || (exist.getIsDelete() != null && exist.getIsDelete() == 1)) {
            throw new GlobalException("规则不存在");
        }
        validateBase(dto);
        Set<Integer> merchantTypes = new LinkedHashSet<>(dto.getMerchantTypes());
        Set<Integer> shopTypes = new LinkedHashSet<>(dto.getShopTypes());
        Set<Integer> categoryIds = new LinkedHashSet<>(dto.getCategoryIds());
        checkTimeOverlap(dto.getShopDepositRuleId(), merchantTypes, shopTypes, categoryIds,
                dto.getStartTime(), dto.getEndTime());

        exist.setRuleName(dto.getRuleName());
        exist.setMerchantTypes(joinCodes(merchantTypes));
        exist.setShopTypes(joinCodes(shopTypes));
        exist.setDepositAmount(dto.getDepositAmount());
        exist.setStartTime(dto.getStartTime());
        exist.setEndTime(dto.getEndTime() == null ? 0L : dto.getEndTime());
        if (dto.getStatus() != null) {
            exist.setStatus(dto.getStatus());
        }
        exist.setUpdateTime(nowSeconds());
        this.updateById(exist);

        // 类目关联先删后插
        ruleCategoryMapper.delete(Wrappers.<ShopDepositRuleCategory>lambdaQuery()
                .eq(ShopDepositRuleCategory::getRuleId, exist.getShopDepositRuleId()));
        saveCategories(exist.getShopDepositRuleId(), categoryIds);
        // 规则修改可能上调应缴：提交后对现有账户对账并启动补缴缓冲
        reconcileAccountsAfterCommit();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(Integer id) {
        ShopDepositRule rule = this.getById(id);
        if (rule == null || (rule.getIsDelete() != null && rule.getIsDelete() == 1)) {
            throw new GlobalException("规则不存在");
        }
        rule.setIsDelete(1);
        rule.setUpdateTime(nowSeconds());
        return this.updateById(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(DepositRuleStatusDTO dto) {
        ShopDepositRule rule = this.getById(dto.getShopDepositRuleId());
        if (rule == null || (rule.getIsDelete() != null && rule.getIsDelete() == 1)) {
            throw new GlobalException("规则不存在");
        }
        rule.setStatus(dto.getStatus());
        rule.setUpdateTime(nowSeconds());
        this.updateById(rule);
        // 启用/停用规则可能改变应缴口径：提交后对现有账户对账（仅上调时启动补缴缓冲，reconcile 自守卫）
        reconcileAccountsAfterCommit();
    }

    // ---------------------------------------------------------------------
    // 内部方法
    // ---------------------------------------------------------------------

    /**
     * 规则变更提交后，触发对现有保证金账户的应缴对账（刷新快照 + 写调整记录 + 上调时启动补缴缓冲）。
     *
     * <p>在事务提交后执行（afterCommit），避免与规则写入同事务长时间持有账户行锁；对账本身幂等、非阻断，
     * 失败仅告警不影响规则保存。无活动事务时（理论上不会）直接同步执行。</p>
     */
    private void reconcileAccountsAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    depositLifecycleService.onDepositRuleChange();
                }
            });
        } else {
            depositLifecycleService.onDepositRuleChange();
        }
    }

    /**
     * 时间重叠校验：同一(主体类型 × 店铺类型 × 类目)维度下，生效时间区间不可与已有启用规则重叠。
     *
     * <p>先用时间窗口 + 启用 + 未删除粗筛候选规则（排除自身），再逐个判断三维度是否同时存在交集；
     * 三维度（主体/店型/类目）任一无交集即视为不同维度，不算冲突。</p>
     */
    private void checkTimeOverlap(Integer selfId, Set<Integer> merchantTypes, Set<Integer> shopTypes,
                                  Set<Integer> categoryIds, Long startTime, Long endTime) {
        // 区间重叠：existing.start <= newEnd && existing.end >= newStart；end=0 表示长期有效(+∞)
        long newStart = startTime == null ? 0L : startTime;
        long newEnd = endTime == null ? 0L : endTime;
        List<ShopDepositRule> candidates = this.list(Wrappers.<ShopDepositRule>lambdaQuery()
                .eq(ShopDepositRule::getIsDelete, 0)
                .eq(ShopDepositRule::getStatus, 1)
                .ne(selfId != null, ShopDepositRule::getShopDepositRuleId, selfId)
                // 新规则长期有效(newEnd=0)时不限制候选起点；否则候选起点 <= 新结束
                .le(newEnd != 0L, ShopDepositRule::getStartTime, newEnd)
                // 候选长期有效(end=0) 或 候选结束 >= 新起点
                .and(w -> w.eq(ShopDepositRule::getEndTime, 0L)
                        .or().ge(ShopDepositRule::getEndTime, newStart)));
        if (CollUtil.isEmpty(candidates)) {
            return;
        }
        List<Integer> candidateIds = candidates.stream().map(ShopDepositRule::getShopDepositRuleId).toList();
        Map<Integer, List<Integer>> candidateCategoryMap = loadRuleCategoryMap(candidateIds);

        for (ShopDepositRule candidate : candidates) {
            Set<Integer> candMerchantTypes = parseCodes(candidate.getMerchantTypes());
            Set<Integer> candShopTypes = parseCodes(candidate.getShopTypes());
            Set<Integer> candCategoryIds = new HashSet<>(
                    candidateCategoryMap.getOrDefault(candidate.getShopDepositRuleId(), List.of()));
            if (intersects(merchantTypes, candMerchantTypes)
                    && intersects(shopTypes, candShopTypes)
                    && intersects(categoryIds, candCategoryIds)) {
                throw new GlobalException("保证金规则同维度生效时间重叠", HttpStatus.DEPOSIT_RULE_TIME_OVERLAP);
            }
        }
    }

    private void validateBase(DepositRuleDTO dto) {
        if (StrUtil.isBlank(dto.getRuleName())) {
            throw new GlobalException("规则名称不能为空");
        }
        if (CollUtil.isEmpty(dto.getMerchantTypes())) {
            throw new GlobalException("适用主体类型不能为空");
        }
        if (CollUtil.isEmpty(dto.getShopTypes())) {
            throw new GlobalException("适用店铺类型不能为空");
        }
        if (CollUtil.isEmpty(dto.getCategoryIds())) {
            throw new GlobalException("适用类目不能为空");
        }
        if (dto.getDepositAmount() == null || dto.getDepositAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new GlobalException("保证金金额非法", HttpStatus.DEPOSIT_AMOUNT_INVALID);
        }
        if (dto.getStartTime() == null) {
            throw new GlobalException("生效开始时间不能为空");
        }
        // endTime=0 或 null 表示长期有效（与商品款式 end_time=0 约定一致），此时不校验 start<=end
        Long endTime = dto.getEndTime();
        if (endTime != null && endTime != 0L && dto.getStartTime() > endTime) {
            throw new GlobalException("生效开始时间不能晚于结束时间");
        }
    }

    private void saveCategories(Integer ruleId, Set<Integer> categoryIds) {
        for (Integer categoryId : categoryIds) {
            if (categoryId == null) {
                continue;
            }
            ShopDepositRuleCategory rc = new ShopDepositRuleCategory();
            rc.setRuleId(ruleId);
            rc.setCategoryId(categoryId);
            ruleCategoryMapper.insert(rc);
        }
    }

    private List<Integer> listCategoryIds(Integer ruleId) {
        return ruleCategoryMapper.selectList(Wrappers.<ShopDepositRuleCategory>lambdaQuery()
                        .eq(ShopDepositRuleCategory::getRuleId, ruleId))
                .stream()
                .map(ShopDepositRuleCategory::getCategoryId)
                .toList();
    }

    private Map<Integer, List<Integer>> loadRuleCategoryMap(List<Integer> ruleIds) {
        if (CollUtil.isEmpty(ruleIds)) {
            return Map.of();
        }
        return ruleCategoryMapper.selectList(Wrappers.<ShopDepositRuleCategory>lambdaQuery()
                        .in(ShopDepositRuleCategory::getRuleId, ruleIds))
                .stream()
                .collect(Collectors.groupingBy(ShopDepositRuleCategory::getRuleId,
                        Collectors.mapping(ShopDepositRuleCategory::getCategoryId, Collectors.toList())));
    }

    private Map<Integer, String> loadCategoryNameMap(Set<Integer> categoryIds) {
        if (CollUtil.isEmpty(categoryIds)) {
            return Map.of();
        }
        List<Category> categories = categoryService.getCategoryByIds(new ArrayList<>(categoryIds));
        if (CollUtil.isEmpty(categories)) {
            return Map.of();
        }
        return categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName, (a, b) -> a));
    }

    private DepositRuleVO toVO(ShopDepositRule rule, List<Integer> categoryIds, Map<Integer, String> categoryNameMap) {
        DepositRuleVO vo = new DepositRuleVO();
        vo.setShopDepositRuleId(rule.getShopDepositRuleId());
        vo.setRuleName(rule.getRuleName());
        vo.setDepositAmount(rule.getDepositAmount());
        vo.setStartTime(rule.getStartTime());
        vo.setEndTime(rule.getEndTime());
        vo.setStartTimeText(rule.getStartTime() == null ? "" : TigUtils.handelTime(rule.getStartTime()));
        vo.setEndTimeText(rule.getEndTime() == null || rule.getEndTime() == 0L
                ? "长期有效" : TigUtils.handelTime(rule.getEndTime()));
        vo.setStatus(rule.getStatus());
        vo.setStatusText(rule.getStatus() != null && rule.getStatus() == 1 ? "启用" : "停用");
        vo.setAddTime(rule.getAddTime() == null ? "" : TigUtils.handelTime(rule.getAddTime()));

        Set<Integer> merchantTypes = parseCodes(rule.getMerchantTypes());
        Set<Integer> shopTypes = parseCodes(rule.getShopTypes());
        vo.setMerchantTypes(new ArrayList<>(merchantTypes));
        vo.setShopTypes(new ArrayList<>(shopTypes));
        vo.setMerchantTypeText(merchantTypes.stream().map(MerchantType::getTypeName).collect(Collectors.joining("、")));
        vo.setShopTypeText(shopTypes.stream().map(DepositRuleShopType::getDescByCode).collect(Collectors.joining("、")));

        vo.setCategoryIds(categoryIds);
        vo.setCategoryNames(categoryIds.stream()
                .map(id -> categoryNameMap.getOrDefault(id, ""))
                .filter(StrUtil::isNotBlank)
                .toList());
        return vo;
    }

    private static boolean intersects(Set<Integer> a, Set<Integer> b) {
        if (CollUtil.isEmpty(a) || CollUtil.isEmpty(b)) {
            return false;
        }
        for (Integer item : a) {
            if (b.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private static String joinCodes(Set<Integer> codes) {
        return codes.stream().filter(java.util.Objects::nonNull)
                .map(String::valueOf).collect(Collectors.joining(","));
    }

    private static Set<Integer> parseCodes(String raw) {
        if (StrUtil.isBlank(raw)) {
            return new LinkedHashSet<>();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }
}
