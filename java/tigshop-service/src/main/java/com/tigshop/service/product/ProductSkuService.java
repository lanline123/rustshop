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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductSkuDTO;
import com.tigshop.bean.dto.product.ProductStockDTO;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.query.product.ProductSkuListQuery;
import com.tigshop.bean.vo.product.ProductStockPageVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 商品sku服务接口
 *
 * @author Jayce
 * @create 2024年11月21日 13:31
 */
public interface ProductSkuService extends BaseService<ProductSku> {
    /**
     * 根据商品id获取商品sku列表
     *
     * @param productId 商品id
     * @return 商品sku列表
     */
    List<ProductSkuDTO> getProductSkusByPid(Integer productId);

    /**
     * 根据商品id获取商品sku列表。
     *
     * @param productId 商品id
     * @param includeDisabled 是否包含禁用sku
     * @return 商品sku列表
     */
    List<ProductSkuDTO> getProductSkusByPid(Integer productId, boolean includeDisabled);

    /**
     * 分页获取商品sku列表（对应商品详情 productList，默认每页 50 条），支持关键字/库存区间过滤与白名单排序
     *
     * @param query 查询参数（id 必填；page/size/keyword/stockMin/stockMax/sortField/sortOrder 可选）
     * @return 商品sku分页
     */
    Page<ProductSkuDTO> getProductSkusPage(ProductSkuListQuery query);

    /**
     * 校验指定 SKU 是否属于商品且处于启用状态；skuId 为 0 时按单规格商品处理。
     *
     * @param productId 商品ID
     * @param skuId SKU ID
     */
    void checkSkuEnabled(Integer productId, Integer skuId);

    /**
     * 根据商品id列表获取商品sku列表
     *
     * @param productIds 商品id列表
     * @return 商品sku列表
     */
    List<ProductSkuDTO> getProductSkusByProductIds(List<Integer> productIds);

    /**
     * 校验商品库存
     *
     * @param quantity   数量
     * @param productId  商品id
     * @param skuId      sku id
     * @return 校验结果
     */
    boolean checkProductStock(int quantity, int productId, int skuId);

    /**
     * 获取商品库存
     *
     * @param productId  商品id
     * @param skuId      sku id
     * @return 商品库存
     */
    int getProductStock(int productId, int skuId);

    /**
     * 根据商品sn获取商品sku
     *
     * @param goodsSn 商品sn
     * @return 商品sku
     */
    ProductSku getProductSkusBySn(String goodsSn);

    /**
     * 转换为dto
     *
     * @param productSku 商品sku
     * @return dto
     */
    ProductSkuDTO convertToDTO(ProductSku productSku);

    /**
     * 获取商品sku详情
     *
     * @param skuId sku id
     * @return 商品sku详情
     */
    ProductSkuDTO getDetail(int skuId, Long storeProductId);

    void decStock(Integer skuId, Integer quantity,String description);

    void incStock(Integer skuId, Integer quantity ,String description);

    Page<ProductStockPageVO> getProductStockPage(ProductStockDTO productStockDTO);

    Page<ProductStockPageVO> getProductStockRecordPage(ProductStockDTO productStockDTO);
}
