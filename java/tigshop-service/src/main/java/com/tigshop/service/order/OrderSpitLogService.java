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

import com.tigshop.bean.dto.order.OrderSpitLogCreateDTO;
import com.tigshop.bean.dto.order.OrderSpitLogListDTO;
import com.tigshop.bean.dto.order.OrderSpitLogUpdateDTO;
import com.tigshop.bean.model.order.OrderSpitLog;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.OrderSpitLogVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.service.common.BaseService;

/**
 * 订单拆分服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface OrderSpitLogService extends BaseService<OrderSpitLog> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    ListResVO<OrderSpitLogVO, OrderSpitLogListDTO> list(OrderSpitLogListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    OrderSpitLogVO detail(Integer id);

    /**
     * 订单id获得数据
     *
     * @param orderId 订单ID
     * @return OrderSpitLog
     */
    OrderSpitLog getByOrderId(Integer orderId);

    Object getParentDataByOrderId(Integer orderId);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(OrderSpitLogCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(OrderSpitLogUpdateDTO updateDTO);

    void addSplitLog(Integer orderId, Integer orderId1, OrderVO order);
}
