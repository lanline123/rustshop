// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.product;

import java.util.List;

/**
 * 产品ES同步服务
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
public interface ProductEsSyncService {

    /**
     * 同步单个产品到ES（异步）
     *
     * @param productId 产品ID
     */
    void syncProductAsync(Long productId);

    /**
     * 批量同步产品到ES（异步）
     *
     * @param productIds 产品ID列表
     */
    void batchSyncProductsAsync(List<Long> productIds);

    /**
     * 从ES删除产品（异步）
     *
     * @param productId 产品ID
     */
    void deleteProductAsync(Long productId);
}
