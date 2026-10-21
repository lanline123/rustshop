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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.product.ProductGroupCreateDTO;
import com.tigshop.bean.dto.product.ProductGroupListDTO;
import com.tigshop.bean.dto.product.ProductGroupUpdateDTO;
import com.tigshop.bean.model.product.ProductGroup;
import com.tigshop.bean.vo.product.ProductGroupVO;
import com.tigshop.service.common.BaseService;

/**
 * 商品分组服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ProductGroupService extends BaseService<ProductGroup> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ProductGroupVO> list(ProductGroupListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ProductGroupVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ProductGroupCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ProductGroupUpdateDTO updateDTO);

    /**
     * 根据商品分组名称插入商品id
     *
     * @return boolean
     */
    boolean updateGroupProductByName(Integer productId, String groupName);

}
