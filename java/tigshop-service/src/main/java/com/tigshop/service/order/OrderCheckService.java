package com.tigshop.service.order;

import com.tigshop.bean.param.order.OrderCheckParam;
import com.tigshop.bean.model.finance.OrderInvoice;
import com.tigshop.bean.query.order.StoreShippingTypeQuery;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.bean.vo.order.OrderSubmitVO;

import java.util.List;
import java.util.Map;

/**
 * 订单结算
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
public interface OrderCheckService {

    /**
     * 获取订单发票信息
     */
    OrderInvoice getLastInvoice(Integer invoiceType, Integer titleType);

    /**
     * 获取订单结算信息
     */
    OrderCheckVO getOrderCheckData(OrderCheckParam param);

    /**
     * 提交订单
     */
    OrderSubmitVO submit(OrderCheckParam param);

    /**
     * 获取可用的支付方式
     */
    List<OrderCheckVO.PaymentType> getAvailablePaymentType(Integer flowType);

    /**
     * 获取店铺配送方式
     */
    Map<Integer, List<OrderCheckVO.ShippingType>> getStoreShippingType(StoreShippingTypeQuery query);

}