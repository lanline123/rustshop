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

import com.tigshop.bean.dto.order.OrderCouponDetailCreateDTO;
import com.tigshop.bean.dto.order.OrderCouponDetailListDTO;
import com.tigshop.bean.dto.order.OrderCouponDetailUpdateDTO;
import com.tigshop.bean.model.order.OrderCouponDetail;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.OrderCouponDetailVO;
import com.tigshop.service.common.BaseService;

/**
 * 订单店铺优惠券服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface OrderCouponDetailService extends BaseService<OrderCouponDetail> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    ListResVO<OrderCouponDetailVO, OrderCouponDetailListDTO> list(OrderCouponDetailListDTO listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    OrderCouponDetailVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(OrderCouponDetailCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(OrderCouponDetailUpdateDTO updateDTO);

}
