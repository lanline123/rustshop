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
import com.tigshop.bean.query.salesman.SalesmanProductPageQuery;
import com.tigshop.bean.vo.product.ProductSkuVO;
import com.tigshop.bean.vo.salesman.SalesmanProductListVO;
import com.tigshop.mapper.common.BaseMapper;
import com.tigshop.bean.model.product.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品映射
 *
 * @author Tigshop团队
 * @create 2024年11月20日 11:08
 */
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 分销商品列表
     */
    Page<SalesmanProductListVO> salesmanProducts(@Param("page") Page<SalesmanProductListVO> page, @Param("pageQuery") SalesmanProductPageQuery pageQuery);

    /**
     * 根据关键字和语言代码模糊搜索商品ID
     *
     * @param keyword    搜索关键词
     * @param localeCode 语言地区代码 (如 en_US, zh_CN)
     * @return 商品ID列表
     */
    List<Integer> searchProductIds(@Param("keyword") String keyword,
                                   @Param("localeCode") String localeCode);

    /** 后台按展示译名搜索；不预先限制商品状态，兼容按原文存储且无 data_id 的译文。 */
    List<Integer> searchProductIdsByDisplayedName(@Param("keyword") String keyword,
                                                @Param("localeCode") String localeCode,
                                                @Param("dataType") Integer dataType);

    Page<ProductSkuVO> selectProductList(Page<ProductSkuVO> page,
                                         @Param("keyword") String keyword,
                                         @Param("categoryIds") List<Integer> categoryIds,
                                         @Param("shopId") Integer shopId,
                                         @Param("productType") List<Integer> productType,
                                         @Param("isSaleMode") Integer isSaleMode,
                                         @Param("isIncludeCycleProduct") Integer isIncludeCycleProduct,
                                         @Param("excludeProductTypes") List<Integer> excludeProductTypes);

    ProductSkuVO selectProduct(@Param("productId") Integer productId,
                                         @Param("skuId") Integer skuId);
}
