package com.tigshop.service.pay;

import com.tigshop.bean.bo.pay.ProfitSharingPaymentContext;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;

/** 支持消费服务端分账支付快照上下文的支付客户端。 */
public interface ProfitSharingAwarePayService extends PayService {

    PayCreateVO.PayInfo pay(OrderVO orderVO, String openid, String clientType,
                            ProfitSharingPaymentContext context);
}
