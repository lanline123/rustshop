import request from "@/utils/request";
import type {
    PaymentFormState,
    BasicPaySettingsForm,
    WechatPaySettingsForm,
    AliPaySettingsForm,
    YaBandPaySettingsForm,
    OfflinePaySettingsForm,
    PayPalSettingsForm,
    YunPaySettingsForm,
    HuifuSettingsForm,
    LakalaSettingsForm,
    YeepaySettingsForm,
    ApplePaySettingsForm
} from "@/types/setting/payment.d";

/**
 * 获取支付设置
 * @description 获取所有支付相关配置
 * @returns 支付配置
 */
export const getPaymentSettings = () => {
    return request<PaymentFormState>({
        url: "setting/config/paySettings",
        method: "get"
    });
};

/**
 * 保存支付设置
 * @description 保存所有支付相关配置
 * @param data - 支付配置数据
 * @returns 操作结果
 */
export const updatePaymentSettings = (data: PaymentFormState) => {
    return request({
        url: "setting/config/savePay",
        method: "post",
        data
    });
};

/**
 * 获取支付基础设置
 * @description 获取支付基础配置
 * @returns 基础配置
 */
export const getBasicSettings = () => {
    return request<BasicPaySettingsForm>({
        url: "setting/config/basicPaySettings",
        method: "get"
    });
};
/**
 * 保存支付基础设置
 * @description 保存支付基础配置
 * @param data - 基础配置数据
 * @returns 操作结果
 */
export const updateBasicSettings = (data: BasicPaySettingsForm) => {
    return request({
        url: "setting/config/saveBasicPay",
        method: "post",
        data
    });
};

/**
 * 获取微信支付设置
 * @description 获取微信支付相关配置
 * @returns 微信支付配置
 */
export const getWechatSettings = () => {
    return request<WechatPaySettingsForm>({
        url: "setting/config/wechatPaySettings",
        method: "get"
    });
};
/**
 * 保存微信支付设置
 * @description 保存微信支付相关配置
 * @param data - 微信支付配置数据
 * @returns 操作结果
 */
export const updateWechatSettings = (data: WechatPaySettingsForm) => {
    return request({
        url: "setting/config/saveWechatPay",
        method: "post",
        data
    });
};

/**
 * 获取支付宝支付设置
 * @description 获取支付宝支付相关配置
 * @returns 支付宝配置
 */
export const getAliPaySettings = () => {
    return request<AliPaySettingsForm>({
        url: "setting/config/aliPaySettings",
        method: "get"
    });
};
/**
 * 保存支付宝支付设置
 * @description 保存支付宝支付相关配置
 * @param data - 支付宝配置数据
 * @returns 操作结果
 */
export const updateAliPaySettings = (data: AliPaySettingsForm) => {
    return request({
        url: "setting/config/saveAliPay",
        method: "post",
        data
    });
};

/**
 * 获取YaBandPay支付设置
 * @description 获取YaBandPay支付相关配置
 * @returns YaBandPay配置
 */
export const getYaBandPaySettings = () => {
    return request<YaBandPaySettingsForm>({
        url: "setting/config/yaBandPaySettings",
        method: "get"
    });
};
/**
 * 保存YaBandPay支付设置
 * @description 保存YaBandPay支付相关配置
 * @param data - YaBandPay配置数据
 * @returns 操作结果
 */
export const updateYaBandPaySettings = (data: YaBandPaySettingsForm) => {
    return request({
        url: "setting/config/saveYaBandPay",
        method: "post",
        data
    });
};

/**
 * 获取线下支付设置
 * @description 获取线下支付相关配置
 * @returns 线下支付配置
 */
export const getOfflinePaySettings = () => {
    return request<OfflinePaySettingsForm>({
        url: "setting/config/offlinePaySettings",
        method: "get"
    });
};
/**
 * 保存线下支付设置
 * @description 保存线下支付相关配置
 * @param data - 线下支付配置数据
 * @returns 操作结果
 */
export const updateOfflinePaySettings = (data: OfflinePaySettingsForm) => {
    return request({
        url: "setting/config/saveOfflinePay",
        method: "post",
        data
    });
};

/**
 * 获取PayPal支付设置
 * @description 获取PayPal支付相关配置
 * @returns PayPal配置
 */
export const getPayPalSettings = () => {
    return request<PayPalSettingsForm>({
        url: "setting/config/payPalSettings",
        method: "get"
    });
};
/**
 * 保存PayPal支付设置
 * @description 保存PayPal支付相关配置
 * @param data - PayPal配置数据
 * @returns 操作结果
 */
export const updatePayPalSettings = (data: PayPalSettingsForm) => {
    return request({
        url: "setting/config/savePayPal",
        method: "post",
        data
    });
};

/**
 * 获取云支付设置
 * @description 获取云支付相关配置
 * @returns 云支付配置
 */
export const getYunPaySettings = () => {
    return request<YunPaySettingsForm>({
        url: "setting/config/yunPaySettings",
        method: "get"
    });
};
/**
 * 保存云支付设置
 * @description 保存云支付相关配置
 * @param data - 云支付配置数据
 * @returns 操作结果
 */
export const updateYunPaySettings = (data: YunPaySettingsForm) => {
    return request({
        url: "setting/config/saveYunPay",
        method: "post",
        data
    });
};

/**
 * 创建平台证书
 * @description 生成支付平台安全证书
 * @returns 创建结果
 */
export const createPlatformCertificate = () => {
    return request({
        url: "setting/config/createPlatformCertificate",
        method: "post"
    });
};


/**
 * 获取汇付天下支付设置
 * @description 获取汇付斗拱聚合支付相关配置
 * @returns 汇付配置
 */
export const getHuifuSettings = () => {
    return request<HuifuSettingsForm>({
        url: "setting/config/huifuSettings",
        method: "get"
    });
};

/**
 * 保存汇付天下支付设置
 * @description 保存汇付斗拱聚合支付相关配置；密钥字段若为掩码串则后端跳过更新
 * @param data - 汇付配置数据
 * @returns 操作结果
 */
export const updateHuifuSettings = (data: HuifuSettingsForm) => {
    return request({
        url: "setting/config/saveHuifu",
        method: "post",
        data
    });
};

/**
 * 获取拉卡拉支付设置
 * @description 获取拉卡拉聚合支付相关配置；私钥/平台证书/SM4 密钥中段掩码
 * @returns 拉卡拉配置
 */
export const getLakalaSettings = () => {
    return request<LakalaSettingsForm>({
        url: "setting/config/lakalaSettings",
        method: "get"
    });
};

/**
 * 保存拉卡拉支付设置
 * @description 保存拉卡拉聚合支付相关配置；私钥/平台证书/SM4 密钥为掩码串时后端跳过更新
 * @param data - 拉卡拉配置数据
 * @returns 操作结果
 */
export const updateLakalaSettings = (data: LakalaSettingsForm) => {
    return request({
        url: "setting/config/saveLakala",
        method: "post",
        data
    });
};

/**
 * 获取易宝支付设置
 * @description 获取易宝聚合支付相关配置；商户私钥中段掩码
 * @returns 易宝配置
 */
export const getYeepaySettings = () => {
    return request<YeepaySettingsForm>({
        url: "setting/config/yeepaySettings",
        method: "get"
    });
};

/**
 * 保存易宝支付设置
 * @description 保存易宝聚合支付相关配置；商户私钥为掩码串时后端跳过更新
 * @param data - 易宝配置数据
 * @returns 操作结果
 */
export const updateYeepaySettings = (data: YeepaySettingsForm) => {
    return request({
        url: "setting/config/saveYeepay",
        method: "post",
        data
    });
};

/**
 * 获取 Apple Pay 支付设置
 * @description 获取 Apple Pay 苹果支付相关配置（含国家/货币下拉源，密钥中段掩码）
 * @returns Apple Pay 配置
 */
export const getApplePaySettings = () => {
    return request<ApplePaySettingsForm>({
        url: "setting/config/applePaySettings",
        method: "get"
    });
};

/**
 * 保存 Apple Pay 支付设置
 * @description 保存 Apple Pay 苹果支付相关配置；密钥字段为掩码串时后端跳过更新；只读的国家/货币下拉源不提交
 * @param data - Apple Pay 配置数据
 * @returns 操作结果
 */
export const updateApplePaySettings = (data: ApplePaySettingsForm) => {
    const { applePayCountryList, applePayCurrencyList, ...payload } = data;
    return request({
        url: "setting/config/saveApplePay",
        method: "post",
        data: payload
    });
};
