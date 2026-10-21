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

package com.tigshop.service.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.ShopFundsCreateDTO;
import com.tigshop.bean.dto.shop.ShopFundsListDTO;
import com.tigshop.bean.dto.shop.ShopFundsUpdateDTO;
import com.tigshop.bean.model.shop.ShopFunds;
import com.tigshop.bean.vo.shop.ShopFundsVO;
import com.tigshop.service.common.BaseService;

/**
 * 店铺表服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ShopFundsService extends BaseService<ShopFunds> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return Page<ShopFundsVO>
     */
    Page<ShopFundsVO> list(ShopFundsListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ShopFundsVO
     */
    ShopFundsVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ShopFundsCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ShopFundsUpdateDTO updateDTO);

    /**
     * 根据商户id获取店铺数量
     * @param merchantId 商户id
     * @return Long
     */
    Long getCountByMerchantId(Integer merchantId);

    /**
     * 根据店铺id获取店铺信息
     * @param shopId 店铺id
     * @return ShopFunds
     */
    ShopFunds getShopByShopId(Integer shopId);
}
