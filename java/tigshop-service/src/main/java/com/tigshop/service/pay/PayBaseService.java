package com.tigshop.service.pay;

import org.springframework.transaction.annotation.Transactional;

/**
 * 订单支付
 *
 * @author Tigshop团队
 * @create 2024-09-30 15:16:06
 */
public interface PayBaseService {
    /**
     * 获取退款回调地址
     *
     * @param payCode 支付渠道
     * @return String
     */
    String getRefundNotifyUrl(String payCode);

    /**
     * 获取支付回调地址
     *
     * @param payCode 支付渠道
     * @return String
     */
    String getPayNotifyUrl(String payCode);

    /**
     * 获取支付回调跳转地址
     *
     * @param orderId 订单ID
     * @return String
     */
    String getReturnUrl(Integer orderId);

    /**
     * 判断是否支付
     *
     * @param paySn
     */
    boolean checkOrderPayStatus(String paySn);

    /**
     * 支付成功
     *
     * @param paySn
     * @return
     */
    void paySuccess(String paySn, String tranceNo);


    @Transactional(rollbackFor = Exception.class)
    void refundSuccess(String refundSn);
}