package com.tigshop.service.wholesale;

import com.tigshop.bean.model.wholesale.ProductWholesaleSkuConfig;

import java.util.List;
import java.util.Map;

/**
 * SKU 级批发配置 Service（{@code product_wholesale_sku_config}：倍数购买）。
 *
 * <p>CR-20260604-0937：删除 {@code moqSku} 字段后，本 Service 仅承载 {@code multiple}
 * （倍数购买仍按 SKU 独立配置）。起批数 {@code moq} 由
 * {@link WholesaleProductConfigService#updateMoq} 统一在商品级配置维护。
 *
 * <p>保存采用 upsert 语义（按 {@code (product_id, sku_id)} 唯一键），只更新传入字段，
 * 保留其它字段不变。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
public interface WholesaleSkuConfigService {

    /** 取该商品下所有有效 SKU 配置。 */
    List<ProductWholesaleSkuConfig> listByProductId(Integer productId);

    /**
     * 批量取（购物车 B2 壳预加载）。
     *
     * @return key 格式 {@code "{productId}:{skuId}"}
     */
    Map<String, ProductWholesaleSkuConfig> mapByProductSkuKeys(List<Integer> productIds);

    /**
     * 保存单 SKU 倍数配置（upsert）。
     *
     * @param multiple 倍数购买（{@code null} 表示不更新；新建时默认 1）
     */
    void upsert(Integer productId, Integer skuId, Integer multiple);

    /**
     * 批量保存 SKU 倍数。
     *
     * @param skuMultiples 每项为 {@code [skuId, multiple]}
     */
    void batchUpsertMultiple(Integer productId, List<int[]> skuMultiples);

    /**
     * 删除某商品下所有 SKU 配置（删除批发商品时级联）。
     *
     * @return 软删行数
     */
    int deleteByProductIds(List<Integer> productIds);
}
