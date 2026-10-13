package com.tigshop.service.wholesale.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tigshop.bean.model.wholesale.ProductWholesaleSkuConfig;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.mapper.wholesale.ProductWholesaleSkuConfigMapper;
import com.tigshop.service.wholesale.WholesaleSkuConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SKU 级批发配置 Service 实现。
 *
 * <p>设计依据：{@code .ai-os/specs/wholesaler-design.md §3 product_wholesale_sku_config}
 * （CR-20260604-0937 简化为仅承载 {@code multiple}）。保存采用 upsert（保留未传字段）。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Service
@RequiredArgsConstructor
public class WholesaleSkuConfigServiceImpl implements WholesaleSkuConfigService {

    private static final int DEFAULT_MULTIPLE = 1;

    private final ProductWholesaleSkuConfigMapper skuConfigMapper;

    @Override
    public List<ProductWholesaleSkuConfig> listByProductId(Integer productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        // W0 自定义方法：已含 is_delete = 0 过滤
        return skuConfigMapper.listByProductId(productId);
    }

    @Override
    public Map<String, ProductWholesaleSkuConfig> mapByProductSkuKeys(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // is_delete 由 @TableLogic 自动过滤
        LambdaQueryWrapper<ProductWholesaleSkuConfig> qw = new LambdaQueryWrapper<>();
        qw.in(ProductWholesaleSkuConfig::getProductId, productIds);
        List<ProductWholesaleSkuConfig> list = skuConfigMapper.selectList(qw);

        Map<String, ProductWholesaleSkuConfig> map = new LinkedHashMap<>(list.size() * 2);
        for (ProductWholesaleSkuConfig sc : list) {
            map.put(buildKey(sc.getProductId(), sc.getSkuId()), sc);
        }
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(Integer productId, Integer skuId, Integer multiple) {
        if (productId == null || productId <= 0) {
            throw new GlobalException("productId 必填", HttpStatus.BAD_REQUEST);
        }
        if (skuId == null || skuId < 0) {
            // skuId=0 = SPU 兜底（单规格 / 全 SKU），与批发价 SKU_SPU_FALLBACK 语义对齐
            throw new GlobalException("skuId 不能为负数", HttpStatus.BAD_REQUEST);
        }
        if (multiple != null && multiple < 1) {
            throw new GlobalException("multiple 必须 ≥ 1", HttpStatus.BAD_REQUEST);
        }

        long now = Instant.now().getEpochSecond();
        // is_delete 由 @TableLogic 自动过滤
        LambdaQueryWrapper<ProductWholesaleSkuConfig> qw = new LambdaQueryWrapper<>();
        qw.eq(ProductWholesaleSkuConfig::getProductId, productId)
                .eq(ProductWholesaleSkuConfig::getSkuId, skuId)
                .last("limit 1");
        ProductWholesaleSkuConfig existing = skuConfigMapper.selectOne(qw);

        if (existing == null) {
            ProductWholesaleSkuConfig entity = new ProductWholesaleSkuConfig();
            entity.setProductId(productId);
            entity.setSkuId(skuId);
            entity.setMultiple(multiple != null ? multiple : DEFAULT_MULTIPLE);
            entity.setIsDelete(0);
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            skuConfigMapper.insert(entity);
        } else {
            if (multiple != null) {
                existing.setMultiple(multiple);
            }
            existing.setUpdateTime(now);
            skuConfigMapper.updateById(existing);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpsertMultiple(Integer productId, List<int[]> skuMultiples) {
        if (skuMultiples == null || skuMultiples.isEmpty()) {
            return;
        }
        for (int[] row : skuMultiples) {
            if (row == null || row.length < 2) {
                throw new GlobalException("skuMultiples 行格式应为 [skuId, multiple]", HttpStatus.BAD_REQUEST);
            }
            upsert(productId, row[0], row[1]);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByProductIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }
        LambdaUpdateWrapper<ProductWholesaleSkuConfig> uw = new LambdaUpdateWrapper<>();
        uw.in(ProductWholesaleSkuConfig::getProductId, productIds)
                .set(ProductWholesaleSkuConfig::getIsDelete, 1)
                .set(ProductWholesaleSkuConfig::getUpdateTime, Instant.now().getEpochSecond());
        return skuConfigMapper.update(null, uw);
    }

    private String buildKey(Integer productId, Integer skuId) {
        return productId + ":" + skuId;
    }
}
