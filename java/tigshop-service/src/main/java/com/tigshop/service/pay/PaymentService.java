package com.tigshop.service.pay;

import java.util.List;

/**
 * 订单支付
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
public interface PaymentService {

    /**
     * 获取支付页面信息
     */
    List<String> getAvailablePayment(String type);

    /**
     * 退款回调成功处理
     */
    void refundSuccess(String refundSn);
}