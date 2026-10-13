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

import com.tigshop.bean.model.product.ProductPresale;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品预售映射
 *
 * @author Tigshop团队
 * @create 2025年11月21日
 */
public interface ProductPresaleMapper extends BaseMapper<ProductPresale> {

    /**
     * 根据商品ID查询预售信息
     *
     * @param productId 商品ID
     * @return 预售信息
     */
    ProductPresale selectByProductId(@Param("productId") Integer productId);

    /**
     * 根据商品ID删除预售信息
     *
     * @param productId 商品ID
     * @return 删除记录数
     */
    int deleteByProductId(@Param("productId") Integer productId);
}

