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

package com.tigshop.service.order;

import com.tigshop.bean.dto.order.AftersalesItemCreateDTO;
import com.tigshop.bean.dto.order.AftersalesItemListDTO;
import com.tigshop.bean.dto.order.AftersalesItemUpdateDTO;
import com.tigshop.bean.model.order.AftersalesItem;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.AftersalesItemVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 售后商品服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface AftersalesItemService extends BaseService<AftersalesItem> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    ListResVO<AftersalesItemVO, AftersalesItemListDTO> list(AftersalesItemListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    AftersalesItemVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(AftersalesItemCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(AftersalesItemUpdateDTO updateDTO);

    List<AftersalesItemVO> getItemByIds(List<Integer> ids, boolean isClientRequest);

    /**
     * 退回库存
     */
    void returnStock(Integer aftersaleId);

    /**
     * 获取有效的售后商品
     */
    List<AftersalesItem> validAftersalesItems(List<Integer> orderItemIds);
}
