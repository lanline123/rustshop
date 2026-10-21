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
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.product.ProductSearchClientDTO;
import com.tigshop.bean.vo.product.ProductSearchFilterListVO;
import com.tigshop.bean.vo.product.ProductSearchFilterSelectedVO;

import java.util.List;
import java.util.Map;

/**
 * 产品ES搜索服务接口
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
public interface ProductEsSearchService {

    /**
     * ES搜索产品列表
     *
     * @param searchDTO 搜索参数
     * @return 产品列表
     */
    Page<ProductListResDTO> searchProducts(ProductSearchClientDTO searchDTO);

    /**
     * 获取ES搜索结果总数
     *
     * @param searchDTO 搜索参数
     * @return 总数
     */
    Long getProductCount(ProductSearchClientDTO searchDTO);

    /**
     * 获取筛选列表
     *
     * @param searchDTO 搜索参数
     * @return 筛选列表
     */
    ProductSearchFilterListVO getFilterList(ProductSearchClientDTO searchDTO);

    /**
     * 获取已选择的筛选条件
     *
     * @param searchDTO 搜索参数
     * @return 已选择的筛选条件
     */
    ProductSearchFilterSelectedVO getFilterSelected(ProductSearchClientDTO searchDTO);

    /**
     * 获取属性搜索建议
     *
     * @param searchDTO 搜索参数
     * @return 属性建议列表
     */
    List<Map<String, Object>> getAttributeSuggestions(ProductSearchClientDTO searchDTO);
}
