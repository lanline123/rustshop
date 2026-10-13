package com.tigshop.service.logistics;

import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.vo.order.OrderVO;

import java.util.Map;

/**
 * 物流服务商策略接口（快递鸟 / 快递100）。
 * <p>providerType：1=快递鸟，2=快递100。</p>
 */
public interface LogisticsProvider {

    int PROVIDER_KDNIAO = 1;
    int PROVIDER_KUAIDI100 = 2;

    /** 服务商标识：1-快递鸟，2-快递100 */
    int providerType();

    /** 当前服务商凭证是否已配置（供后台订单详情「电子面单」开关判断） */
    boolean isConfigured();

    /**
     * 即时物流轨迹查询
     *
     * @param company    物流公司
     * @param trackingNo 运单号
     * @param mobile     收/寄件人手机号（顺丰等必填）
     */
    ShippingResult queryTrack(LogisticsCompany company, String trackingNo, String mobile);

    /**
     * 电子面单下单，返回结构对齐快递鸟：
     * {@code {Success, Reason, Order:{OrderCode, LogisticCode}, PrintTemplate}}
     */
    Map<String, Object> createElectronicWaybill(OrderVO order, String remark);
}
