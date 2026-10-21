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

package com.tigshop.service.product;

import com.tigshop.bean.dto.product.ProductAttrsDTO;
import com.tigshop.bean.model.product.ProductAttribute;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 商品属性服务
 *
 * @author Tigshop团队
 * @create 2024年11月28日 09:54
 */
public interface ProductAttributesService extends BaseService<ProductAttribute> {
    /**
     * 根据商品id获取商品属性
     *
     * @param productId 商品id
     * @return List<ProductAttribute>
     */
    List<ProductAttribute> getAttributesByProductId(int productId);

    /**
     * 根据商品id获取商品属性
     *
     * @param productId 商品id
     * @return ProductAttrsDTO
     */
    ProductAttrsDTO getProductAttributes(int productId);

    /**
     * 根据id批量获取商品属性
     *
     * @param ids 属性Ids
     * @return List<ProductAttribute>
     */
    List<ProductAttribute> getProductAttributesByIds(List<Integer> ids);
}
