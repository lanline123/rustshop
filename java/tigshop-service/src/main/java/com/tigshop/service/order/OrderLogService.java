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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderLogCreateDTO;
import com.tigshop.bean.dto.order.OrderLogListDTO;
import com.tigshop.bean.dto.order.OrderLogUpdateDTO;
import com.tigshop.bean.model.order.OrderLog;
import com.tigshop.bean.vo.order.OrderLogVO;
import com.tigshop.service.common.BaseService;

/**
 * 订单商品服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface OrderLogService extends BaseService<OrderLog> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<OrderLogVO> list(OrderLogListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    OrderLogVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(OrderLogCreateDTO createDTO);

    /**
     * 快速创建日志
     *
     * @param description 描述
     * @param orderId     订单ID
     * @param orderSn     订单编号
     */
    void quickCreate(String description, Integer orderId, String orderSn);

    /**
     * 以管理后台身份快速创建日志
     *
     * @param description 描述
     * @param orderId     订单ID
     * @param orderSn     订单编号
     */
    void quickCreateBySystemAdmin(String description, Integer orderId, String orderSn);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(OrderLogUpdateDTO updateDTO);

}
