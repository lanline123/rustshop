package com.tigshop.service.pay.impl;

import com.tigshop.bean.bo.PayPlatformStateBO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.bean.vo.order.pay.PayNotifyVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.pay.PayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service("CashPayService")
@Slf4j
public class CashPayServiceImpl extends PayBaseServiceImpl implements PayService {


    /**
     *
     * @param orderVO    订单信息
     * @param openid     openid
     * @param clientType 支付客户端类型
     * @return
     */
    @Override
    public PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType) {
        return null;
    }


    /**
     * 现金暂无退款功能
     *
     * @param outTradeNo   商户订单号
     * @param refundNo     退款单号
     * @param refundAmount 退款金额
     * @param totalAmount  总金额
     * @param clientType   支付客户端类型
     * @return
     */
    @Override
    public PayNotifyVO payRefund(String outTradeNo, String refundNo, BigDecimal refundAmount, BigDecimal totalAmount, String clientType, String subMchId) {
        throw new GlobalException("现金支付暂无退款功能");
    }

    @Override
    public String payNotify(Map<String, Object> resMap) {
        String paySn = (String) resMap.get("out_trade_no");
        paySuccess(paySn, (String) resMap.get("trade_no"));
        return "success";
    }

    @Override
    public String refundNotify(Map<String, Object> map) {
        throw new GlobalException("现金支付暂无退款通知功能");
    }

    @Override
    public PayPlatformStateBO payPlatformStatus(String outTradeNo, String tradeNo, String clientType) {
        return null;
    }
}
