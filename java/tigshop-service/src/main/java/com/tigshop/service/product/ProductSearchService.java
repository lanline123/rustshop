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
import com.tigshop.bean.vo.product.KeywordSearchListVO;
import com.tigshop.bean.vo.product.ProductSearchFilterListVO;
import com.tigshop.bean.vo.product.ProductSearchFilterSelectedVO;

import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 *
 * @author Tigshop团队
 * @create 2024年11月20日 11:09
 */
public interface ProductSearchService {
    /**
     * 搜索列表
     *
     * @param productSearchClientDTO 查询条件
     * @return ProductListVO
     */
    Page<ProductListResDTO> list(ProductSearchClientDTO productSearchClientDTO);

    /**
     * 属性列表
     *
     * @param productSearchClientDTO 查询条件
     * @return ProductListVO
     */
    ProductSearchFilterListVO getFilterList(ProductSearchClientDTO productSearchClientDTO);

    /**
     * 已选择的属性列表
     *
     * @param productSearchClientDTO 查询条件
     * @return ProductListVO
     */
    ProductSearchFilterSelectedVO getFilterSelected(ProductSearchClientDTO productSearchClientDTO);

    /**
     * 搜索建议
     *
     * @param keyword 关键词
     * @return List<KeywordSearchListVO>
     */
    List<KeywordSearchListVO> searchGuess(String keyword);

    /**
     * 获取属性搜索建议
     *
     * @param searchDTO 搜索参数
     * @return 属性建议列表
     */
    List<Map<String, Object>> getAttributeSuggestions(ProductSearchClientDTO searchDTO);

    /**
     * 获取商品搜索建议
     *
     * @param keyword 搜索关键词
     * @return 商品搜索建议列表
     */
    List<KeywordSearchListVO> searchGuessPosProduct(String keyword);
}
