// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductStockDTO;
import com.tigshop.bean.vo.product.ProductSkuCountVO;
import com.tigshop.bean.vo.product.ProductStockPageVO;
import com.tigshop.bean.vo.product.ProductStockResultVO;
import com.tigshop.bean.vo.product.StoreProductStockResultVO;
import com.tigshop.mapper.common.BaseMapper;
import com.tigshop.bean.model.product.ProductSku;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品属性表映射
 *
 * @author Jayce
 * @create 2024年11月21日 13:32
 */
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
    Page<ProductStockResultVO> getProductStockPage(Page<ProductStockResultVO> page, ProductStockDTO productStockDTO);

    Page<StoreProductStockResultVO> getProductStockPageByO2O(Page<StoreProductStockResultVO> page, ProductStockDTO productStockDTO);

    /**
     * 查询商品所有SKU的价格列表
     *
     * @param productId 商品ID
     * @return SKU价格列表
     */
    List<BigDecimal> selectPricesByProductId(@Param("productId") Integer productId);

    /**
     * 查询商品SKU的最低价格
     *
     * @param productId 商品ID
     * @return 最低价格
     */
    BigDecimal selectMinPriceByProductId(@Param("productId") Integer productId);

    /**
     * 批量统计商品 SKU 数量（COUNT(*) GROUP BY product_id）。
     * <p>用途：CR-20260603-1720 / CD-035 — C 端列表卡片 wholesaleCardMoq 派生判定单/多规格
     * （skuCount &le; 1 = 单规格，可卡片加购；&gt; 1 = 多规格，跳详情）。
     * <p>注意：返回的 productId 仅包含存在 SKU 行的商品；调用方需对未返回的 productId
     * 按"sku_count=0"处理（保守跳详情）。
     *
     * @param productIds product.product_id 列表（调用方保证非空）
     * @return SKU 数量统计列表（productId + skuCount）
     */
    List<ProductSkuCountVO> countGroupByProductId(@Param("productIds") List<Integer> productIds);
}
