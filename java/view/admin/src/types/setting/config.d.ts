// 获取详情返回参数类型

export interface BaseFormResult {
    item?: BaseFormState;
    countrys: Regions[];
}

export interface Regions {
    regionId: number;
    regionName: string;
}

export interface BaseFormState {
    base: BaseConfig;
    baseApiAppPay: BaseApiAppPayConfig;
    baseApiCollection: BaseApiCollectionConfig;
    baseApiIcon: BaseApiIconConfig;
    baseApiMiniProgram: BaseApiMiniProgramConfig;
    baseApiStorage: BaseApiStorageConfig;
    baseApiWechat: BaseApiWechatConfig;
    baseApiWechatMerchant: BaseApiWechatMerchantConfig;
    baseDisplay: BaseDisplayConfig;
    baseKefu: BaseKefuConfig;
    baseMailbox: BaseMailboxConfig;
    baseShopping: BaseShoppingConfig;
    baseSms: BaseSmsConfig;
    baseApiCompanyData: {
        type: number;
        smsNote: number;
        tips: string;
    };
    baseApiLang: {
        langOn?: number;
        langType?: number;
        langVolcengineAccessKey?: string;
        langVolcengineSecret?: string;
    };
}

interface GrowUpSetting {
    buyGoods: number;
    buyGoodsMoney: number;
    buyGoodsGrowth: number;
    buyOrder: number;
    buyOrderNumber: number;
    buyOrderGrowth: number;
    evpi: number;
    evpiGrowth: number;
    bindPhone: number;
    bindPhoneGrowth: number;
}

interface BaseConfig {
    /** 经营许可证展示开关：1=开启（默认），0=关闭 */
    businessLicenseEnabled: number;
    businessLicense: string;
    ediLicense: string;
    businessLicenseType: string[];
    shopName: string;
    kefuAddress: string;
    uploadSaveFullDomain?: number;
    shopLogo: string;
    lightShopLogo: string;
    shopIcpNo: string;
    shopIcpNoUrl: string;
    shop110No: string;
    shop110Link: string;
    shopRegClosed: number;
    closeOrder: number;
    shopCompany: number;
    shopCompanyTxt: string;
    poweredByStatus: number;
    poweredBy: number;
    poweredByLogo: string;
    defaultTechSupport: string;
    defaultCopyright: string;
}

interface GlobalConfig {
    workbuddyApiKey?: string;
    workbuddyApiKeyExpiresAt?: number;
    shopTitleSuffix: string;
    shopTitle: string;
    shopKeywords: string;
    shopDesc: string;
    defaultAvatar: string;
    icoImg: string;
    pcDomain: string;
    h5Domain: string;
    imDomain: string;
    adminDomain: string;
    autoRedirect: number;
    uploadMaxSize: number;
    imageUploadMaxSize: number;
    materialUploadNameType: number;
    clientDefaultUse: number;
    searchKeywords: string;
    msgHackWord: string;
    isOpenPscws: string;
    shopDefaultRegions: string;
    selectSearchType: string;
    defaultCountry: number;
    countries?: Regions[];
    icoDefinedCss: string;
    storageType: number;
    storageSaveFullPath: number;
    storageLocalUrl: string;
    storageOssUrl: string;
    storageOssAccessKeyId: string;
    storageOssAccessKeySecret: string;
    storageOssBucket: string;
    storageOssRegion: string;
    storageCosUrl: string;
    storageCosSecretId: string;
    storageCosSecretKey: string;
    storageCosBucket: string;
    storageCosRegion: string;
    langOn: number;
    langType: number;
    langVolcengineAccessKey: string;
    langVolcengineSecret: string;
    navTheme: string;
    primaryColor: string;
    layout: string;
    adminLightLogo: string | null;
    versionInfoHidden: number;
    seoCategoryEnable: number;
    seoProductEnable: number;
    storageQiniuUrl: string;
    storageQiniuSecretKey: string;
    storageQiniuAccessKey: string;
    storageQiniuBucket: string;
    storageQiniuRegion: string;
    storageS3Url: string;
    storageS3AccessKey: string;
    storageS3SecretKey: string;
    storageS3Bucket: string;
    storageS3Region: string;
    storageS3Endpoint: string;
}

export interface LoginConfig {
    openWechatRegister: number;
    wechatShareNeedLogin: number;
    wechatRegisterBindPhone: number;
    openWechatPcLogin: number;
    openWechatOauth: number;
    usernamePrefix: string;
    openEmailRegister: number;
    isOpenMobileAreaCode: number;
    googleLoginOn: number;
    googleClientId: string;
    googleClientSecret: string;
    facebookLoginOn: number;
    facebookClientId: string;
    facebookClientSecret: string;
    twitterLoginOn: number;
    twitterClientId: string;
    twitterClientSecret: string;
    wechatAppId: string;
    wechatAppSecret: string;
    wechatServerUrl: string;
    wechatServerToken: string;
    wechatServerSecret: string;
}

export interface OrderConfig {
    autoDeliveryDays: number;
    autoReturnGoods: number;
    autoReturnGoodsDays: number;
    afterSalesLimitDays: number;
    /** 售后审核（0关闭/1开启） */
    afterSalesReview: number;
    /** 财务审核（0关闭/1开启） */
    financialReview: number;
    /** 虚拟商品自动发货（0关闭/1开启） */
    virtualOrderAutoDeliver: number;
    /** 买家售后及主动取消未支付订单是否必须选择平台原因（0关闭/1开启） */
    afterSalesReasonEnabled: number;
    /** 平台统一维护的售后/取消原因，按数组顺序展示 */
    afterSalesReasonList: string[];
    autoCancelOrderMinute: number;
    isPlatformCancelPaidOrder: number;
    isPlatformCancelDeliverOrder: number;
    isShopCancelDeliverOrder: number;
    /** 平台代客下单是否允许使用积分（0关闭/1开启） */
    adminShoppingUsePoint: number;
    /** 平台代客下单是否允许使用余额（0关闭/1开启） */
    adminShoppingUseBalance: number;
    // 买家退货多少天后，系统将自动确认收货
    autoConfirmReceiptDays: number;
}

interface BaseApiAppPayConfig {
    wechatPayAppId: string;
    wechatPayAppSecret: string;
}

interface BaseApiCollectionConfig {
    oneboundKey: string;
    oneboundSecret: string | number;
}

interface BaseApiIconConfig {
    icoTigCss: string;
    icoDefinedCss: string;
}

interface BaseApiMiniProgramConfig {
    wechatMiniProgramAppId: string;
    wechatMiniProgramSecret: string;
}

export interface WechatLiveSettingsForm {
    wechatLiveMiniAppId: string;
    wechatLiveMiniAppSecret: string;
}

/** 小程序风控配置（与直播解耦，始终可用） */
export interface MiniProgramRiskSettingsForm {
    miniImgSecCheck?: number;
}

interface BaseApiStorageConfig {
    storageType: number;
    storageLocalUrl: string;
    storageOssUrl: string;
    storageOssAccessKeyId: string;
    storageOssAccessKeySecret: string;
    storageOssBucket: string;
    storageOssRegion: string;
    storageCosUrl: string;
    storageCosSecretId: string;
    storageCosSecretKey: string;
    storageCosBucket: string;
    storageCosRegion: string;
    storageS3Url?: string;
    storageS3AccessKey?: string;
    storageS3SecretKey?: string;
    storageS3Bucket?: string;
    storageS3Region?: string;
    storageS3Endpoint?: string;
    storageSaveFullPath: number;
}

interface BaseApiWechatConfig {
    wechatOauth: number;
    wechatAppId: string;
    wechatAppSecret: string;
    wechatServerUrl: string;
    wechatServerToken: string;
    wechatServerSecret: string;
}

interface BaseApiWechatMerchantConfig {
    wechatPayMchidType: string;
    wechatPayMchid: string;
    wechatPaySubMchid: string;
    wechatPayKey: string;
}

interface BaseDisplayConfig {
    msgHackWord: string;
    isOpenPscws: string;
    selfStoreName: string;
    shopDefaultRegions: string;
    defaultCountry: number;
    showCatLevel: number;
    countries?: Regions[];
}

interface RegionSettingConfig {
    shopDefaultRegions: number[];
    defaultCountry: number;
}

interface BaseKefuConfig {
    kefuType: number;
    kefuYzfType: number;
    kefuYzfSign: string;
    kefuWorkwxId: string;
    kefuCode: string;
    kefuCodeBlank: number;
    corpId: string;
    kefuPhone: string;
    kefuTime: string;
}

interface KefuSettingConfig {
    kefuType: number;
    kefuYzfType: number;
    kefuYzfSign: string;
    kefuWorkwxId: string;
    kefuCode: string;
    kefuCodeBlank: number;
    kefuJavascript: string;
    wapKefuJavascript: string;
    corpId: string;
}

interface KefuInfoConfig {
    kefuPhone: string;
    kefuTime: string;
}

interface BaseMailboxConfig {
    serviceEmail: string;
    sendConfirmEmail: number;
    orderPayEmail: number;
    sendServiceEmail: number;
    sendShipEmail: number;
}

interface BasicProductConfig {
    dollarSign: string;
    dollarSignCn: string;
    snPrefix: string;
    customProductNameLength?: number;
    showSelledCount: number | null;
    showMarketprice: number | null;
    isEnquiry: number | null;
    bulkPurchase: number | null;
    enableAttributeFilter: number | null;
    marketPriceRate: number;
    /** 商品编辑库存是否可修改：1=可修改（默认）；0=不可修改 */
    productEditStockModify?: number;
    /** 规格图片可上传范围：1=仅第一组规格可上传图片（默认）；2=全部规格可上传图片 */
    specImageMode?: number;
    /** 是否开启商品评价：1=开启（默认）；0=关闭 */
    productCommentEnabled?: number;
    /** 是否开启匿名评价：1=开启（默认）；0=关闭。仅在 productCommentEnabled=1 时生效 */
    anonymousCommentEnabled?: number;
}

interface ShowRelatedConfig {
    isShowPriceTrend: number;
    showSelledCount: number | null;
    showMarketprice: number | null;
    isSpeGoodsNumber: number;
    speGoodsNumber1: number;
    speGoodsNumber2: number;
    speGoodsNumber3: number;
    pageSize: number;
    historyNumber: number;
    lyBrandType: string;
    commentDefaultTag: string;
}

interface BaseShoppingConfig {
    childAreaNeedRegion: number | null;
    integralName: string;
    integralScale: string;
    orderSendPoint: string;
    /**
     * 订单赠送积分节点：1-待发货，2-已发货，3-已完成
     */
    orderSendPointNode?: number;
    /**
     * 已完成订单赠送积分延迟天数
     */
    orderCompleteSendPointDays?: string;
    integralPercentBaseAmount: string;
    integralPercent: string;
    commentSendPoint: string;
    showSendPoint: string;
    useQiandaoPoint: number | null;
    continuousSignIn: number | null;
    canInvoice: number | null;
    invoiceAdded: number | null;
    returnConsignee: string;
    returnMobile: string;
    returnAddress: string;
}

interface ShoppingGlobalConfig {
    autoSplitPaidOrder: number;
    childAreaNeedRegion: number;
}

interface SettlementSettingConfig {
    useBonus: number;
    useSurplus: number;
}

interface PointsSettingConfig {
    useIntegral: number;
    integralName: string;
    integralScale: number;
    integralPercent: number;
    commentSendPoint: number;
    showSendPoint: number;
    useQiandaoPoint: number;
    orderSendPoint: number;
}

interface InvoiceSettingConfig {
    canInvoice: number;
    invoiceContent: string;
    invoiceAdded: number;
}

interface ActivitySettingConfig {
    lotteryClosed: number;
    lotteryPoint: number;
    isOpenPin: number;
    isOpenBargain: number;
}

interface AfterSalesSettingConfig {
    returnConsignee: string;
    returnMobile: number;
    returnAddress: string;
}

interface BaseSmsConfig {
    smsKeyId: string;
    smsKeySecret: string;
    smsSignName: string;
    smsShopMobile: string;
}

export interface PaymentFormState {
    useSurplus?: number;
    useCod?: number;
    usePoints?: number;
    useCoupon?: number;
    useWechat?: number;
    useAlipay?: number;
    maxShopCount?: number;
    useOffline?: number;
    useYabanpay?: number;
    useYabanpayWechat?: number;
    useYabanpayAlipay?: number;
    yabanpayCurrency?: string;
    yabanpayCurrencyList?: YaBandPayCurrency[];
    personApplyEnabled?: number;
    merchantApplyNeedCheck?: number;
    shopProductNeedCheck?: number;
    enabledCommissionOrder?: number;
    wechatMchidType?: number;
    wechatPayMchid?: string;
    shopAgreement?: string;
    shopRankDateRage?: string;
    maxRecommendProductCount?: string;
    defaultAdminPrefix?: string;
    wechatPaySubMchid?: string;
    wechatPayKey?: string;
    wechatPaySerialNo?: string;
    maxSubAdministrator?: string;
    alipayAppid?: string;
    alipayRsaPrivateKey?: string;
    alipayRsaPublicKey?: string;
    offlinePayBank?: string;
    offlinePayCompany?: string;
    wechatPayCertificate?: number;
    wechatPayPrivateKey?: number;
    wechatPayPlatformCertificate?: number;
    yabandpayUid?: string;
    yabandpaySecretKey?: number;
    usePaypal?: number;
    paypalClientId?: string;
    paypalSecret?: string;
    paypalCurrency?: string;
    paypalCurrencyList?: PayPalCurrency[];
    defaultShopName?: string;
    useYunpay?: number;
    yunpayUid?: string;
    yunpaySecretKey?: string;
    wechatPayCheckType?: number;
    wechatPayPublicKey?: number;
    wechatPayPublicKeyId?: string;
}

type layoutType = "default" | "side" | "top" | "mix" | "topMenu" | "";
type navThemeType = "light" | "dark" | "realDark" | "";
type ColorType = "blue" | "red" | "green" | "orange" | "purple" | "";

export interface AdminConfigState {
    layout: layoutType;
    navTheme: navThemeType;
    primaryColor: ColorType;
    adminLightLogo: string | null;
    versionInfoHidden: number;
    pageSize: number;
    icoDefinedCss: string;
    dollarSign: string;
    /** 货币中文名（后端全局配置，如 元/美元），跨境版切换主货币后自动同步 */
    dollarSignCn: string;
    storageType: number;
    uploadMaxSize: number;
    imageUploadMaxSize: number;
    clientDefaultUse: number;
    storageUrl: string;
    shopTitle: string;
    version: string;
    pcDomain: string;
    h5Domain: string;
    imDomain: string;
    versionType: string;
    amountUnit: string;
    shopCompany: number;
    shopCompanyTxt: string;
    poweredBy: number;
    poweredByLogo: string;
    poweredByStatus: number;
    defaultTechSupport: string;
    defaultCopyright: string;
    withdrawSettingVO: any;
    /** 是否开启保证金功能 1开启 0关闭 */
    isOpenDeposit?: number;
    /** 店铺客户功能平台总开关（能力门）：1开启 0关闭（由 getAdminBase 下发） */
    isOpenShopMember?: number;
    /** 店铺客户信息脱敏：1开启（店铺端客户电话/姓名脱敏展示）0关闭（由 getAdminBase 下发） */
    shopMemberDesensitize?: number;
    /** 店铺添加客户：1允许店铺自行新增店铺客户 0不允许（由 getAdminBase 下发） */
    shopMemberAllowAdd?: number;
    /** 店铺导入客户：1允许店铺导入自有客户 0不允许（由 getAdminBase 下发） */
    shopMemberAllowImport?: number;
    customProductNameLength?: number;
    /** 商品编辑库存是否可修改：1=可修改（默认）；0=不可修改（由 getAdminBase 下发） */
    productEditStockModify?: number;
    /** 规格图片可上传范围：1=仅第一组规格（默认）；2=全部规格（由 getAdminBase 下发） */
    specImageMode?: number;
    searchSettings: {
        enableAttributeFilter?: number | null;
        selectSearchType?: string;
    };
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

export interface BaseLogistics {
    address?: string;
    areaName?: string;
    cityName?: string;
    kdniaoApiKey?: string;
    kdniaoBusinessId?: string;
    logisticsType?: string;
    mobile?: string;
    provinceName?: string;
    receiveAddress?: string;
    receiveAreaName?: string;
    receiveCityName?: string;
    receiveMobile?: string;
    receiveProvinceName?: string;
    receiveSender?: string;
    sender?: string;
}

/** 短信服务商（与后端 SmsProviderEnum 对齐） */
export type SmsProviderType = 1 | 2 | 3 | 4;

export interface BaseNotice {
    /** 当前启用的短信服务商 */
    smsProvider?: SmsProviderType | string | number | null;
    /** 阿里云 */
    smsKeyId: string;
    smsKeySecret: string;
    smsSignName: string;
    smsShopMobile: string;
    /** 腾讯云 */
    smsTencentSecretId?: string;
    smsTencentSecretKey?: string;
    smsTencentSdkAppId?: string;
    smsTencentSignName?: string;
    smsTencentShopMobile?: string;
    /** 华为云 */
    smsHuaweiAppKey?: string;
    smsHuaweiAppSecret?: string;
    smsHuaweiChannel?: string;
    smsHuaweiSignName?: string;
    smsHuaweiEndpoint?: string;
    smsHuaweiShopMobile?: string;
    /** 火山引擎 */
    smsVolcengineAccessKey?: string;
    smsVolcengineSecretKey?: string;
    smsVolcengineSmsAccount?: string;
    smsVolcengineSignName?: string;
    smsVolcengineShopMobile?: string;
    /** 阿里云短信通道开关（仅跨境版；1=+86国内、2=其他海外，多选英文逗号分隔） */
    smsAliyunChannel?: string;
    /** 阿里云国际短信发送方标识 SenderID（仅跨境版；SendMessageToGlobe 的 From） */
    smsSenderId?: string;
    serviceEmail: string;
    sendConfirmEmail: number;
    orderPayEmail: number;
    sendServiceEmail: number;
    sendShipEmail: number;
    mailService?: number;
    smtpSsl?: number;
    smtpHost?: string;
    smtpPort?: string;
    smtpUser?: string;
    smtpPass?: string;
    smtpMail?: string;
    mailCharset?: string;
    testMailAddress?: string;
}

export interface BaseInterfactSettings {
    wechatOauth: number;
    wechatAppId: string;
    wechatAppSecret: string;
    wechatServerUrl: string;
    wechatServerToken: string;
    wechatServerSecret: string;
}

export interface BaseAuthenticationSettings {
    type: number;
    smsNote: number;
    tips: string;
    isIdentity: number;
    isEnquiry: number;
    closeAuth: number;
    bulkPurchase: number;
}

export interface BaseAuthenticationSettings {
    type: number;
    smsNote: number;
    tips: string;
    isIdentity: number;
    isEnquiry: number;
}

export interface BaseCategoryDecorate {
    productCategoryDecorateType: number;
}

export interface BaseProductDetailStyle {
    productDetailStyleType: number;
}

export interface BaseSearchStyle {
    searchStyleType: number;
}

export interface BaseThemeStyle {
    themeId?: number;
    themeStyle?: string;
}

export interface BaseTheme {
    layout: layoutType;
    navTheme: navThemeType;
    primaryColor: ColorType;
    // uniqueOpened: boolean;
    // isMultiLabel: boolean;
}

export interface PersonalizedFormState {
    guestLikeGoodsName: string;
    isGuestLikeGoods: number;
    defaultAvatar: string;
    searchSelfStore: number;
    indexSearchShop: number;
}

/**
 * AmapAllVO，地图设置参数
 */
export interface AmapAllResponse {
    /**
     * 高德地图Key
     */
    amapJsKey?: string;
    /**
     * 高德地图SECRET
     */
    amapJsSecret?: string;
    /**
     * 高德地图Key
     */
    amapMiniKey?: string;
    /**
     * 高德地图SECRET
     */
    amapMiniSecret?: string;
    /**
     * 高德地图Key
     */
    amapWebKey?: string;
    /**
     * 高德地图SECRET
     */
    amapWebSecret?: string;
    [property: string]: any;
}
