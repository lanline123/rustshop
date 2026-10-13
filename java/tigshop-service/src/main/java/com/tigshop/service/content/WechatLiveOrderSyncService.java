package com.tigshop.service.content;

import com.tigshop.bean.dto.content.WechatLiveSyncCallbackDTO;
import com.tigshop.bean.model.finance.RefundApply;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.vo.order.OrderItemVO;

import java.util.List;

/**
 * 微信直播订单同步服务
 *
 * @author Tigshop团队
 * @create 2026年03月08日 16:56
 */
public interface WechatLiveOrderSyncService {

    void bindOrderRelations(Order order, List<OrderItem> orderItems);

    void onOrderCreated(Order order);

    void onOrderPaid(Order order);

    void onShippingSyncStart(Order order, List<OrderItemVO> deliverItems);

    void onShippingSyncSuccess(Order order, List<OrderItemVO> deliverItems, String requestData, String responseData);

    void onShippingSyncFailed(Order order, String errorMessage, String requestData);

    void onAftersaleChanged(Aftersales aftersales, String eventType);

    void onRefundChanged(RefundApply refundApply, String eventType);

    void handleInboundCallback(WechatLiveSyncCallbackDTO callbackDTO);

    void retrySyncLog(Long logId);

    void processPendingLogs();

    void reconcileLiveOrders();
}
