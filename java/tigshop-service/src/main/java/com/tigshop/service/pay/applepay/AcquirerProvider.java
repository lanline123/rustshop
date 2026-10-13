package com.tigshop.service.pay.applepay;

/**
 * 具体收单机构适配器（每家一个实现，由 {@link AcquirerClientRouter} 按配置路由）。
 *
 * @author Tigshop团队
 */
public interface AcquirerProvider extends AcquirerClient {

    /**
     * 机构 code，与配置 applePayAcquirerProvider 取值对应：ums/stripe/adyen/generic。
     */
    String providerCode();
}
