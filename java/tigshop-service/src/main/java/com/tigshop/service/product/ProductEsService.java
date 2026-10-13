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

import com.tigshop.common.utils.ApiResponse;

import java.util.List;
import java.util.Map;

/**
 * 产品ES服务接口
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
public interface ProductEsService {

    /**
     * 初始化产品索引
     *
     * @return 操作结果
     */
    Map<String, Object> initIndex();

    /**
     * 同步单个产品到ES
     *
     * @param productId 产品ID
     * @return 操作结果
     */
    Map<String, Object> syncProduct(Long productId);

    /**
     * 批量同步产品到ES
     *
     * @param productIds 产品ID列表
     * @param batchSize 批处理大小
     * @return 操作结果
     */
    Map<String, Object> batchSyncProducts(List<Long> productIds, Integer batchSize);

    /**
     * 全量同步产品到ES
     *
     * @param batchSize 批处理大小
     * @return 操作结果
     */
    Map<String, Object> syncAllProducts(Integer batchSize);

    /**
     * 流式全量同步产品到ES
     *
     * @param batchSize 批处理大小
     * @return 操作结果
     */
    Map<String, Object> syncAllProductsStream(Integer batchSize);

    /**
     * 从ES删除产品
     *
     * @param productId 产品ID
     * @return 操作结果
     */
    Map<String, Object> deleteProduct(Long productId);

    /**
     * 检查ES连接
     *
     * @return 连接状态
     */
    Map<String, Object> checkConnection();
}
