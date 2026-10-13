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

import com.tigshop.bean.dto.order.OrderItemCreateDTO;
import com.tigshop.bean.dto.order.OrderItemListDTO;
import com.tigshop.bean.dto.order.OrderItemUpdateDTO;
import com.tigshop.bean.dto.order.OrderModifyProductDTO;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 订单商品服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface OrderItemService extends BaseService<OrderItem> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    ListResVO<OrderItemVO, OrderItemListDTO> list(OrderItemListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    OrderItemVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(OrderItemCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(OrderItemUpdateDTO updateDTO);

    /**
     * 删除
     *
     * @param ids 订单ID的LIST
     * @return List<OrderItemVo>
     */
    List<OrderItemVO> getItemByOrderIds(List<Integer> ids);

    /**
     * 查询订单商品，并按请求上下文决定是否携带供应商供货价快照。
     *
     * @param ids                 订单ID列表
     * @param includeSupplyPrice 仅后台供应商订单价格组装时传 true
     * @return 订单商品列表
     */
    List<OrderItemVO> getItemByOrderIds(List<Integer> ids, boolean includeSupplyPrice);

    void modifyOrderItem(OrderVO orderVO, OrderModifyProductDTO modifyProductDTO);
}
