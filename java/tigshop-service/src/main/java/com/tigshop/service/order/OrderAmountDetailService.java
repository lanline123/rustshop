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

import com.tigshop.bean.dto.order.OrderAmountDetailCreateDTO;
import com.tigshop.bean.dto.order.OrderAmountDetailListDTO;
import com.tigshop.bean.dto.order.OrderAmountDetailUpdateDTO;
import com.tigshop.bean.model.order.OrderAmountDetail;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.OrderAmountDetailVO;
import com.tigshop.service.common.BaseService;

/**
 * 订单店铺金额服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface OrderAmountDetailService extends BaseService<OrderAmountDetail> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    ListResVO<OrderAmountDetailVO, OrderAmountDetailListDTO> list(OrderAmountDetailListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    OrderAmountDetailVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(OrderAmountDetailCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(OrderAmountDetailUpdateDTO updateDTO);

}
