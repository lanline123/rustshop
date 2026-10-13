package com.tigshop.service.wholesale.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.dto.wholesale.WholesaleBatchCommonDTO;
import com.tigshop.bean.dto.wholesale.WholesaleBatchGradeDTO;
import com.tigshop.bean.dto.wholesale.WholesaleBatchTierDTO;
import com.tigshop.bean.dto.wholesale.WholesaleGradeRowDTO;
import com.tigshop.bean.dto.wholesale.WholesalePriceRowDTO;
import com.tigshop.bean.dto.wholesale.WholesalePriceSaveDTO;
import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.bean.model.wholesale.ProductWholesalePrice;
import com.tigshop.bean.model.wholesale.ProductWholesaleSkuConfig;
import com.tigshop.bean.model.wholesale.WholesalerGrade;
import com.tigshop.bean.vo.wholesale.WholesalePriceTierVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.wholesale.ProductWholesaleConfigMapper;
import com.tigshop.mapper.wholesale.ProductWholesalePriceMapper;
import com.tigshop.mapper.wholesale.ProductWholesaleSkuConfigMapper;
import com.tigshop.service.wholesale.WholesalePriceService;
import com.tigshop.service.wholesale.WholesaleProductConfigService;
import com.tigshop.service.wholesale.WholesalerGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 三维批发价 Service 实现（{@code product_wholesale_price}）。
 *
 * <p>设计依据：{@code .ai-os/specs/wholesaler-tasks-w2-steps.md § TASK-WS-202}、
 * {@code wholesaler-api-contract.md §2.B B4-B7}、{@code wholesaler-design.md §3/§4}。
 *
 * <p><b>全量替换 + 物理删（W0 设计）</b>：
 * {@code product_wholesale_price} 的唯一键 {@code uk_combo} 含 {@code is_delete}
 * （{@code product_id, sku_id, grade_id, tier_min_qty, is_delete}），且 model 上有
 * {@code @TableLogic}。若用 {@code LambdaQueryWrapper.delete} 会被改写为软删，
 * 重复保存/批量时旧软删行（{@code is_delete=1}）与新软删行碰撞唯一键导致异常。
 * 因此写操作一律走 mapper 的物理删 {@link ProductWholesalePriceMapper#deleteByProductId}
 * 配合「读-合并-整表替换」实现分维度覆盖（见 {@link #batchSetCommon} / {@link #batchSetByGrade}）。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Service
@RequiredArgsConstructor
public class WholesalePriceServiceImpl implements WholesalePriceService {

    /** price_mode：1=折扣换算 */
    private static final int PRICE_MODE_DISCOUNT = 1;
    /** price_mode：2=直接金额 */
    private static final int PRICE_MODE_FIXED = 2;
    /** 单次保存最大行数（api-contract B5） */
    private static final int MAX_ROWS = 1000;
    /** 同 (sku,grade) 阶梯段上限（C14） */
    private static final int MAX_TIERS_PER_GROUP = 20;
    /** 折扣下界（十分制，CD-015 / C13） */
    private static final BigDecimal DISCOUNT_MIN = new BigDecimal("0.10");
    /** 折扣上界（十分制，CD-015 / C13） */
    private static final BigDecimal DISCOUNT_MAX = new BigDecimal("9.99");
    /** grade_id=0：通用价 */
    private static final int GRADE_UNIVERSAL = 0;
    /** sku_id=0：SPU 兜底（resolver 中匹配全部 SKU） */
    private static final int SKU_SPU_FALLBACK = 0;

    private final ProductWholesalePriceMapper priceMapper;
    private final WholesaleProductConfigService productConfigService;
    private final WholesalerGradeService gradeService;
    private final ProductWholesaleConfigMapper wholesaleConfigMapper;
    private final ProductWholesaleSkuConfigMapper wholesaleSkuConfigMapper;

    @Override
    public List<ProductWholesalePrice> listByProductId(Integer productId) {
        if (productId == null || productId <= 0) {
            return Collections.emptyList();
        }
        List<ProductWholesalePrice> list = priceMapper.listByProductId(productId);
        return list != null ? list : Collections.emptyList();
    }

    @Override
    public Map<Integer, List<ProductWholesalePrice>> mapByProductIdsAndGrade(
            List<Integer> productIds, Integer gradeId) {
        // 空入参防御：不打 DB（购物车预取阶段非批发商或空列表都会走到此处）
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        int g = (gradeId == null || gradeId < 0) ? GRADE_UNIVERSAL : gradeId;
        // 1 次 IN 查询：status=1 启用 + grade_id ∈ {0 通用价, 当前等级}；is_delete 由 @TableLogic 过滤
        LambdaQueryWrapper<ProductWholesalePrice> qw = new LambdaQueryWrapper<>();
        qw.in(ProductWholesalePrice::getProductId, productIds)
                .eq(ProductWholesalePrice::getStatus, 1)
                .and(w -> w.eq(ProductWholesalePrice::getGradeId, GRADE_UNIVERSAL)
                        .or().eq(ProductWholesalePrice::getGradeId, g));
        List<ProductWholesalePrice> rows = priceMapper.selectList(qw);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        return rows.stream().collect(Collectors.groupingBy(ProductWholesalePrice::getProductId));
    }

    @Override
    public Set<Integer> listPricedProductIdsByGrade(Integer gradeId) {
        int g = (gradeId == null || gradeId < 0) ? GRADE_UNIVERSAL : gradeId;
        // 单次查询：status=1 启用 + grade_id ∈ {0 通用价, 当前等级}；is_delete 由 @TableLogic 过滤
        // 只取 product_id 列，去重后返回（供 C 端促销列表 notIn 过滤）
        LambdaQueryWrapper<ProductWholesalePrice> qw = new LambdaQueryWrapper<>();
        qw.select(ProductWholesalePrice::getProductId)
                .eq(ProductWholesalePrice::getStatus, 1)
                .and(w -> w.eq(ProductWholesalePrice::getGradeId, GRADE_UNIVERSAL)
                        .or().eq(ProductWholesalePrice::getGradeId, g));
        List<ProductWholesalePrice> rows = priceMapper.selectList(qw);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptySet();
        }
        return rows.stream()
                .map(ProductWholesalePrice::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasPricedProduct(Integer productId, Integer gradeId) {
        if (productId == null || productId <= 0) {
            return false;
        }
        int g = (gradeId == null || gradeId < 0) ? GRADE_UNIVERSAL : gradeId;
        // 单商品命中查询（LIMIT 1）：status=1 启用 + grade_id ∈ {0 通用价, 当前等级}；is_delete 由 @TableLogic 过滤
        // 口径与 listPricedProductIdsByGrade / mapByProductIdsAndGrade 一致（C 端详情页活动隐藏判定）
        LambdaQueryWrapper<ProductWholesalePrice> qw = new LambdaQueryWrapper<>();
        qw.eq(ProductWholesalePrice::getProductId, productId)
                .eq(ProductWholesalePrice::getStatus, 1)
                .and(w -> w.eq(ProductWholesalePrice::getGradeId, GRADE_UNIVERSAL)
                        .or().eq(ProductWholesalePrice::getGradeId, g))
                .last("limit 1");
        List<ProductWholesalePrice> rows = priceMapper.selectList(qw);
        return rows != null && !rows.isEmpty();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(WholesalePriceSaveDTO dto) {
        if (dto == null || dto.getProductId() == null || dto.getProductId() <= 0) {
            throw new GlobalException("productId 必填", HttpStatus.BAD_REQUEST);
        }
        Integer productId = dto.getProductId();
        // 前置：商品必须已配置批发能力（不做懒创建；新增配置走 WholesaleProductConfigService.create）
        if (!productConfigService.isConfigured(productId)) {
            throw new GlobalException("商品未配置批发能力", HttpStatus.WHOLESALE_PRODUCT_NOT_FOUND);
        }
        List<WholesalePriceRowDTO> priceRows = dto.getPriceRows();
        if (priceRows == null || priceRows.isEmpty()) {
            throw new GlobalException("priceRows 不能为空", HttpStatus.BAD_REQUEST);
        }
        if (priceRows.size() > MAX_ROWS) {
            throw new GlobalException("priceRows 不能超过 " + MAX_ROWS + " 条", HttpStatus.BAD_REQUEST);
        }

        long now = Instant.now().getEpochSecond();
        // 去重 key=sku|grade|tier；阶梯计数 key=sku|grade
        Set<String> uniqueKeys = new HashSet<>();
        Map<String, Integer> tierCountPerSkuGrade = new HashMap<>();
        List<ProductWholesalePrice> rows = new ArrayList<>(priceRows.size());

        int rowIdx = 0;
        for (WholesalePriceRowDTO r : priceRows) {
            rowIdx++;
            if (r == null) {
                errInvalid(rowIdx, "行数据不能为空");
            }
            if (r.getSkuId() == null || r.getSkuId() < 0) {
                errInvalid(rowIdx, "skuId 必须 ≥ 0");
            }
            if (r.getGradeId() == null || r.getGradeId() < 0) {
                errInvalid(rowIdx, "gradeId 必须 ≥ 0");
            }
            if (r.getTierMinQty() == null || r.getTierMinQty() < 1) {
                errInvalid(rowIdx, "tierMinQty 必须 ≥ 1");
            }
            int mode = (r.getPriceMode() != null
                    && (r.getPriceMode() == PRICE_MODE_DISCOUNT || r.getPriceMode() == PRICE_MODE_FIXED))
                    ? r.getPriceMode() : -1;
            if (mode == -1) {
                errInvalid(rowIdx, "priceMode 必须为 1 或 2");
            }
            if (mode == PRICE_MODE_DISCOUNT) {
                if (!isDiscountInRange(r.getDiscount())) {
                    errInvalid(rowIdx, "discount 必须在 [0.10, 9.99]");
                }
            } else if (r.getWholesalePrice() == null || r.getWholesalePrice().signum() <= 0) {
                errInvalid(rowIdx, "wholesalePrice 必须 > 0");
            }
            // gradeId>0 时校验等级启用中（内部抛 5303/5304）
            if (r.getGradeId() > 0) {
                gradeService.requireEnabled(r.getGradeId());
            }
            registerUniqueness(rowIdx, uniqueKeys, tierCountPerSkuGrade,
                    r.getSkuId(), r.getGradeId(), r.getTierMinQty());

            rows.add(toEntity(productId, r.getSkuId(), r.getGradeId(), r.getTierMinQty(),
                    mode, r.getDiscount(), r.getWholesalePrice(), now));
        }

        // 全量替换：物理删旧行 + 批量插入
        persistFullReplace(productId, rows);

        // 同步多规格价格模式（仅 DTO 显式传入时）
        if (dto.getMultiSpecPriceMode() != null) {
            productConfigService.updateMultiSpecMode(productId, dto.getMultiSpecPriceMode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSetCommon(WholesaleBatchCommonDTO dto) {
        if (dto == null || dto.getProductId() == null || dto.getProductId() <= 0) {
            throw new GlobalException("productId 必填", HttpStatus.BAD_REQUEST);
        }
        Integer productId = dto.getProductId();
        if (!productConfigService.isConfigured(productId)) {
            throw new GlobalException("商品未配置批发能力", HttpStatus.WHOLESALE_PRODUCT_NOT_FOUND);
        }
        int mode = requirePriceMode(dto.getPriceMode());
        List<Integer> targetSkus = resolveTargetSkus(dto.getSkuIds());
        boolean allSku = isEmpty(dto.getSkuIds());

        long now = Instant.now().getEpochSecond();
        List<ProductWholesalePrice> newRows =
                buildBatchRows(productId, targetSkus, GRADE_UNIVERSAL, mode, dto.getTiers(), now);

        // 读-合并：保留非「目标 SKU × 通用价」的行，其余被覆盖
        Set<Integer> targetSkuSet = new HashSet<>(targetSkus);
        List<ProductWholesalePrice> kept = listByProductId(productId).stream()
                .filter(p -> !(isUniversal(p) && (allSku || targetSkuSet.contains(p.getSkuId()))))
                .collect(Collectors.toList());

        persistFullReplace(productId, mergeRows(kept, newRows));
        return newRows.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSetByGrade(WholesaleBatchGradeDTO dto) {
        if (dto == null || dto.getProductId() == null || dto.getProductId() <= 0) {
            throw new GlobalException("productId 必填", HttpStatus.BAD_REQUEST);
        }
        Integer productId = dto.getProductId();
        if (!productConfigService.isConfigured(productId)) {
            throw new GlobalException("商品未配置批发能力", HttpStatus.WHOLESALE_PRODUCT_NOT_FOUND);
        }
        int mode = requirePriceMode(dto.getPriceMode());
        List<WholesaleGradeRowDTO> gradeRows = dto.getGradeRows();
        if (gradeRows == null || gradeRows.isEmpty()) {
            throw new GlobalException("gradeRows 不能为空", HttpStatus.BAD_REQUEST);
        }
        List<Integer> targetSkus = resolveTargetSkus(dto.getSkuIds());
        boolean allSku = isEmpty(dto.getSkuIds());

        long now = Instant.now().getEpochSecond();
        Set<Integer> targetGradeIds = new LinkedHashSet<>();
        List<ProductWholesalePrice> newRows = new ArrayList<>();
        for (WholesaleGradeRowDTO gr : gradeRows) {
            if (gr == null || gr.getGradeId() == null || gr.getGradeId() <= 0) {
                throw priceInvalid("gradeRows[].gradeId 必须 > 0");
            }
            if (!targetGradeIds.add(gr.getGradeId())) {
                throw priceInvalid("gradeRows 中 gradeId 重复：" + gr.getGradeId());
            }
            // 类型售价必须挂在启用中的等级上（内部抛 5303/5304）
            gradeService.requireEnabled(gr.getGradeId());
            newRows.addAll(buildBatchRows(productId, targetSkus, gr.getGradeId(), mode, gr.getTiers(), now));
        }

        // 读-合并：保留非「目标 SKU × 目标等级」的行，其余被覆盖
        Set<Integer> targetSkuSet = new HashSet<>(targetSkus);
        List<ProductWholesalePrice> kept = listByProductId(productId).stream()
                .filter(p -> !(p.getGradeId() != null && targetGradeIds.contains(p.getGradeId())
                        && (allSku || targetSkuSet.contains(p.getSkuId()))))
                .collect(Collectors.toList());

        persistFullReplace(productId, mergeRows(kept, newRows));
        return newRows.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByProductIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        // 物理删（与 save / batch 一致；商品下架/删除级联清 price 行）
        for (Integer pid : productIds) {
            if (pid != null && pid > 0) {
                priceMapper.deleteByProductId(pid);
            }
        }
    }

    // ====================== 私有辅助 ======================

    /**
     * 整表替换持久化：物理删该商品全部 price 行后批量插入 rows。
     * <p>save 与 batch 共用本低阶持久化，但 batch 不复用 public {@link #save}（语义不同）。
     */
    private void persistFullReplace(Integer productId, List<ProductWholesalePrice> rows) {
        priceMapper.deleteByProductId(productId);
        for (ProductWholesalePrice row : rows) {
            priceMapper.insert(row);
        }
    }

    /**
     * 构建批量阶梯行（同一组 tiers 应用到每个 sku × 给定 grade）。
     * <p>校验：minQty≥1、mode 对应值合法、组内 minQty 不重复、阶梯 ≤ 20 段。
     */
    private List<ProductWholesalePrice> buildBatchRows(Integer productId, List<Integer> skus,
            int gradeId, int mode, List<WholesaleBatchTierDTO> tiers, long now) {
        if (tiers == null || tiers.isEmpty()) {
            throw new GlobalException("tiers 不能为空", HttpStatus.BAD_REQUEST);
        }
        if (tiers.size() > MAX_TIERS_PER_GROUP) {
            throw priceInvalid("同 (sku,grade) 阶梯不能超过 " + MAX_TIERS_PER_GROUP + " 段");
        }
        Set<Integer> minQtySet = new HashSet<>();
        for (WholesaleBatchTierDTO t : tiers) {
            if (t == null || t.getMinQty() == null || t.getMinQty() < 1) {
                throw priceInvalid("阶梯起量 minQty 必须 ≥ 1");
            }
            if (mode == PRICE_MODE_DISCOUNT) {
                if (!isDiscountInRange(t.getDiscount())) {
                    throw priceInvalid("discount 必须在 [0.10, 9.99]");
                }
            } else if (t.getWholesalePrice() == null || t.getWholesalePrice().signum() <= 0) {
                throw priceInvalid("wholesalePrice 必须 > 0");
            }
            if (!minQtySet.add(t.getMinQty())) {
                throw priceInvalid("阶梯起量 minQty 重复：" + t.getMinQty());
            }
        }
        List<ProductWholesalePrice> rows = new ArrayList<>(skus.size() * tiers.size());
        for (Integer skuId : skus) {
            for (WholesaleBatchTierDTO t : tiers) {
                rows.add(toEntity(productId, skuId, gradeId, t.getMinQty(),
                        mode, t.getDiscount(), t.getWholesalePrice(), now));
            }
        }
        return rows;
    }

    private List<ProductWholesalePrice> mergeRows(List<ProductWholesalePrice> kept,
            List<ProductWholesalePrice> newRows) {
        List<ProductWholesalePrice> merged = new ArrayList<>(kept.size() + newRows.size());
        merged.addAll(kept);
        merged.addAll(newRows);
        return merged;
    }

    /**
     * 目标 SKU 解析：空 = 全 SKU，落到 SPU 兜底行 {@code sku_id=0}（resolver 匹配全部 SKU）。
     */
    private List<Integer> resolveTargetSkus(List<Integer> skuIds) {
        if (isEmpty(skuIds)) {
            return Collections.singletonList(SKU_SPU_FALLBACK);
        }
        LinkedHashSet<Integer> set = new LinkedHashSet<>();
        for (Integer s : skuIds) {
            if (s == null || s < 0) {
                throw priceInvalid("skuId 必须 ≥ 0");
            }
            set.add(s);
        }
        return new ArrayList<>(set);
    }

    private ProductWholesalePrice toEntity(Integer productId, Integer skuId, Integer gradeId,
            Integer tierMinQty, int mode, BigDecimal discount, BigDecimal wholesalePrice, long now) {
        ProductWholesalePrice p = new ProductWholesalePrice();
        p.setProductId(productId);
        p.setSkuId(skuId);
        p.setGradeId(gradeId);
        p.setTierMinQty(tierMinQty);
        p.setPriceMode(mode);
        p.setDiscount(discount != null ? discount : BigDecimal.ZERO);
        p.setWholesalePrice(wholesalePrice != null ? wholesalePrice : BigDecimal.ZERO);
        p.setStatus(1);
        p.setIsDelete(0);
        p.setCreateTime(now);
        p.setUpdateTime(now);
        return p;
    }

    private void registerUniqueness(int rowIdx, Set<String> uniqueKeys,
            Map<String, Integer> tierCountPerSkuGrade, Integer skuId, Integer gradeId, Integer tierMinQty) {
        String uk = skuId + "|" + gradeId + "|" + tierMinQty;
        if (!uniqueKeys.add(uk)) {
            errInvalid(rowIdx, "(sku,grade,tier) 组合重复");
        }
        String sgKey = skuId + "|" + gradeId;
        int cnt = tierCountPerSkuGrade.merge(sgKey, 1, Integer::sum);
        if (cnt > MAX_TIERS_PER_GROUP) {
            errInvalid(rowIdx, "同 (sku,grade) 阶梯不能超过 " + MAX_TIERS_PER_GROUP + " 段");
        }
    }

    private int requirePriceMode(Integer priceMode) {
        if (priceMode == null || (priceMode != PRICE_MODE_DISCOUNT && priceMode != PRICE_MODE_FIXED)) {
            throw priceInvalid("priceMode 必须为 1 或 2");
        }
        return priceMode;
    }

    private boolean isDiscountInRange(BigDecimal discount) {
        return discount != null
                && discount.compareTo(DISCOUNT_MIN) >= 0
                && discount.compareTo(DISCOUNT_MAX) <= 0;
    }

    private boolean isUniversal(ProductWholesalePrice p) {
        return p.getGradeId() != null && p.getGradeId() == GRADE_UNIVERSAL;
    }

    private boolean isEmpty(List<Integer> list) {
        return list == null || list.isEmpty();
    }

    private void errInvalid(int rowIdx, String reason) {
        throw new GlobalException(String.format("第 %d 行：%s", rowIdx, reason),
                HttpStatus.WHOLESALE_PRICE_INVALID);
    }

    private GlobalException priceInvalid(String reason) {
        return new GlobalException(reason, HttpStatus.WHOLESALE_PRICE_INVALID);
    }

    // ====================== C 端 backfill（TASK-WS-209 / WS-210） ======================

    @Override
    public Map<Integer, BigDecimal> resolveFirstWholesalePriceMap(
            List<Integer> productIds, Integer gradeId, Map<Integer, BigDecimal> retailPriceMap) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        if (gradeId == null || gradeId <= 0) {
            return Collections.emptyMap();
        }
        // CR-20260613-1629：单 SQL 批量预取「第一阶梯」批发价（起量最低阶梯，禁 N+1）。
        // 每商品最多返回 2 行（通用第一阶梯 + 当前等级第一阶梯），等级优先在此聚合。
        List<ProductWholesalePrice> firstRows =
                priceMapper.selectFirstPriceByProductIds(productIds, gradeId);
        if (firstRows == null || firstRows.isEmpty()) {
            return Collections.emptyMap();
        }
        // 等级优先：同商品多行里，优先保留 grade_id=当前等级，否则保留 grade_id=0（通用）
        Map<Integer, ProductWholesalePrice> chosenByProduct = new HashMap<>(firstRows.size() * 2);
        for (ProductWholesalePrice row : firstRows) {
            if (row == null || row.getProductId() == null) {
                continue;
            }
            Integer pid = row.getProductId();
            ProductWholesalePrice prev = chosenByProduct.get(pid);
            if (prev == null) {
                chosenByProduct.put(pid, row);
                continue;
            }
            boolean rowIsCurrentGrade = gradeId.equals(row.getGradeId());
            boolean prevIsCurrentGrade = gradeId.equals(prev.getGradeId());
            if (rowIsCurrentGrade && !prevIsCurrentGrade) {
                chosenByProduct.put(pid, row);
            }
        }
        Map<Integer, BigDecimal> result = new HashMap<>(chosenByProduct.size() * 2);
        for (ProductWholesalePrice row : chosenByProduct.values()) {
            BigDecimal retailPrice = retailPriceMap != null ? retailPriceMap.get(row.getProductId()) : null;
            BigDecimal finalPrice = computeFinalPrice(row, retailPrice);
            if (finalPrice != null) {
                result.put(row.getProductId(), finalPrice);
            }
        }
        return result;
    }

    /**
     * CR-20260603-1720 / CD-035（CR-20260604-0937 简化）：批量解析 C 端列表卡片
     * {@code wholesaleCardMoq}。
     *
     * <p>3 条命中规则（AND，缺一为 null）：
     * <ol>
     *   <li>调用方保证 productIds 是 firstPriceMap.keySet()（已通过批发命中过滤，规则 1）</li>
     *   <li>{@code product_wholesale_config} 存在且 {@code is_delete=0}（规则 2）</li>
     *   <li>{@code config.moq > 0}（统一字段，不再分 SPU/SKU 维度查询，规则 3）</li>
     * </ol>
     *
     * <p>SQL 成本：仅 1 次 IN（{@code wholesale_config}）；空入参不打 DB。
     *
     * <p>对比 CR-20260604-0937 前的实现：取消"SKU 数 ≤ 1"限制（多规格批发商品也能直接卡片加购），
     * 去掉 {@code product_sku COUNT} 与 {@code wholesale_sku_config} 两次额外 IN 查询。
     */
    @Override
    public Map<Integer, Integer> resolveCardMoqMap(List<Integer> productIds) {
        // CR-20260613-0943：委托 resolveCardConfigMap（同一份 config 查询），仅抽 moq 字段，语义不变
        Map<Integer, ProductWholesaleConfig> configMap = resolveCardConfigMap(productIds);
        if (configMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, Integer> result = new HashMap<>(configMap.size() * 2);
        for (Map.Entry<Integer, ProductWholesaleConfig> e : configMap.entrySet()) {
            result.put(e.getKey(), e.getValue().getMoq());
        }
        return result;
    }

    @Override
    public Map<Integer, ProductWholesaleConfig> resolveCardConfigMap(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 去重保护：避免 IN 列表重复 productId 造成无谓 SQL 体积
        List<Integer> uniqueIds = productIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // SQL #1: product_wholesale_config（is_delete=0）按 productIds 取 moq + moqDimension
        List<ProductWholesaleConfig> configs = wholesaleConfigMapper.selectByProductIds(uniqueIds);
        if (configs == null || configs.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, ProductWholesaleConfig> result = new HashMap<>(configs.size() * 2);
        for (ProductWholesaleConfig cfg : configs) {
            if (cfg == null || cfg.getProductId() == null) {
                continue;
            }
            Integer moq = cfg.getMoq();
            if (moq == null || moq <= 0) {
                continue; // moq=0/null 视为未配置有效起批数，前端按 null 跳详情
            }
            result.put(cfg.getProductId(), cfg);
        }
        return result;
    }

    @Override
    public Map<Integer, Integer> resolveSpuMultipleMap(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Integer> uniqueIds = productIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (uniqueIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // SQL: product_wholesale_sku_config（is_delete=0）批量取全部 SKU 倍数配置（每商品可能多条）
        List<ProductWholesaleSkuConfig> rows = wholesaleSkuConfigMapper.selectSpuConfigByProductIds(uniqueIds);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        // CR-20260615-0911：聚合口径「sku_id=0 优先 → 否则 multiple>1 的最小值」。
        // 原仅取 sku_id=0，但「有真实 SKU 行」的商品（含单规格）倍数按真实 sku_id 录入，致列表读不到；
        // 此口径与购物车 resolveSkuConfig（精确 SKU→兜底）及后台 buildHint（min multiple>1）一致。
        Map<Integer, Integer> spuFallback = new HashMap<>(rows.size() * 2); // productId → sku_id=0 的 multiple(>1)
        Map<Integer, Integer> minOther = new HashMap<>(rows.size() * 2);    // productId → 其余 SKU 的 min multiple(>1)
        for (ProductWholesaleSkuConfig row : rows) {
            if (row == null || row.getProductId() == null) {
                continue;
            }
            Integer multiple = row.getMultiple();
            if (multiple == null || multiple <= 1) {
                continue; // ≤1 视为无倍数约束
            }
            Integer pid = row.getProductId();
            if (row.getSkuId() != null && row.getSkuId() == SKU_SPU_FALLBACK) {
                spuFallback.put(pid, multiple);
            } else {
                Integer prev = minOther.get(pid);
                if (prev == null || multiple < prev) {
                    minOther.put(pid, multiple);
                }
            }
        }
        Map<Integer, Integer> result = new HashMap<>(uniqueIds.size() * 2);
        for (Integer pid : uniqueIds) {
            Integer chosen = spuFallback.get(pid);
            if (chosen == null) {
                chosen = minOther.get(pid);
            }
            if (chosen != null) {
                result.put(pid, chosen);
            }
        }
        return result;
    }

    @Override
    public List<WholesalePriceTierVO> buildWholesalePriceTierList(Integer productId, BigDecimal retailPrice) {
        return buildWholesalePriceTierList(productId, retailPrice, null);
    }

    @Override
    public List<WholesalePriceTierVO> buildWholesalePriceTierList(Integer productId, BigDecimal retailPrice,
            Map<Integer, BigDecimal> skuRetailPriceMap) {
        // gradeFilter=null → 全等级（保持 CD-022 历史语义，供 2/3 参重载与既有调用方使用）
        return buildTierListInternal(productId, retailPrice, skuRetailPriceMap, null);
    }

    @Override
    public List<WholesalePriceTierVO> buildWholesalePriceTierList(Integer productId, BigDecimal retailPrice,
            Map<Integer, BigDecimal> skuRetailPriceMap, Integer gradeId) {
        // CR-20260612-1659：按当前等级过滤；无有效等级（≤0）与成交价 resolver 同口径，返回空
        if (gradeId == null || gradeId <= 0) {
            return Collections.emptyList();
        }
        return buildTierListInternal(productId, retailPrice, skuRetailPriceMap, gradeId);
    }

    /**
     * 阶梯价列表构造核心：{@code gradeFilter==null} 返回全等级（CD-022）；
     * 非空时仅保留 {@code grade_id==0(通用)} 或 {@code grade_id==gradeFilter} 的行
     * （CR-20260612-1659，口径同 {@code WholesalePriceResolver.matchesGrade}）。
     */
    private List<WholesalePriceTierVO> buildTierListInternal(Integer productId, BigDecimal retailPrice,
            Map<Integer, BigDecimal> skuRetailPriceMap, Integer gradeFilter) {
        if (productId == null || productId <= 0) {
            return Collections.emptyList();
        }
        List<ProductWholesalePrice> rows = listByProductId(productId);
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        // status=1 过滤；按 id ASC 保持 admin 录入顺序。
        // gradeFilter 非空时额外按等级收窄（grade_id=0 通用 或 = 当前等级）。
        List<ProductWholesalePrice> sorted = rows.stream()
                .filter(p -> p.getStatus() == null || p.getStatus() == 1)
                .filter(p -> matchesGradeFilter(p.getGradeId(), gradeFilter))
                .sorted(Comparator.comparing(ProductWholesalePrice::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        if (sorted.isEmpty()) {
            return Collections.emptyList();
        }
        // 批量预取 gradeName（grade_id=0 → "通用"；其余调用 mapByIds）
        Map<Integer, WholesalerGrade> gradeMap = loadGradeNameMap(sorted);

        List<WholesalePriceTierVO> result = new ArrayList<>(sorted.size());
        for (ProductWholesalePrice row : sorted) {
            WholesalePriceTierVO vo = new WholesalePriceTierVO();
            vo.setPriceId(row.getId());
            vo.setTierMinQty(row.getTierMinQty());
            vo.setPriceMode(row.getPriceMode());
            vo.setDiscount(row.getDiscount());
            vo.setFixedPrice(row.getWholesalePrice());
            vo.setGradeId(row.getGradeId());
            vo.setSkuId(row.getSkuId());
            vo.setGradeName(resolveGradeName(row.getGradeId(), gradeMap));
            vo.setDisplayPrice(computeFinalPrice(row, resolveRowRetailPrice(row, retailPrice, skuRetailPriceMap)));
            result.add(vo);
        }
        return result;
    }

    /**
     * 等级过滤（CR-20260612-1659，口径同 {@code WholesalePriceResolver.matchesGrade}）：
     * {@code gradeFilter==null} → 不过滤（全等级）；否则保留 {@code rowGradeId==0/null(通用)}
     * 或 {@code rowGradeId==gradeFilter(当前等级)} 的行。
     */
    private static boolean matchesGradeFilter(Integer rowGradeId, Integer gradeFilter) {
        if (gradeFilter == null) {
            return true;
        }
        if (rowGradeId == null || rowGradeId == 0) {
            return true;
        }
        return rowGradeId.equals(gradeFilter);
    }

    /**
     * 解析单行折扣基数：行 {@code sku_id > 0} 且 {@code skuRetailPriceMap} 命中 → 用该 SKU 零售价；
     * 否则（SPU 兜底 {@code sku_id=0} 或映射未命中）回退商品级 {@code retailPrice}。
     *
     * <p>与下单算价 {@code WholesalePriceResolver}（折扣基数=命中行所属 SKU 零售价）对齐，
     * 避免多 SKU 商品详情把所有阶梯折扣价塌成同一商品级基数（最低 SKU 价）。
     */
    private BigDecimal resolveRowRetailPrice(ProductWholesalePrice row, BigDecimal retailPrice,
            Map<Integer, BigDecimal> skuRetailPriceMap) {
        if (row != null && skuRetailPriceMap != null
                && row.getSkuId() != null && row.getSkuId() > 0) {
            BigDecimal skuPrice = skuRetailPriceMap.get(row.getSkuId());
            if (skuPrice != null) {
                return skuPrice;
            }
        }
        return retailPrice;
    }

    /**
     * 计算单行最终价：折扣模式 = {@code retailPrice × discount / 10}（CD-015，HALF_UP 2 位）；
     * 固定模式 = {@code wholesale_price}。
     *
     * @return 计算后的最终价；折扣模式但 retailPrice/discount 缺失则返回 null
     */
    private BigDecimal computeFinalPrice(ProductWholesalePrice row, BigDecimal retailPrice) {
        if (row == null || row.getPriceMode() == null) {
            return null;
        }
        if (row.getPriceMode() == PRICE_MODE_FIXED) {
            return row.getWholesalePrice();
        }
        if (row.getPriceMode() == PRICE_MODE_DISCOUNT) {
            if (retailPrice == null || row.getDiscount() == null) {
                return null;
            }
            // CD-015：retailPrice × discount / 10（十分制折率），HALF_UP 2 位
            return retailPrice.multiply(row.getDiscount())
                    .divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    /**
     * 批量加载阶梯行涉及的等级名称（gradeId&gt;0 部分）；gradeId=0 走文案常量。
     */
    private Map<Integer, WholesalerGrade> loadGradeNameMap(List<ProductWholesalePrice> rows) {
        List<Integer> gradeIds = rows.stream()
                .map(ProductWholesalePrice::getGradeId)
                .filter(g -> g != null && g > 0)
                .distinct()
                .collect(Collectors.toList());
        if (gradeIds.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<Integer, WholesalerGrade> map = gradeService.mapByIds(gradeIds);
        return map != null ? map : new LinkedHashMap<>();
    }

    private String resolveGradeName(Integer gradeId, Map<Integer, WholesalerGrade> gradeMap) {
        if (gradeId == null || gradeId == GRADE_UNIVERSAL) {
            return "通用";
        }
        WholesalerGrade grade = gradeMap.get(gradeId);
        return grade != null ? grade.getGradeName() : null;
    }
}
