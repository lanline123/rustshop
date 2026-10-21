// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.wholesale;

import com.tigshop.bean.model.wholesale.ProductWholesaleSkuConfig;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SKU 级批发配置 Mapper
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Mapper
public interface ProductWholesaleSkuConfigMapper extends BaseMapper<ProductWholesaleSkuConfig> {

    /**
     * 列出某商品全部未删除的 SKU 配置。
     *
     * @param productId product.product_id
     * @return SKU 配置列表
     */
    List<ProductWholesaleSkuConfig> listByProductId(@Param("productId") Integer productId);

    /**
     * 批量取某批商品的全部未删除 SKU 倍数配置（CR-20260613-0943 / 调整 CR-20260615-0911）。
     * <p>用于 C 端列表/卡片批量回填 {@code wholesaleMultiple}，1 次 IN 查询规避 N+1。
     * <p>CR-20260615-0911：原仅取 {@code sku_id=0} SPU 兜底行，但「有真实 SKU 行」的商品
     * （含单规格）倍数按真实 sku_id 录入，致列表读不到。改为取全部未删配置行，由
     * {@code resolveSpuMultipleMap} 按「sku_id=0 优先 → 否则 min(multiple>1)」聚合。
     *
     * @param productIds product.product_id 列表（调用方保证非空、去重）
     * @return is_delete=0 的全部 SKU 配置（每商品可能多条）
     */
    List<ProductWholesaleSkuConfig> selectSpuConfigByProductIds(@Param("productIds") List<Integer> productIds);

    /**
     * 物理删除某批商品的全部 SKU 级批发配置（绕开 {@code @TableLogic} 软删改写）。
     * <p>策略依据：表上 {@code uk_product_sku} 唯一索引不区分 {@code is_delete}；
     * config 表批量删除时同步级联清理（避免重新添加批发能力时残留过期 SKU 配置）。
     *
     * @param productIds product.product_id 列表（调用方保证非空）
     * @return 物理删除行数
     */
    int hardDeleteByProductIds(@Param("productIds") List<Integer> productIds);
}
