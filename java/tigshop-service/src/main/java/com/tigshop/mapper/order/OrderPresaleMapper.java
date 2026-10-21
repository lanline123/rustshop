// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.order;

import com.tigshop.bean.model.order.OrderPresale;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单预售信息映射
 *
 * @author Tigshop团队
 * @create 2024年11月24日
 */
@Mapper
public interface OrderPresaleMapper extends BaseMapper<OrderPresale> {

    /**
     * 根据订单ID查询预售信息
     * @param orderId 订单ID
     * @return 预售信息
     */
    OrderPresale selectByOrderId(@Param("orderId") Integer orderId);

    /**
     * 根据订单号查询预售信息
     * @param orderSn 订单号
     * @return 预售信息
     */
    OrderPresale selectByOrderSn(@Param("orderSn") String orderSn);

    /**
     * 查询待支付尾款的订单（已到尾款开始支付时间）
     * @param currentTime 当前时间
     * @return 订单列表
     */
    List<OrderPresale> selectWaitingFinalPaymentOrders(@Param("currentTime") Long currentTime);
}

