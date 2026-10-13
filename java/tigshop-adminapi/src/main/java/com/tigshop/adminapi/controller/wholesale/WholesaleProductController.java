package com.tigshop.adminapi.controller.wholesale;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.wholesale.WholesaleBatchCommonDTO;
import com.tigshop.bean.dto.wholesale.WholesaleBatchGradeDTO;
import com.tigshop.bean.dto.wholesale.WholesaleBuyRuleBatchSaveDTO;
import com.tigshop.bean.dto.wholesale.WholesaleBuyRuleSaveDTO;
import com.tigshop.bean.dto.wholesale.WholesaleMultipleSaveDTO;
import com.tigshop.bean.dto.wholesale.WholesaleProductAddDTO;
import com.tigshop.bean.dto.wholesale.WholesaleProductBatchAddDTO;
import com.tigshop.bean.dto.wholesale.WholesalePriceSaveDTO;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.bean.model.wholesale.ProductWholesalePrice;
import com.tigshop.bean.model.wholesale.ProductWholesaleSkuConfig;
import com.tigshop.bean.model.wholesale.WholesalerGrade;
import com.tigshop.bean.query.wholesale.WholesaleProductPageQuery;
import com.tigshop.bean.query.wholesale.WholesaleSelectableProductPageQuery;
import com.tigshop.bean.vo.wholesale.WholesalePriceDetailVO;
import com.tigshop.bean.vo.wholesale.WholesalePriceRowVO;
import com.tigshop.bean.vo.wholesale.WholesaleProductBatchAddVO;
import com.tigshop.bean.vo.wholesale.WholesaleProductBuyRuleVO;
import com.tigshop.bean.vo.wholesale.WholesaleProductVO;
import com.tigshop.bean.vo.wholesale.WholesaleSkuPriceVO;
import com.tigshop.bean.vo.wholesale.WholesaleSkuRuleVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.wholesale.WholesalePriceService;
import com.tigshop.service.wholesale.WholesaleProductConfigService;
import com.tigshop.service.wholesale.WholesaleProductQueryService;
import com.tigshop.service.wholesale.WholesaleSkuConfigService;
import com.tigshop.service.wholesale.WholesalerGradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 批发商品管理（admin / 商家共用，CR-20260610-162600 多租户）。
 *
 * <p>接口契约见 api-contract §2.B1~B9，覆盖 TASK-WS-204（列表/添加/删除）、TASK-WS-205（批发价）、
 * TASK-WS-206（起批规则）、TASK-WS-207（倍数购买）。
 *
 * <p><b>多租户（CD-039）</b>：操作者身份取请求头（平台 admin=0 / 商家=自己 shopId）。列表/选品按
 * {@code shop_id} 自动隔离；写侧与详情读接口统一前置 {@code configService.requireOwnership}，
 * 跨店操作抛 5314。批发商管理/等级/设置仍为平台独有（D4），不在本控制器。
 *
 * <p>返回值直接为业务对象，由全局 ResponseAdvice 包装为 AjaxResult；异常通过
 * {@link com.tigshop.common.exception.GlobalException} 抛出并由全局处理器映射。
 *
 * <p>本控制器仅做参数绑定、轻量编排与读侧 VO 装配（{@code price/detail}）；写侧业务逻辑收敛在各
 * {@code WholesaleXxxService}，列表读模型聚合收敛在 {@link WholesaleProductQueryService}。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Tag(name = "批发商品管理")
@RestController
@RequestMapping("/adminapi/wholesale/product")
@RequiredArgsConstructor
@Validated
public class WholesaleProductController {

    /** multiSpecPriceMode 默认值：1=商品级混批（api-contract B2） */
    private static final int DEFAULT_MULTI_SPEC_PRICE_MODE = 1;
    /** moqDimension 默认值：1=SPU（api-contract B2） */
    private static final int DEFAULT_MOQ_DIMENSION = 1;
    /** grade_id=0：通用价 */
    private static final int GRADE_UNIVERSAL = 0;
    private static final String GRADE_UNIVERSAL_NAME = "通用价";

    private final WholesaleProductConfigService configService;
    private final WholesaleProductQueryService queryService;
    private final WholesalePriceService priceService;
    private final WholesaleSkuConfigService skuConfigService;
    private final WholesalerGradeService gradeService;
    private final ProductSkuService productSkuService;
    private final ProductService productService;

    /** sku_id=0：SPU 兜底（单规格 / 无 SKU 行商品的批发录入行，与批发价 SKU_SPU_FALLBACK 一致） */
    private static final int SKU_SPU_FALLBACK = 0;
    /** 单规格合成行展示名 */
    private static final String SINGLE_SPEC_SPEC_TEXT = "默认规格";

    // ====================== TASK-WS-204 列表 / 添加 / 删除 ======================

    @Operation(summary = "批发商品分页")
    @GetMapping("/page")
    public IPage<WholesaleProductVO> page(WholesaleProductPageQuery query) {
        return queryService.page(query);
    }

    @Operation(summary = "可选批发商品分页（当前操作者名下商品 + 剔除已配置批发；CR-20260603-1700 / CR-20260610 多租户）")
    @GetMapping("/selectable/page")
    public IPage<ProductListResDTO> selectablePage(WholesaleSelectableProductPageQuery query) {
        return queryService.selectablePage(query);
    }

    @Operation(summary = "添加批发商品（从商品库选择）")
    @PostMapping("/add")
    public Map<String, Integer> add(@Valid @RequestBody WholesaleProductAddDTO dto) {
        int specMode = dto.getMultiSpecPriceMode() != null
                ? dto.getMultiSpecPriceMode() : DEFAULT_MULTI_SPEC_PRICE_MODE;
        int dimension = dto.getMoqDimension() != null
                ? dto.getMoqDimension() : DEFAULT_MOQ_DIMENSION;
        // create 内部校验商品存在（5310）+ 未重复配置（5311）
        configService.create(dto.getProductId(), specMode, dimension);
        return Map.of("productId", dto.getProductId());
    }

    @Operation(summary = "批量添加批发商品（一次选多个普通商品转批发）")
    @PostMapping("/batchAdd")
    public WholesaleProductBatchAddVO batchAdd(@Valid @RequestBody WholesaleProductBatchAddDTO dto) {
        // 仅创建 config 默认配置；价格仍需逐商品 /price/save 录入（CD-023）；
        // 已存在 active config 的 productId 进入 skippedIds[]，单事务内执行（详见 Service Javadoc）
        return configService.batchAdd(dto.getProductIds());
    }

    @Operation(summary = "批量删除批发商品")
    @PostMapping("/batchDelete")
    public Map<String, Integer> batchDelete(@RequestBody Map<String, List<Integer>> body) {
        List<Integer> productIds = body != null ? body.get("productIds") : null;
        if (productIds == null || productIds.isEmpty()) {
            return Map.of("deletedCount", 0);
        }
        // 归属校验（CD-039）：price 行物理删在 config 软删之前执行，必须前置整批校验
        configService.requireOwnership(productIds);
        // 级联删除：先物理删 price 行（T-201 边界外、由本任务打通），再软删 config + sku_config
        priceService.deleteByProductIds(productIds);
        int deleted = configService.batchDelete(productIds);
        return Map.of("deletedCount", deleted);
    }

    // ====================== TASK-WS-205 批发价 ======================

    @Operation(summary = "批发价详情")
    @GetMapping("/price/detail")
    public WholesalePriceDetailVO priceDetail(@Parameter(description = "商品 ID") @RequestParam Integer productId) {
        configService.requireOwnership(productId);
        return buildPriceDetail(productId);
    }

    @Operation(summary = "批发价保存（单规格 + 多规格统一）")
    @PostMapping("/price/save")
    public void priceSave(@Valid @RequestBody WholesalePriceSaveDTO dto) {
        configService.requireOwnership(dto.getProductId());
        priceService.save(dto);
    }

    @Operation(summary = "批量设置通用售价")
    @PostMapping("/price/batchSetCommon")
    public Map<String, Integer> batchSetCommon(@Valid @RequestBody WholesaleBatchCommonDTO dto) {
        configService.requireOwnership(dto.getProductId());
        return Map.of("affectedRows", priceService.batchSetCommon(dto));
    }

    @Operation(summary = "批量设置类型售价")
    @PostMapping("/price/batchSetByGrade")
    public Map<String, Integer> batchSetByGrade(@Valid @RequestBody WholesaleBatchGradeDTO dto) {
        configService.requireOwnership(dto.getProductId());
        return Map.of("affectedRows", priceService.batchSetByGrade(dto));
    }

    // ====================== TASK-WS-206 起批规则 ======================

    @Operation(summary = "起批规则详情")
    @GetMapping("/buyRule/detail")
    public WholesaleProductBuyRuleVO buyRuleDetail(@Parameter(description = "商品 ID") @RequestParam Integer productId) {
        configService.requireOwnership(productId);
        return buildBuyRuleDetail(productId);
    }

    @Operation(summary = "起批量设置（CR-20260604-0937：统一字段 moq）")
    @PostMapping("/buyRule/save")
    public void buyRuleSave(@Valid @RequestBody WholesaleBuyRuleSaveDTO dto) {
        // admin UI 不论 SPU/SKU 维度只填一个 moq 数字，含义随 dimension 解释；
        // updateMoq 内部仅改 product_wholesale_config.dimension + moq，不动 sku_config
        configService.updateMoq(dto.getProductId(), dto.getDimension(), dto.getMoq());
    }

    @Operation(summary = "起批量批量设置（CR-20260604-1042；一次给 N 个 product 应用同一对 dimension + moq）")
    @PostMapping("/buyRule/batchSave")
    public Map<String, Integer> buyRuleBatchSave(@Valid @RequestBody WholesaleBuyRuleBatchSaveDTO dto) {
        int updated = configService.batchUpdateMoq(dto.getProductIds(), dto.getDimension(), dto.getMoq());
        return Map.of("updatedCount", updated);
    }

    // ====================== TASK-WS-207 倍数购买 ======================

    @Operation(summary = "倍数购买保存（按 SKU 批量）")
    @PostMapping("/multiple/save")
    public void multipleSave(@Valid @RequestBody WholesaleMultipleSaveDTO dto) {
        configService.requireOwnership(dto.getProductId());
        List<int[]> skuMultiples = dto.getSkuMultiples().stream()
                .filter(Objects::nonNull)
                .map(s -> new int[]{s.getSkuId(), s.getMultiple()})
                .collect(Collectors.toList());
        skuConfigService.batchUpsertMultiple(dto.getProductId(), skuMultiples);
    }

    // ====================== 读侧 VO 装配（私有，不污染 Service） ======================

    /**
     * 装配批发价详情：SKU 列表 × 每 SKU 的价格行。
     *
     * <p>价格行归属规则：以 sku_id 命中（{@code row.skuId == sku.skuId}）或 SPU 兜底
     * （{@code row.skuId == 0}，商品级混批 / 单规格）划入该 SKU，与 resolver 取价语义一致。
     */
    private WholesalePriceDetailVO buildPriceDetail(Integer productId) {
        ProductWholesaleConfig config = configService.getActiveByProductId(productId);
        if (config == null) {
            throw new GlobalException("商品不存在或未配置批发能力", HttpStatus.WHOLESALE_PRODUCT_NOT_FOUND);
        }
        List<ProductSkuDTO> skus = productSkuService.getProductSkusByPid(productId);
        List<ProductWholesalePrice> priceRows = priceService.listByProductId(productId);
        Map<Integer, String> gradeNameMap = resolveGradeNames(priceRows);

        WholesalePriceDetailVO vo = new WholesalePriceDetailVO();
        vo.setProductId(productId);
        vo.setMultiSpecPriceMode(config.getMultiSpecPriceMode());
        vo.setIsMultiSpec(skus != null && skus.size() > 1);
        vo.setPriceMode(priceRows.isEmpty() ? null : priceRows.get(0).getPriceMode());

        List<WholesaleSkuPriceVO> skuVos = new ArrayList<>();
        if (skus != null) {
            for (ProductSkuDTO sku : skus) {
                WholesaleSkuPriceVO skuVo = new WholesaleSkuPriceVO();
                skuVo.setSkuId(sku.getSkuId());
                skuVo.setSkuSpecText(sku.getSkuValue());
                skuVo.setRetailPrice(parsePrice(sku.getSkuPrice()));
                skuVo.setPriceRows(priceRows.stream()
                        .filter(r -> belongsToSku(r, sku.getSkuId()))
                        .map(r -> toPriceRowVO(r, gradeNameMap))
                        .sorted(Comparator
                                .comparing(WholesalePriceRowVO::getGradeId, Comparator.nullsFirst(Integer::compareTo))
                                .thenComparing(WholesalePriceRowVO::getTierMinQty, Comparator.nullsFirst(Integer::compareTo)))
                        .collect(Collectors.toList()));
                skuVos.add(skuVo);
            }
        }
        // 单规格 / 无 SKU 行商品：product_sku 无行 → 合成一条 SPU 兜底行（sku_id=0），
        // 承载已存在的 sku_id=0 批发价并供前端录入；与批发价保存 SKU_SPU_FALLBACK 口径一致
        if (skuVos.isEmpty()) {
            skuVos.add(buildSpuFallbackPriceRow(productId, priceRows, gradeNameMap));
        }
        vo.setSkus(skuVos);
        return vo;
    }

    /** 合成单规格 SPU 兜底价格行（sku_id=0）：零售价取 product.product_price，价格行取 sku_id=0 行。 */
    private WholesaleSkuPriceVO buildSpuFallbackPriceRow(Integer productId,
            List<ProductWholesalePrice> priceRows, Map<Integer, String> gradeNameMap) {
        WholesaleSkuPriceVO skuVo = new WholesaleSkuPriceVO();
        skuVo.setSkuId(SKU_SPU_FALLBACK);
        skuVo.setSkuSpecText(SINGLE_SPEC_SPEC_TEXT);
        Product product = productService.getById(productId);
        skuVo.setRetailPrice(product != null ? product.getProductPrice() : null);
        skuVo.setPriceRows(priceRows.stream()
                .filter(r -> belongsToSku(r, SKU_SPU_FALLBACK))
                .map(r -> toPriceRowVO(r, gradeNameMap))
                .sorted(Comparator
                        .comparing(WholesalePriceRowVO::getGradeId, Comparator.nullsFirst(Integer::compareTo))
                        .thenComparing(WholesalePriceRowVO::getTierMinQty, Comparator.nullsFirst(Integer::compareTo)))
                .collect(Collectors.toList()));
        return skuVo;
    }

    /**
     * 装配起批规则详情：商品级维度 / moq（统一字段） + SKU 级倍数列表 + 提示文案。
     * <p>CR-20260604-0937：起批数统一从 {@code config.moq} 读取；SKU 维度也共用同一 moq。
     */
    private WholesaleProductBuyRuleVO buildBuyRuleDetail(Integer productId) {
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
        List<WholesaleSkuRuleVO> skuRules = skuConfigs.stream()
                .map(sc -> {
                    WholesaleSkuRuleVO r = new WholesaleSkuRuleVO();
                    r.setSkuId(sc.getSkuId());
                    r.setMultiple(sc.getMultiple());
                    return r;
                })
                .collect(Collectors.toList());
        // 单规格 / 无 SKU 行商品：product_sku 无行 → 保证至少一条 sku_id=0 行供前端录入倍数
        // （与 price/detail 同口径；已配置 sku_id=0 时不重复合成）
        List<ProductSkuDTO> skuList = productSkuService.getProductSkusByPid(productId);
        boolean noSkuRows = skuList == null || skuList.isEmpty();
        boolean hasSpuRule = skuRules.stream()
                .anyMatch(r -> r.getSkuId() != null && r.getSkuId() == SKU_SPU_FALLBACK);
        if (noSkuRows && !hasSpuRule) {
            WholesaleSkuRuleVO spuRule = new WholesaleSkuRuleVO();
            spuRule.setSkuId(SKU_SPU_FALLBACK);
            spuRule.setMultiple(null);
            skuRules.add(spuRule);
        }
        vo.setSkuRules(skuRules);
        vo.setHint(buildHint(config, skuRules));
        return vo;
    }

    private String buildHint(ProductWholesaleConfig config, List<WholesaleSkuRuleVO> skuRules) {
        boolean spu = config.getMoqDimension() != null && config.getMoqDimension() == DEFAULT_MOQ_DIMENSION;
        StringBuilder sb = new StringBuilder();
        int moq = config.getMoq() != null ? config.getMoq() : 1;
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

    private boolean belongsToSku(ProductWholesalePrice row, Integer skuId) {
        if (row.getSkuId() == null) {
            return false;
        }
        return row.getSkuId() == 0 || row.getSkuId().equals(skuId);
    }

    private WholesalePriceRowVO toPriceRowVO(ProductWholesalePrice row, Map<Integer, String> gradeNameMap) {
        WholesalePriceRowVO vo = new WholesalePriceRowVO();
        vo.setGradeId(row.getGradeId());
        vo.setGradeName(row.getGradeId() != null && row.getGradeId() == GRADE_UNIVERSAL
                ? GRADE_UNIVERSAL_NAME
                : gradeNameMap.getOrDefault(row.getGradeId(), null));
        vo.setTierMinQty(row.getTierMinQty());
        vo.setPriceMode(row.getPriceMode());
        vo.setDiscount(row.getDiscount());
        vo.setWholesalePrice(row.getWholesalePrice());
        return vo;
    }

    private Map<Integer, String> resolveGradeNames(List<ProductWholesalePrice> priceRows) {
        List<Integer> gradeIds = priceRows.stream()
                .map(ProductWholesalePrice::getGradeId)
                .filter(g -> g != null && g > 0)
                .distinct()
                .collect(Collectors.toList());
        if (gradeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, WholesalerGrade> map = gradeService.mapByIds(gradeIds);
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getGradeName(), (a, b) -> a));
    }

    private BigDecimal parsePrice(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
