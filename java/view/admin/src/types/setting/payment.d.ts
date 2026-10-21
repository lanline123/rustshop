export interface PaymentFormState {}

export interface BasicPaySettingsForm {
    useSurplus: number;
    usePoints: number;
    useCoupon: number;
    useCredit: number;
    /**
     * 支付顺序：多个用逗号隔开，如 wechat,alipax,paypal,stripe
     */
    paymentOrder: string;
}
export interface WechatPaySettingsForm {
    useWechat: number;
    /**
     * 是否使用YaBandPay（聚合支付）
     * 0: 直连微信支付
     * 1: 使用YaBandPay
     */
    wechatUseYabanpay: number;
    wechatMchidType: number;
    wechatPayMchid: string;
    wechatPaySubMchid: number;
    wechatPayKey: string;
    wechatPaySerialNo: string;
    wechatPayCertificate: number;
    wechatPayPrivateKey: number;
    wechatPayCheckType: number;
    wechatPayPlatformCertificate: number;
    wechatPayPublicKeyId: number;
    wechatPayPublicKey: number;
    wechatMiniProgramAppId: string;
    wechatMiniProgramSecret: string;
    wechatPayAppId: string;
    wechatPayAppSecret: string;
    wechatPayV2Key: string;
}
export interface AliPaySettingsForm {
    useAlipay: number;
    /**
     * 是否使用YaBandPay（聚合支付）
     * 0: 直连支付宝支付
     * 1: 使用YaBandPay
     */
    alipayUseYabanpay: number;
    alipayAppid: string;
    alipayMobileAppid: string;
    alipayRsaPrivateKey: string;
    alipayRsaPublicKey: string;
}
export interface YaBandPaySettingsForm {
    useYabanpay: number;
    useYabanpayWechat: number;
    useYabanpayAlipay: number;
    yabanpayCurrency: string;
    yabandpayUid: string;
    yabandpaySecretKey: string;
    yabanpayCurrencyList?: YaBandPayCurrency[];
}

export interface OfflinePaySettingsForm {
    useOffline: number;
    thirdPartyPaymentTransferEnable: number; // 第三方支付转账是否启用 0关闭 1开启
    offlinePayBankEnable: number; // 银行汇款是否启用 0关闭 1开启
    offlinePayCompanyEnable: number; // 企业汇款是否启用 0关闭 1开启
    otherPaymentRemittanceEnable: number; // 其他汇款方式是否启用 0关闭 1开启
    thirdPartyPaymentTransfer: string;
    offlinePayBank: string;
    offlinePayCompany: string;
    otherPaymentRemittance: string;
}

export interface PayPalSettingsForm {
    usePaypal: number;
    paypalCurrency: string;
    paypalClientId: string;
    paypalSecret: string;
    paypalCurrencyList?: PayPalCurrency[];
}
export interface YunPaySettingsForm {
    useYunpay: number;
    yunpayUid: string;
    yunpaySecretKey: string;
}

export interface YaBandPayCurrency {
    id: number;
    name: string;
    value: string;
}

export interface PayPalCurrency {
    id: number;
    name: string;
    value: string;
}

/** 汇付天下（汇付斗拱）支付设置；总开关字段暂定 useHuifu，后端最终字段确定后替换 */
export interface HuifuSettingsForm {
    /** 是否启用汇付天下：0关闭 1开启 */
    useHuifu: number;
    /** 汇付系统号 sys_id */
    huifuSysId: string;
    /** 汇付产品号 product_id */
    huifuProductId: string;
    /** 汇付商户号 huifu_id */
    huifuMerchantId: string;
    /** 商户RSA私钥（回显可能已掩码） */
    huifuRsaPrivateKey: string;
    /** 汇付公钥（回显可能已掩码） */
    huifuPublicKey: string;
    /** 汇付API网关地址 */
    huifuGateway: string;
    /** 统一收银台托管项目号 project_id（PC 收银台用；留空则不下发） */
    huifuHostingProjectId: string;
}

/**
 * 拉卡拉支付设置（一期：微信 / 支付宝聚合收单）
 * 私钥 / 平台证书 / SM4 密钥为文本粘贴，读取时中间段掩码，未改动则原样回传（后端跳过更新）
 */
export interface LakalaSettingsForm {
    /** 是否启用拉卡拉支付：0关闭 1开启 */
    useLakala: number;
    /** 拉卡拉开放平台 AppId */
    lakalaAppId: string;
    /** 商户号 merchantNo */
    lakalaMerchantNo: string;
    /** 终端号 termNo */
    lakalaTermNo: string;
    /** 商户证书序列号 serialNo */
    lakalaSerialNo: string;
    /** 商户私钥（PKCS8 Base64，回显掩码） */
    lakalaMerchantPrivateKey: string;
    /** 拉卡拉开放平台证书（.cer 内容，用于验签，回显掩码） */
    lakalaPlatformCert: string;
    /** 报文对称加密 SM4 密钥（可选，回显掩码） */
    lakalaSm4Key: string;
    /** 拉卡拉网关地址（生产/沙箱不同） */
    lakalaGatewayUrl: string;
    /** 环境：0生产 1沙箱/测试 */
    lakalaSandbox: number;
    /** 微信子 AppId（公众号 JSAPI 场景用） */
    lakalaWxSubAppid: string;
    /** 微信小程序 AppId（小程序场景用） */
    lakalaWxMiniAppid: string;
}

/**
 * 易宝支付设置（微信 / 支付宝聚合收单）
 * 商户私钥为文本粘贴，读取时中间段掩码，未改动则原样回传（后端跳过更新）
 */
export interface YeepaySettingsForm {
    /** 是否启用易宝支付：0关闭 1开启 */
    useYeepay: number;
    /** 易宝开放平台应用 AppKey */
    yeepayAppKey: string;
    /** 发起方商户编号 parentMerchantNo */
    yeepayParentMerchantNo: string;
    /** 收款商户编号 merchantNo（留空则与发起方商户号一致） */
    yeepayMerchantNo: string;
    /** 商户（ISV）私钥（PKCS8 Base64，回显掩码） */
    yeepayIsvPrivateKey: string;
    /** 易宝接口地址（选填，留空用 SDK 默认地址） */
    yeepayServerRoot: string;
}

/** Apple Pay 国家/地区下拉源项（后端 config 预置只读 JSON） */
export interface ApplePayCountry {
    id?: number;
    name: string;
    value: string;
}

/** Apple Pay 货币下拉源项（后端 config 预置只读 JSON） */
export interface ApplePayCurrency {
    id?: number;
    name: string;
    value: string;
}

/**
 * Apple Pay 苹果支付设置
 * 一期原生单流程，无沙箱字段；证书只存状态（0未传/1已传），密钥中段掩码
 */
export interface ApplePaySettingsForm {
    /** 是否启用 Apple Pay：0关闭 1开启 */
    useApplePay: number;
    /** 商户标识 Merchant ID，形如 merchant.com.yourshop */
    applePayMerchantId: string;
    /** 商户展示名（Apple Pay 面板展示） */
    applePayDisplayName: string;
    /** 国家/地区 ISO 3166 两位码，默认 CN */
    applePayCountryCode: string;
    /** 货币 ISO 4217 三位码，默认 CNY */
    applePayCurrency: string;
    /** 支持卡组织，逗号拼接：chinaUnionPay,visa,masterCard,amex,jcb,discover */
    applePaySupportedNetworks: string;
    /** 商户身份证书状态：0未传 1已传（uploadFile type=4） */
    applePayMerchantIdentityCert: number;
    /** 商户身份证书私钥状态：0/1（uploadFile type=5） */
    applePayMerchantIdentityKey: number;
    /** 支付处理证书私钥状态：0/1（uploadFile type=6，仅 generic 需要） */
    applePayProcessingPrivateKey: number;
    /** 收单机构：generic 通用自解密 / adyen 跨境 / ums 银联商务 */
    applePayAcquirerProvider: string;
    /** 银联签名类型（仅 ums）：SHA256 默认 / SM3 */
    applePayUmsSignType: string;
    /** 收单网关地址 */
    applePayAcquirerGateway: string;
    /** 收单商户号（回显中段掩码） */
    applePayAcquirerMerchantNo: string;
    /** 收单密钥（回显中段掩码） */
    applePayAcquirerKey: string;
    /** 国家下拉数据源（只读，不参与提交） */
    applePayCountryList?: ApplePayCountry[];
    /** 货币下拉数据源（只读，不参与提交） */
    applePayCurrencyList?: ApplePayCurrency[];
}
