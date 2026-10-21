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

package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.ProductGiftCreateDTO;
import com.tigshop.bean.dto.promotion.ProductGiftListDTO;
import com.tigshop.bean.dto.promotion.ProductGiftUpdateDTO;
import com.tigshop.bean.model.promotion.ProductGift;
import com.tigshop.bean.vo.promotion.ProductGiftVO;
import com.tigshop.service.common.BaseService;

/**
 * 活动赠品服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface ProductGiftService extends BaseService<ProductGift> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<ProductGiftVO> list(ProductGiftListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    ProductGiftVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(ProductGiftCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(ProductGiftUpdateDTO updateDTO);

    /**
     * 减库存
     * @param giftId
     * @param quantity
     */
    void decStock(Integer giftId, Integer quantity);
}
