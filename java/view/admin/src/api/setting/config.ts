import request from "@/utils/request";
import type { BaseFormResult, BaseFormState, AdminConfigState, OrderConfig } from "@/types/setting/config";

/**
 * 获取图标配置
 * @description 获取系统图标配置
 * @returns 图标配置
 */
export const getIcon = () => {
    return request<any>({
        url: "setting/config/getIcon",
        method: "get"
    });
};

/**
 * 获取基础配置
 * @description 获取系统基础配置信息
 * @param params - 查询参数
 * @returns 基础配置
 */
export const getConfig = (params: object) => {
    return request<BaseFormResult>({
        url: "setting/config/getBase",
        method: "get",
        params
    });
};

// export const getBasicConfig = () => {
//     return request<BaseFormResult>({
//         url: "setting/config/basicConfig",
//         method: "get"
//     });
// };
// export const saveBasicConfig = (params: object) => {
//     return request<BaseFormState>({
//         url: "setting/config/saveBasic",
//         method: "post",
//         data: params
//     });
// };

// export const saveConfig = (params: object) => {
//     return request<BaseFormState>({
//         url: "setting/config/save",
//         method: "post",
//         data: params
//     });
// };
/**
 * 创建平台证书
 * @description 生成平台安全证书
 * @returns 创建结果
 */
export const createPlatformCertificate = () => {
    return request({
        url: "setting/config/createPlatformCertificate",
        method: "post"
    });
};
// export const updateConfig = (params: object) => {
//     return request<BaseFormState>({
//         url: "setting/config/update",
//         method: "post",
//         data: params
//     });
// };
/**
 * 获取管理端配置
 * @description 获取后台管理端的配置信息
 * @returns 管理端配置
 */
export const getAdminConfig = () => {
    return request<AdminConfigState>({
        url: "setting/config/getAdmin",
        method: "get"
    });
};

/**
 * 获取管理端基础信息
 * @description 获取后台的基础配置
 * @returns 基础信息
 */
export const getAdminBase = () => {
    return request<AdminConfigState>({
        url: "setting/config/getAdminBase",
        method: "get"
    });
};


import type { BaseConfig, AmapAllResponse, BaseTheme, LoginConfig, OrderConfig,  GlobalConfig, PaymentFormState, BaseThemeStyle, BaseCategoryDecorate, BaseProductDetailStyle, BaseSearchStyle, BaseAuthenticationSettings,  BaseInterfactSettings, BaseKefuConfig, BasicProductConfig, BaseShoppingConfig, BaseNotice, BaseDisplayConfig, PersonalizedFormState, MiniProgramRiskSettingsForm } from "@/types/setting/config";
import type { WechatLiveSettingsForm } from "@/types/setting/config";

/**
 * 获取基础设置
 * @description 获取站点基础设置信息
 * @returns 基础设置
 */
export const getConfigBasic = () => {
    return request<BaseConfig>({
        url: "setting/config/basicSettings",
        method: "get"
    });
};
/**
 * 保存基础设置
 * @description 保存站点基础设置
 * @param data - 基础设置数据
 * @returns 操作结果
 */
export const saveConfigBasic = (data: BaseConfig) => {
    return request({
        url: "setting/config/saveBasic",
        method: "post",
        data
    });
};

/**
 * 获取全局配置
 * @description 获取系统全局配置信息
 * @returns 全局配置
 */
export const getConfigGlobal = () => {
    return request<GlobalConfig>({
        url: "setting/config/globalSettings",
        method: "get"
    });
};
/**
 * 保存全局配置
 * @description 保存系统全局配置
 * @param data - 全局配置数据
 * @returns 操作结果
 */
export const saveConfigGlobal = (data: object) => {
    return request<GlobalConfig>({
        url: "setting/config/saveGlobal",
        method: "post",
        data
    });
};

export interface WorkBuddyKeySettings {
    maskedKey: string;
    expiresAt?: number;
    configured: boolean;
}

export interface WorkBuddyKeyGenerateResult {
    key?: string;
    expiresAt?: number;
}

export const getWorkBuddyKeySettings = () => request<WorkBuddyKeySettings>({
    url: "setting/config/workbuddySettings",
    method: "get"
});

export const generateWorkBuddyKey = () => request<WorkBuddyKeyGenerateResult>({
    url: "setting/config/generateWorkbuddyKey",
    method: "post"
});

export const revealWorkBuddyKey = () => request<WorkBuddyKeyGenerateResult>({
    url: "setting/config/revealWorkbuddyKey",
    method: "post"
});

/**
 * 获取订单配置
 * @description 获取订单相关配置信息
 * @returns 订单配置
 */
export const getConfigOrder = () => {
    return request<OrderConfig>({
        url: "setting/config/orderSettings",
        method: "get"
    });
};
/**
 * 保存订单配置
 * @description 保存订单相关配置
 * @param data - 订单配置数据
 * @returns 操作结果
 */
export const saveConfigOrder = (data: OrderConfig) => {
    return request({
        url: "setting/config/saveOrder",
        method: "post",
        data
    });
};

/**
 * 获取登录配置
 * @description 获取用户登录相关配置
 * @returns 登录配置
 */
export const getConfigLogin = () => {
    return request<LoginConfig>({
        url: "setting/config/loginSettings",
        method: "get"
    });
};
/**
 * 保存登录配置
 * @description 保存用户登录相关配置
 * @param data - 登录配置数据
 * @returns 操作结果
 */
export const saveConfigLogin = (data: LoginConfig) => {
    return request({
        url: "setting/config/saveLogin",
        method: "post",
        data
    });
};

/**
 * 获取商品配置
 * @description 获取商品相关配置信息
 * @returns 商品配置
 */
export const getConfigProduct = () => {
    return request<BasicProductConfig>({
        url: "setting/config/productSettings",
        method: "get"
    });
};
/**
 * 保存商品配置
 * @description 保存商品相关配置
 * @param data - 商品配置数据
 * @returns 操作结果
 */
export const saveConfigProduct = (data: BasicProductConfig) => {
    return request({
        url: "setting/config/saveProduct",
        method: "post",
        data
    });
};

/**
 * 获取购物配置
 * @description 获取购物相关配置信息
 * @returns 购物配置
 */
export const getConfigShopping = () => {
    return request<BaseShoppingConfig>({
        url: "setting/config/shoppingSettings",
        method: "get"
    });
};
/**
 * 保存购物配置
 * @description 保存购物相关配置
 * @param data - 购物配置数据
 * @returns 操作结果
 */
export const saveConfigShopping = (data: BaseShoppingConfig) => {
    return request({
        url: "setting/config/saveShopping",
        method: "post",
        data
    });
};

/**
 * 获取通知配置
 * @description 获取消息通知相关配置
 * @returns 通知配置
 */
export const getConfigNotify = () => {
    return request<BaseNotice>({
        url: "setting/config/notifySettings",
        method: "get"
    });
};
/**
 * 保存通知配置
 * @description 保存消息通知相关配置
 * @param data - 通知配置数据
 * @returns 操作结果
 */
export const saveConfigNotify = (data: BaseNotice) => {
    return request({
        url: "setting/config/saveNotify",
        method: "post",
        data
    });
};

/**
 * 获取显示配置
 * @description 获取前端显示相关配置
 * @returns 显示配置
 */
export const getConfigShow = () => {
    return request<BaseDisplayConfig>({
        url: "setting/config/showSettings",
        method: "get"
    });
};
/**
 * 保存显示配置
 * @description 保存前端显示相关配置
 * @param data - 显示配置数据
 * @returns 操作结果
 */
export const saveConfigShow = (data: BaseDisplayConfig) => {
    return request({
        url: "setting/config/saveShow",
        method: "post",
        data
    });
};

/**
 * 获取客服配置
 * @description 获取在线客服相关配置
 * @returns 客服配置
 */
export const getConfigKefu = () => {
    return request<BaseKefuConfig>({
        url: "setting/config/kefuSettings",
        method: "get"
    });
};
/**
 * 保存客服配置
 * @description 保存在线客服相关配置
 * @param data - 客服配置数据
 * @returns 操作结果
 */
export const saveConfigKefu = (data: BaseKefuConfig) => {
    return request({
        url: "setting/config/saveKefu",
        method: "post",
        data
    });
};

/**
 * 获取接口配置
 * @description 获取第三方接口相关配置
 * @returns 接口配置
 */
export const getConfigApi = () => {
    return request<BaseInterfactSettings>({
        url: "setting/config/apiSettings",
        method: "get"
    });
};
/**
 * 保存接口配置
 * @description 保存第三方接口相关配置
 * @param data - 接口配置数据
 * @returns 操作结果
 */
export const saveConfigApi = (data: BaseInterfactSettings) => {
    return request({
        url: "setting/config/saveApi",
        method: "post",
        data
    });
};

/**
 * 获取认证配置
 * @description 获取用户认证相关配置
 * @returns 认证配置
 */
export const getConfigAuth = () => {
    return request<BaseAuthenticationSettings>({
        url: "setting/config/authSettings",
        method: "get"
    });
};
/**
 * 保存认证配置
 * @description 保存用户认证相关配置
 * @param data - 认证配置数据
 * @returns 操作结果
 */
export const saveConfigAuth = (data: BaseAuthenticationSettings) => {
    return request({
        url: "setting/config/saveAuth",
        method: "post",
        data
    });
};

/**
 * 获取分类页装修配置
 * @description 获取分类页面的装修配置
 * @returns 分类装修配置
 */
export const getConfigCategoryDecorate = () => {
    return request<BaseCategoryDecorate>({
        url: "setting/config/categoryDecorateSettings",
        method: "get"
    });
};
/**
 * 保存分类页装修配置
 * @description 保存分类页面的装修配置
 * @param data - 分类装修配置数据
 * @returns 操作结果
 */
export const saveConfigCategoryDecorate = (data: BaseCategoryDecorate) => {
    return request({
        url: "setting/config/saveCategoryDecorate",
        method: "post",
        data
    });
};

/**
 * 获取商品详情页风格配置
 * @description 获取 PC 商品详情页风格配置
 * @returns 商品详情页风格配置
 */
export const getConfigProductDetailStyle = () => {
    return request<BaseProductDetailStyle>({
        url: "setting/config/productDetailStyleSettings",
        method: "get"
    });
};
/**
 * 保存商品详情页风格配置
 * @description 保存 PC 商品详情页风格配置
 * @param data - 商品详情页风格配置数据
 * @returns 操作结果
 */
export const saveConfigProductDetailStyle = (data: BaseProductDetailStyle) => {
    return request({
        url: "setting/config/saveProductDetailStyle",
        method: "post",
        data
    });
};

/**
 * 获取搜索页风格配置
 * @description 获取 PC 搜索页风格配置
 * @returns 搜索页风格配置
 */
export const getConfigSearchStyle = () => {
    return request<BaseSearchStyle>({
        url: "setting/config/searchStyleSettings",
        method: "get"
    });
};
/**
 * 保存搜索页风格配置
 * @description 保存 PC 搜索页风格配置
 * @param data - 搜索页风格配置数据
 * @returns 操作结果
 */
export const saveConfigSearchStyle = (data: BaseSearchStyle) => {
    return request({
        url: "setting/config/saveSearchStyle",
        method: "post",
        data
    });
};

/**
 * 获取主题风格配置
 * @description 获取网站主题风格配置
 * @returns 主题风格配置
 */
export const getConfigThemeStyle = () => {
    return request<BaseThemeStyle>({
        url: "setting/config/themeStyleSettings",
        method: "get"
    });
};
/**
 * 保存主题风格配置
 * @description 保存网站主题风格配置
 * @param data - 主题风格配置数据
 * @returns 操作结果
 */
export const saveConfigThemeStyle = (data: BaseThemeStyle) => {
    return request({
        url: "setting/config/saveThemeStyle",
        method: "post",
        data
    });
};

/**
 * 获取店铺配置
 * @description 获取多商户店铺相关配置
 * @returns 店铺配置
 */
export const getConfigShopSetting = () => {
    return request<PaymentFormState>({
        url: "setting/config/shopSettings",
        method: "get"
    });
};
/**
 * 保存店铺配置
 * @description 保存多商户店铺相关配置
 * @param data - 店铺配置数据
 * @returns 操作结果
 */
export const saveConfigShopSetting = (data: PaymentFormState) => {
    return request({
        url: "setting/config/saveShop",
        method: "post",
        data
    });
};

/**
 * 获取主题切换配置
 * @description 获取主题布局切换配置
 * @returns 主题切换配置
 */
export const getConfigLayoutThemeSwitch = () => {
    return request<BaseTheme>({
        url: "setting/config/layoutThemeSwitchSettings",
        method: "get"
    });
};
/**
 * 保存主题切换配置
 * @description 保存主题布局切换配置
 * @param data - 主题切换配置数据
 * @returns 操作结果
 */
export const saveConfigLayoutThemeSwitch = (data: BaseTheme) => {
    return request({
        url: "setting/config/saveLayoutThemeSwitch",
        method: "post",
        data
    });
};

/**
 * 获取商户配置
 * @description 获取商户入驻相关配置
 * @returns 商户配置
 */
export const getConfigMerchantSetting = () => {
    return request<PaymentFormState>({
        url: "setting/config/merchantSettings",
        method: "get"
    });
};
/**
 * 保存商户配置
 * @description 保存商户入驻相关配置
 * @param data - 商户配置数据
 * @returns 操作结果
 */
export const saveConfigMerchantSetting = (data: PaymentFormState) => {
    return request({
        url: "setting/config/saveMerchant",
        method: "post",
        data
    });
};

/**
 * 获取地图配置
 * @description 获取高德地图相关配置
 * @param params - 查询参数
 * @returns 地图配置
 */
export const getConfigAmapAllSettings = (params?: object) => {
    return request<AmapAllResponse>({
        url: "setting/config/amapAllSettings",
        method: "get",
        params
    });
};
/**
 * 保存地图配置
 * @description 保存高德地图相关配置
 * @param data - 地图配置数据
 * @returns 操作结果
 */
export const saveAllAmapSettings = (data: AmapAllResponse) => {
    return request({
        url: "setting/config/saveAllAmapSettings",
        method: "post",
        data
    });
};

/**
 * 全量初始化商品到ES
 * @description 将所有商品数据同步到Elasticsearch
 * @returns 初始化结果
 */
export const InitAllToEsProduct = () => {
    return request({
        url: "product/product/initAllToEs",
        method: "post"
    });
};

/**
 * 获取个性化设置
 * @description 获取系统个性化设置信息
 * @returns 个性化设置
 */
export const getPersonalizedSetting = () => {
    return request<PersonalizedFormState>({
        url: "setting/config/getPersonalizedSetting",
        method: "get"
    });
};

/**
 * 保存个性化设置
 * @description 保存系统个性化设置
 * @param data - 个性化设置数据
 * @returns 操作结果
 */
export const savePersonalizedSetting = (data: PersonalizedFormState) => {
    return request({
        url: "setting/config/savePersonalizedSetting",
        method: "post",
        data
    });
};


/**
 * 获取个人中心装修新旧版开关
 * @description useNewMemberCenter：1=使用新版装修，0=使用旧版装修
 * @returns 开关配置
 */
export const getMemberCenterDecorateSetting = () => {
    return request<{ useNewMemberCenter: number }>({
        url: "setting/config/memberCenterDecorateSettings",
        method: "get"
    });
};

/**
 * 保存个人中心装修新旧版开关
 * @param data - { useNewMemberCenter: 1=新版 0=旧版 }
 * @returns 操作结果
 */
export const saveMemberCenterDecorate = (data: { useNewMemberCenter: number }) => {
    return request({
        url: "setting/config/saveMemberCenterDecorate",
        method: "post",
        data
    });
};

/**
 * 获取小程序风控配置
 * @description 获取小程序图片鉴黄等风控开关（与直播能力解耦，始终可用）
 */
export const getMiniProgramRiskSettings = () => {
    return request<MiniProgramRiskSettingsForm>({
        url: "setting/config/miniProgramRiskSettings",
        method: "get"
    });
};

/**
 * 保存小程序风控配置
 * @description 保存小程序图片鉴黄等风控开关
 */
export const saveMiniProgramRiskSettings = (data: MiniProgramRiskSettingsForm) => {
    return request({
        url: "setting/config/saveMiniProgramRisk",
        method: "post",
        data
    });
};

/**
 * 获取小程序直播独立小程序配置
 * @description 获取小程序直播/视频号所使用的独立小程序 AppID 和 AppSecret
 * @returns 小程序直播配置
 */
export const getWechatLiveSettings = () => {
    return request<WechatLiveSettingsForm>({
        url: "setting/config/wechatLiveSettings",
        method: "get"
    });
};

/**
 * 保存小程序直播独立小程序配置
 * @description 保存小程序直播/视频号所使用的独立小程序 AppID 和 AppSecret
 * @param data - 小程序直播配置数据
 * @returns 操作结果
 */
export const saveWechatLiveSettings = (data: WechatLiveSettingsForm) => {
    return request({
        url: "setting/config/saveWechatLive",
        method: "post",
        data
    });
};
