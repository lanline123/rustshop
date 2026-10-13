package com.tigshop.service.wholesale.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.wholesale.ProductWholesaleConfig;
import com.tigshop.bean.vo.wholesale.WholesaleProductBatchAddVO;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.mapper.wholesale.ProductWholesaleConfigMapper;
import com.tigshop.mapper.wholesale.ProductWholesaleSkuConfigMapper;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.wholesale.WholesaleProductConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品级批发配置 Service 实现。
 *
 * <p>设计依据：{@code .ai-os/specs/wholesaler-design.md §3 product_wholesale_config}
 * 与 {@code wholesaler-tasks-w2-steps.md § TASK-WS-201}。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Service
@RequiredArgsConstructor
public class WholesaleProductConfigServiceImpl implements WholesaleProductConfigService {

    private static final int MULTI_SPEC_MODE_SPU = 1;
    private static final int MULTI_SPEC_MODE_SKU = 2;
    private static final int MOQ_DIMENSION_SPU = 1;
    private static final int MOQ_DIMENSION_SKU = 2;
    private static final int DEFAULT_MOQ = 1;
    /** product_type=1：普通（实物）商品，仅此类型可配置批发 */
    private static final int PRODUCT_TYPE_NORMAL = 1;
    /** delivery_mode=1：单次交付，周期购（2）不支持配置批发 */
    private static final int DELIVERY_MODE_SINGLE = 1;

    private final ProductWholesaleConfigMapper configMapper;
    private final ProductWholesaleSkuConfigMapper skuConfigMapper;
    private final ProductService productService;

    @Override
    public boolean isConfigured(Integer productId) {
        return getActiveByProductId(productId) != null;
    }

    @Override
    public ProductWholesaleConfig getActiveByProductId(Integer productId) {
        if (productId == null) {
            return null;
        }
        // is_delete 由 @TableLogic 自动过滤；status=1 表示批发能力启用
        LambdaQueryWrapper<ProductWholesaleConfig> qw = new LambdaQueryWrapper<>();
        qw.eq(ProductWholesaleConfig::getProductId, productId)
                .eq(ProductWholesaleConfig::getStatus, 1)
                .last("limit 1");
        return configMapper.selectOne(qw);
    }

    @Override
    public Map<Integer, ProductWholesaleConfig> mapByProductIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<ProductWholesaleConfig> qw = new LambdaQueryWrapper<>();
        qw.in(ProductWholesaleConfig::getProductId, productIds)
                .eq(ProductWholesaleConfig::getStatus, 1);
        List<ProductWholesaleConfig> list = configMapper.selectList(qw);
        // 一个 product 至多一条有效配置（uniq_product_id），后者覆盖以防脏数据
        return list.stream().collect(Collectors.toMap(
                ProductWholesaleConfig::getProductId, Function.identity(), (a, b) -> b));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer create(Integer productId, Integer multiSpecPriceMode, Integer moqDimension) {
        if (productId == null || productId <= 0) {
            throw new GlobalException("productId 必填", HttpStatus.BAD_REQUEST);
        }
        // 商品必须存在（调既有 ProductService.getProductByIds 做存在性校验）
        List<Product> products = productService.getProductByIds(Collections.singletonList(productId));
        if (products == null || products.isEmpty()) {
            throw new GlobalException("商品不存在", HttpStatus.WHOLESALE_PRODUCT_NOT_FOUND);
        }
        // 归属校验（CR-20260610-162600 / CD-039 多租户）：操作者只能配置自己名下商品。
        // 平台 admin getShopId()=0 → 平台商品；商家 → 自己店铺商品；跨店一律 5314。
        // shopId 为 null 视为平台商品（0）。
        Product product = products.get(0);
        int productShopId = product.getShopId() == null ? 0 : product.getShopId();
        if (productShopId != currentOperatorShopId()) {
            throw new GlobalException("只能配置本店铺商品的批发能力", HttpStatus.WHOLESALE_NON_PLATFORM_PRODUCT);
        }
        // 商品类型限制（2026-06-11 用户决策）：仅普通（实物，product_type=1，null 视为普通）
        // 且非预售（sale_mode 为 0/null）商品可配置批发；其余类型 / 预售一律 5315
        if (product.getProductType() != null && product.getProductType() != PRODUCT_TYPE_NORMAL) {
            throw new GlobalException("仅支持普通实物商品配置批发", HttpStatus.WHOLESALE_PRODUCT_TYPE_NOT_SUPPORTED);
        }
        if (product.getSaleMode() != null && product.getSaleMode() > 0) {
            throw new GlobalException("预售商品不支持配置批发", HttpStatus.WHOLESALE_PRODUCT_TYPE_NOT_SUPPORTED);
        }
        if (product.getDeliveryMode() != null && product.getDeliveryMode() != DELIVERY_MODE_SINGLE) {
            throw new GlobalException("周期购商品不支持配置批发", HttpStatus.WHOLESALE_PRODUCT_TYPE_NOT_SUPPORTED);
        }
        int specMode = normalizeMultiSpecMode(multiSpecPriceMode);
        int dimension = normalizeMoqDimension(moqDimension);

        // 一个商品仅允许一条批发配置（uniq_product_id）；含已删/禁用一并判定避免唯一索引冲突
        if (existsAnyByProductId(productId)) {
            throw new GlobalException("该商品已配置批发能力", HttpStatus.WHOLESALE_PRODUCT_ALREADY_CONFIGURED);
        }

        long now = Instant.now().getEpochSecond();
        ProductWholesaleConfig entity = new ProductWholesaleConfig();
        entity.setProductId(productId);
        // 归属冗余落库（CD-039）：admin page / selectablePage 按 shop_id 过滤
        entity.setShopId(productShopId);
        entity.setMultiSpecPriceMode(specMode);
        entity.setMoqDimension(dimension);
        entity.setMoq(DEFAULT_MOQ);
        entity.setStatus(1);
        entity.setIsDelete(0);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        configMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMultiSpecMode(Integer productId, Integer mode) {
        int specMode = normalizeMultiSpecMode(mode);
        ProductWholesaleConfig config = requireConfig(productId);
        assertOwned(config);
        config.setMultiSpecPriceMode(specMode);
        config.setUpdateTime(Instant.now().getEpochSecond());
        configMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMoq(Integer productId, Integer dimension, Integer moq) {
        int dim = normalizeMoqDimension(dimension);
        ProductWholesaleConfig config = requireConfig(productId);
        assertOwned(config);
        config.setMoqDimension(dim);
        // CR-20260604-0937：moq 统一字段；含义随 dimension 解释（SPU=加总起批，SKU=每SKU起批）
        if (moq != null) {
            if (moq < 1) {
                throw new GlobalException("moq 必须 ≥ 1", HttpStatus.BAD_REQUEST);
            }
            config.setMoq(moq);
        }
        config.setUpdateTime(Instant.now().getEpochSecond());
        configMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateMoq(List<Integer> productIds, Integer dimension, Integer moq) {
        if (productIds == null || productIds.isEmpty()) {
            // 防御：Controller 层 @Valid 已拦截，此处兜底空入参直接返回 0（无 DB）
            return 0;
        }
        // 校验复用：与单接口 updateMoq 同源；dimension / moq 任一非法 → 整批 BAD_REQUEST
        int dim = normalizeMoqDimension(dimension);
        if (moq == null || moq < 1) {
            throw new GlobalException("moq 必须 ≥ 1", HttpStatus.BAD_REQUEST);
        }

        // 去重保序（与 batchAdd 一致；admin 误传重复 id 时不重复 update）
        List<Integer> distinctIds = new ArrayList<>(new LinkedHashSet<>(productIds));

        // 一次 IN 查现存 active configs（is_delete 由 @TableLogic 自动过滤）
        List<ProductWholesaleConfig> existing = configMapper.selectByProductIds(distinctIds);
        Map<Integer, ProductWholesaleConfig> existingMap = existing.stream()
                .collect(Collectors.toMap(ProductWholesaleConfig::getProductId, c -> c, (a, b) -> a));

        // 缺失检测：把缺失 ids 一次性反馈给 admin，避免"前几个 update 完才发现"的体验
        List<Integer> missing = distinctIds.stream()
                .filter(id -> !existingMap.containsKey(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new GlobalException("以下商品未配置批发能力：" + missing,
                    HttpStatus.WHOLESALE_PRODUCT_NOT_FOUND);
        }

        // 归属校验（CD-039）：任一行跨店 → 5314 整批回滚
        existing.forEach(this::assertOwned);

        long now = Instant.now().getEpochSecond();
        int updated = 0;
        for (Integer pid : distinctIds) {
            ProductWholesaleConfig c = existingMap.get(pid);
            c.setMoqDimension(dim);
            c.setMoq(moq);
            c.setUpdateTime(now);
            updated += configMapper.updateById(c);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }
        // 归属校验（CD-039）：物理删除前确认整批归属当前操作者（含 status=0 行）
        requireOwnership(productIds);
        // 物理删除策略（CD-033 / Bug-20260603-1640 修复）：
        // - 表上 uk_product_id / uk_product_sku 唯一索引不区分 is_delete，软删后再次添加会撞 1062；
        // - 与 product_wholesale_price 物理删策略一致（同坑），避免 admin 删除批发商品后重新添加报
        //   "数据类型异常"（DataIntegrityViolationException）；
        // - price 行物理级联由 Controller 调 WholesalePriceService.deleteByProductIds 完成，本 Service 不越界。
        skuConfigMapper.hardDeleteByProductIds(productIds);
        return configMapper.hardDeleteByProductIds(productIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WholesaleProductBatchAddVO batchAdd(List<Integer> productIds) {
        WholesaleProductBatchAddVO result = new WholesaleProductBatchAddVO();
        result.setSuccessCount(0);
        result.setSkippedIds(Collections.emptyList());
        result.setFailedItems(Collections.emptyList());
        if (productIds == null || productIds.isEmpty()) {
            // 防御：Controller 层 @Valid 已拦截，这里兜底空入参直接返回空结果
            return result;
        }

        // 去重保持顺序（admin 可能误传重复 id；第二次出现视为"已存在"跳过）
        List<Integer> distinctIds = new ArrayList<>(new LinkedHashSet<>(productIds));

        // 单次 IN 查询拉取已配置的 active config（CD-023 幂等语义按"active"判定；
        // 软删行的极端情况会在 create() 内部由 existsAnyByProductId 兜底，让整批回滚——
        // 与 add 单接口语义一致，不在批量入口偷偷放宽规则）
        Map<Integer, ProductWholesaleConfig> existing = mapByProductIds(distinctIds);

        List<Integer> skipped = new ArrayList<>();
        List<WholesaleProductBatchAddVO.FailedItem> failedItems = new ArrayList<>();
        int successCount = 0;
        for (Integer productId : distinctIds) {
            if (existing.containsKey(productId)) {
                skipped.add(productId);
                continue;
            }
            // 复用既有 create：内部校验商品存在（5310）+ 归属校验（5314，CD-039）+ 唯一索引兜底（5311）
            // 默认值与 admin 单加 /add 一致（multiSpecPriceMode=1 商品级混批 / moqDimension=1 SPU /
            // moqSpu=1），单 / 多 SKU 的差异由后续 admin 在 /price/save / /multiple/save 二次设置
            try {
                create(productId, MULTI_SPEC_MODE_SPU, MOQ_DIMENSION_SPU);
                successCount++;
            } catch (GlobalException e) {
                // 归属校验（CR-20260610-162600 / CD-039）：混选他人店铺商品逐项进 failedItems，
                // 不阻断整批；其余业务异常（5310 商品不存在 / 5311 已配置兜底）仍按整批回滚抛出。
                // 注：create 经 this. 自调用，@Transactional 不生效，此处 catch 不会触发整批回滚。
                if (e.getCode() == HttpStatus.WHOLESALE_NON_PLATFORM_PRODUCT
                        || e.getCode() == HttpStatus.WHOLESALE_PRODUCT_TYPE_NOT_SUPPORTED) {
                    // 跨店(5314) / 非普通或预售商品(5315)：逐项进 failedItems，不阻断整批
                    failedItems.add(new WholesaleProductBatchAddVO.FailedItem(
                            productId, e.getCode(), e.getMessage()));
                } else {
                    throw e;
                }
            }
        }

        result.setSuccessCount(successCount);
        result.setSkippedIds(skipped);
        result.setFailedItems(failedItems);
        return result;
    }

    @Override
    public void requireOwnership(Integer productId) {
        if (productId == null) {
            return;
        }
        requireOwnership(Collections.singletonList(productId));
    }

    @Override
    public void requireOwnership(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        List<Integer> distinctIds = productIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (distinctIds.isEmpty()) {
            return;
        }
        // 不复用 mapByProductIds（其过滤 status=1）：归属校验须覆盖已禁用行，
        // 否则跨店操作者可对 status=0 的他店配置执行删除等写操作。is_delete 由 @TableLogic 过滤。
        LambdaQueryWrapper<ProductWholesaleConfig> qw = new LambdaQueryWrapper<>();
        qw.in(ProductWholesaleConfig::getProductId, distinctIds);
        List<ProductWholesaleConfig> rows = configMapper.selectList(qw);
        if (rows == null || rows.isEmpty()) {
            // 无配置行 → 归属无从谈起，交由业务方法自身的 5310 处理
            return;
        }
        rows.forEach(this::assertOwned);
    }

    /**
     * 当前操作者 shopId（CD-039）：平台 admin=0，商家=自己 shopId。
     *
     * <p>无请求头上下文（单测 / 内部调用）按 0（平台）处理 —— 与项目先例一致
     * （{@code ProductController} 对 {@code getShopId()==null} 不注入过滤条件）。
     */
    private static int currentOperatorShopId() {
        Integer shopId = HeaderUtils.getShopId();
        return shopId == null ? 0 : shopId;
    }

    /** 单行归属断言：config.shop_id 与当前操作者不一致 → 5314（CD-039 改语义后复用原错误码）。 */
    private void assertOwned(ProductWholesaleConfig config) {
        if (config == null) {
            return;
        }
        int owner = config.getShopId() == null ? 0 : config.getShopId();
        if (owner != currentOperatorShopId()) {
            throw new GlobalException("只能配置本店铺商品的批发能力", HttpStatus.WHOLESALE_NON_PLATFORM_PRODUCT);
        }
    }

    private boolean existsAnyByProductId(Integer productId) {
        // 含已软删行的存在性计数（Bug-20260603-1640 latent 修复）：
        // LambdaQueryWrapper 配合 ProductWholesaleConfig#isDelete 上的 @TableLogic 会自动追加
        // is_delete = 0，导致软删行查不到、INSERT 时撞 uk_product_id 唯一索引（1062）。
        // 用 mapper 层 native SQL 显式绕开，与 batchDelete 物理删策略形成双向防护
        // （即使未来手动迁移数据或外部脚本写入 is_delete=1，仍能正确判定）。
        return configMapper.countAnyByProductId(productId) > 0;
    }

    private ProductWholesaleConfig requireConfig(Integer productId) {
        if (productId == null) {
            throw new GlobalException("productId 必填", HttpStatus.BAD_REQUEST);
        }
        // is_delete 由 @TableLogic 自动过滤
        LambdaQueryWrapper<ProductWholesaleConfig> qw = new LambdaQueryWrapper<>();
        qw.eq(ProductWholesaleConfig::getProductId, productId).last("limit 1");
        ProductWholesaleConfig config = configMapper.selectOne(qw);
        if (config == null) {
            throw new GlobalException("商品未配置批发能力", HttpStatus.WHOLESALE_PRODUCT_NOT_FOUND);
        }
        return config;
    }

    private int normalizeMultiSpecMode(Integer mode) {
        if (mode == null || (mode != MULTI_SPEC_MODE_SPU && mode != MULTI_SPEC_MODE_SKU)) {
            throw new GlobalException("multiSpecPriceMode 必须为 1 或 2", HttpStatus.BAD_REQUEST);
        }
        return mode;
    }

    private int normalizeMoqDimension(Integer dimension) {
        if (dimension == null || (dimension != MOQ_DIMENSION_SPU && dimension != MOQ_DIMENSION_SKU)) {
            throw new GlobalException("moqDimension 必须为 1 或 2", HttpStatus.BAD_REQUEST);
        }
        return dimension;
    }
}
