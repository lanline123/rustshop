package com.tigshop.bean.enums.setting;

import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础配置枚举
 *
 * @author kidd
 * @since 2025/4/1 15:45
 */
@Getter
@AllArgsConstructor
public enum SettingsEnum {

    // *** 商城设置-基础信息 ***

    // *** 商城信息 ***
    SHOP_LOGO("shopLogo", "商城LOGO", ValueTypeEnum.STRING, null),
    LIGHT_SHOP_LOGO("lightShopLogo", "lightShopLogo", ValueTypeEnum.STRING, null),
    SHOP_NAME("shopName", "商城简称", ValueTypeEnum.STRING, null),
    SHOP_COMPANY("shopCompany", "版权信息; 1-系统默认，0-自定义", ValueTypeEnum.INTEGER, "1"),
    DEFAULT_COPYRIGHT("defaultCopyright", "默认版权", ValueTypeEnum.STRING, "© 2018 江西佰商科技版权所有"),
    SHOP_COMPANY_TXT("shopCompanyTxt", "版权信息自定义值", ValueTypeEnum.STRING, null),
    POWERED_BY_STATUS("poweredByStatus", "标题栏版权隐藏；1-隐藏，0-显示", ValueTypeEnum.INTEGER, null),
    POWERED_BY("poweredBy", "技术支持标识；1-系统默认，0-自定义", ValueTypeEnum.INTEGER, "1"),
    POWERED_BY_LOGO("poweredByLogo", "技术支持标识logo", ValueTypeEnum.STRING, null),
    KEFU_ADDRESS("kefuAddress", "企业地址", ValueTypeEnum.STRING, null),

    // *** 备案信息 ***
    SHOP_ICP_NO("shopIcpNo", "ICP备案号", ValueTypeEnum.STRING, null),
    SHOP_ICP_NO_URL("shopIcpNoUrl", "ICP备案链接", ValueTypeEnum.STRING, null),
    SHOP_110_NO("shop110No", "公安备案号", ValueTypeEnum.STRING, null),
    SHOP_110_LINK("shop110Link", "公安备案链接", ValueTypeEnum.STRING, null),

    // *** 经营许可证 ***
    BUSINESS_LICENSE("businessLicense", "增值电信业务经营许可证(ICP)证号", ValueTypeEnum.STRING, null),
    EDI_LICENSE("ediLicense", "EDI经营许可证号", ValueTypeEnum.STRING, null),
    BUSINESS_LICENSE_TYPE("businessLicenseType", "经营许可证类型，可多选：icp、edi", ValueTypeEnum.JSON_ARRAY, "[]"),
    BUSINESS_LICENSE_ENABLED("businessLicenseEnabled", "经营许可证显示开关；1-开启，0-关闭", ValueTypeEnum.INTEGER, "1"),

    // *** 商城状态 ***
    CLOSE_ORDER("closeOrder", "关闭下单；0-否，1-是", ValueTypeEnum.INTEGER, null),
    SHOP_REG_CLOSED("shopRegClosed", "关闭注册；0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** 商城设置-商品设置 ***
    ENABLE_ATTRIBUTE_FILTER("enableAttributeFilter", "属性筛选：0表示关闭属性筛选，1表示开启属性筛选", ValueTypeEnum.INTEGER, "1"),
    PRODUCT_EDIT_STOCK_MODIFY("productEditStockModify", "商品编辑库存是否可修改；0-否，1-是", ValueTypeEnum.INTEGER, "1"),
    SPEC_IMAGE_MODE("specImageMode", "规格图片上传范围；1-仅第一组规格可上传图片，2-全部规格可上传图片", ValueTypeEnum.INTEGER, "1"),

    // *** 商品货币设置 ***
    DOLLAR_SIGN("dollarSign", "货币符号", ValueTypeEnum.STRING, null),
    DOLLAR_SIGN_CN("dollarSignCn", "商品货币中文", ValueTypeEnum.STRING, null),

    // *** 商品录入设置 ***
    SN_PREFIX("snPrefix", "商品货号前缀", ValueTypeEnum.STRING, null),
    MARKET_PRICE_RATE("marketPriceRate", "市场价换算", ValueTypeEnum.DECIMAL, null),

    // *** 商品名称长度设置 ***
    CUSTOM_PRODUCT_NAME_LENGTH("customProductNameLength", "自定义商品名称长度", ValueTypeEnum.INTEGER, "200"),
    // *** 商品信息显示设置 ***
    SHOW_SELLED_COUNT("showSelledCount", "显示销量; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    SHOW_MARKETPRICE("showMarketprice", "显示市场价; 0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** 商品评价设置 ***
    PRODUCT_COMMENT_ENABLED("productCommentEnabled", "是否开启商品评价; 0-关闭，1-开启", ValueTypeEnum.INTEGER, "1"),
    ANONYMOUS_COMMENT_ENABLED("anonymousCommentEnabled", "是否开启匿名评价; 0-关闭，1-开启", ValueTypeEnum.INTEGER, "1"),

    // *** 商城设置-交易设置 ***

    // *** 购物全局设置 ***
    CHILD_AREA_NEED_REGION("childAreaNeedRegion", "运费模板地区设置机制；0-未设置的地区皆可配送（使用默认运费设置），1-仅设置的地区可配送", ValueTypeEnum.INTEGER, null),

    // *** 积分设置 ***
    INTEGRAL_NAME("integralName", "积分名称", ValueTypeEnum.STRING, null),
    INTEGRAL_SCALE("integralScale", "积分换算比例", ValueTypeEnum.STRING, null),
    ORDER_SEND_POINT("orderSendPoint", "下单送积分", ValueTypeEnum.STRING, null),
    ORDER_SEND_POINT_NODE("orderSendPointNode", "下单送积分节点；1-待发货，2-已发货，3-已完成", ValueTypeEnum.INTEGER, "1"),
    ORDER_COMPLETE_SEND_POINT_DAYS("orderCompleteSendPointDays", "会员订单已完成后多少天赠送积分；0表示立即赠送，大于0表示延迟N天后赠送", ValueTypeEnum.INTEGER, "0"),
    INTEGRAL_PERCENT("integralPercent", "积分支付比例", ValueTypeEnum.STRING, null),
    INTEGRAL_PERCENT_BASE_AMOUNT("integralPercentBaseAmount", "积分支付比例金额基数", ValueTypeEnum.STRING, "100"),
    COMMENT_SEND_POINT("commentSendPoint", "评论商品送积分", ValueTypeEnum.STRING, null),
    SHOW_SEND_POINT("showSendPoint", "晒单商品送积分", ValueTypeEnum.STRING, null),
    USE_QIANDAO_POINT("useQiandaoPoint", "签到赠送积分; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    CONTINUOUS_SIGN_IN("continuousSignIn", "连续签到；0-关闭，1-开启，漏签时重置签到天数", ValueTypeEnum.INTEGER, "0"),

    // *** 发票设置 ***
    CAN_INVOICE("canInvoice", "是否能开发票; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    INVOICE_ADDED("invoiceAdded", "是否支持增值税专用发票; 0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** 退换货设置 ***
    RETURN_CONSIGNEE("returnConsignee", "回寄联系人设置", ValueTypeEnum.STRING, null),
    RETURN_MOBILE("returnMobile", "回寄电话设置", ValueTypeEnum.STRING, null),
    RETURN_ADDRESS("returnAddress", "回寄地址设置", ValueTypeEnum.STRING, null),

    // *** 商城设置-订单设置 ***

    AUTO_DELIVERY_DAYS("autoDeliveryDays", "自动收货天数", ValueTypeEnum.DECIMAL, null),
    AUTO_RETURN_GOODS("autoReturnGoods", "自动同意退货选项; 1-开启，0-关闭", ValueTypeEnum.INTEGER, null),
    AUTO_RETURN_GOODS_DAYS("autoReturnGoodsDays", "自动同意退天数", ValueTypeEnum.DECIMAL, null),
    AUTO_CONFIRM_RECEIPT_DAYS("autoConfirmReceiptDays", "买家退货多少天后，系统将自动确认收货", ValueTypeEnum.DECIMAL, null),
    AFTER_SALES_LIMIT_DAYS("afterSalesLimitDays", "售后限制天数", ValueTypeEnum.DECIMAL, null),
    AUTO_CANCEL_ORDER_MINUTE("autoCancelOrderMinute", "未付款订单超时时间", ValueTypeEnum.INTEGER, null),
    IS_PLATFORM_CANCEL_PAID_ORDER("isPlatformCancelPaidOrder", "平台已支付订单可取消订单", ValueTypeEnum.INTEGER, "0"),
    IS_PLATFORM_CANCEL_DELIVER_ORDER("isPlatformCancelDeliverOrder", "平台已发货订单可取消发货", ValueTypeEnum.INTEGER, "0"),
    IS_SHOP_CANCEL_DELIVER_ORDER("isShopCancelDeliverOrder", "店铺已发货订单可取消发货", ValueTypeEnum.INTEGER, "0"),
    AFTER_SALES_REVIEW("afterSalesReview", "售后审核; 0-关闭，1-开启", ValueTypeEnum.INTEGER, "1"),
    FINANCIAL_REVIEW("financialReview", "财务审核; 0-关闭，1-开启", ValueTypeEnum.INTEGER, "1"),
    VIRTUAL_ORDER_AUTO_DELIVER("virtualOrderAutoDeliver", "虚拟商品自动发货; 0-关闭，1-开启", ValueTypeEnum.INTEGER, "0"),
    AFTER_SALES_REASON_ENABLED("afterSalesReasonEnabled", "买家售后与取消订单原因开关; 0-关闭，1-开启", ValueTypeEnum.INTEGER, "1"),
    AFTER_SALES_REASON_LIST("afterSalesReasonList", "买家售后与取消订单共用原因列表", ValueTypeEnum.JSON_ARRAY,
            "[\"多拍/拍错/不喜欢\",\"未按约定时间发货\",\"协商一致退款\",\"地址/电话填错了\",\"其他\"]"),

    // *** 供应链采购设置 ***
    PURCHASE_AUTO_FINISH_DAYS("purchaseAutoFinishDays", "预计送达后自动关闭或完结天数；空值表示关闭定时处理", ValueTypeEnum.INTEGER, null),
    PURCHASE_ALLOW_OVER_RECEIVE("purchaseAllowOverReceive", "采购入库是否允许超收；0-关闭，1-开启", ValueTypeEnum.INTEGER, "1"),


    //代客下单设置
    ADMIN_SHOPPING_USE_POINT("adminShoppingUsePoint", "管理员购物是否使用积分; 0-否，1-是", ValueTypeEnum.INTEGER, "0"),
    ADMIN_SHOPPING_USE_BALANCE("adminShoppingUseBalance", "管理员购物是否使用余额; 0-否，1-是", ValueTypeEnum.INTEGER, "0"),

    // *** 商城设置-客服设置 ***

    // *** 客服设置 ***
    KEFU_TYPE("kefuType", "客服选择；0-无客服，1-腾讯云客服，2-企业微信客服，3-自定义客服，4-TigChart客服，5-WhatsApp客服", ValueTypeEnum.INTEGER, null),
    KEFU_YZF_TYPE("kefuYzfType", "腾讯云客服打开方式；0-打开链接地址，1-小弹窗打开", ValueTypeEnum.INTEGER, null),
    KEFU_YZF_SIGN("kefuYzfSign", "腾讯云客服sign", ValueTypeEnum.STRING, null),
    KEFU_WORKWX_ID("kefuWorkwxId", "企业微信客服ID", ValueTypeEnum.STRING, null),
    CORP_ID("corpId", "企业微信企业ID", ValueTypeEnum.STRING, null),
    KEFU_CODE("kefuCode", "客服链接", ValueTypeEnum.STRING, null),
    KEFU_CODE_BLANK("kefuCodeBlank", "客服链接打开方式；0-打开链接地址，1-小弹窗打开", ValueTypeEnum.INTEGER, null),

    // *** 客服信息 ***
    KEFU_PHONE("kefuPhone", "客服热线电话", ValueTypeEnum.STRING, null),
    KEFU_TIME("kefuTime", "服务时间", ValueTypeEnum.STRING, null),

    // *** 个性化设置 ***
    // 猜你喜欢
    GUEST_LIKE_GOODS_NAME("guestLikeGoodsName", "猜你喜欢自定义名称", ValueTypeEnum.STRING, null),
    IS_GUEST_LIKE_GOODS("isGuestLikeGoods", "是否启用猜你喜欢", ValueTypeEnum.INTEGER, "1"),

    // *** 配送设置-配送设置 ***

    // *** 快递发货设置 ***
    DEFAULT_LOGISTICS_NAME("defaultLogisticsName", "默认配送名称", ValueTypeEnum.STRING, null),

    // *** 快递接口设置 ***
    LOGISTICS_PROVIDER("logisticsProvider", "物流服务商；1-快递鸟，2-快递100", ValueTypeEnum.INTEGER, "1"),
    KDNIAO_API_KEY("kdniaoApiKey", "快递鸟API Key", ValueTypeEnum.STRING, null),
    KDNIAO_BUSINESS_ID("kdniaoBusinessId", "快递鸟服务ID", ValueTypeEnum.STRING, null),
    KUAIDI100_CUSTOMER("kuaidi100Customer", "快递100授权码(customer)", ValueTypeEnum.STRING, null),
    KUAIDI100_KEY("kuaidi100Key", "快递100授权key", ValueTypeEnum.STRING, null),
    KUAIDI100_SECRET("kuaidi100Secret", "快递100授权secret（电子面单签名用）", ValueTypeEnum.STRING, null),
    KUAIDI100_SIID("kuaidi100Siid", "快递100云打印机设备码（printType=CLOUD时必填）", ValueTypeEnum.STRING, null),
    KUAIDI100_PRINT_TYPE("kuaidi100PrintType", "快递100电子面单打印类型；NON/IMAGE/HTML/CLOUD，默认HTML", ValueTypeEnum.STRING, "HTML"),

    // *** 发货信息设置 ***
    SENDER("sender", "发货人", ValueTypeEnum.STRING, null),
    MOBILE("mobile", "发货人联系方式", ValueTypeEnum.STRING, null),
    PROVINCE_NAME("provinceName", "发货地省份", ValueTypeEnum.STRING, null),
    CITY_NAME("cityName", "发货城市", ValueTypeEnum.STRING, null),
    AREA_NAME("areaName", "发货地区", ValueTypeEnum.STRING, null),
    ADDRESS("address", "发货详细地址", ValueTypeEnum.STRING, null),
    RECEIVE_SENDER("receiveSender", "收货人", ValueTypeEnum.STRING, null),
    RECEIVE_MOBILE("receiveMobile", "收货人联系方式", ValueTypeEnum.STRING, null),
    RECEIVE_PROVINCE_NAME("receiveProvinceName", "收货地省份", ValueTypeEnum.STRING, null),
    RECEIVE_CITY_NAME("receiveCityName", "收货城市", ValueTypeEnum.STRING, null),
    RECEIVE_AREA_NAME("receiveAreaName", "收货地区", ValueTypeEnum.STRING, null),
    RECEIVE_ADDRESS("receiveAddress", "收货详细地址", ValueTypeEnum.STRING, null),

    // *** 系统设置-全局设置 ***

    // *** 后台风格 ***
    NAV_THEME("navTheme", "外观", ValueTypeEnum.STRING, null),
    PRIMARY_COLOR("primaryColor", "主题色", ValueTypeEnum.STRING, null),
    LAYOUT("layout", "导航模式", ValueTypeEnum.STRING, null),
    ADMIN_LIGHT_LOGO("adminLightLogo", "后台LOGO", ValueTypeEnum.STRING, null),
    VERSION_INFO_HIDDEN("versionInfoHidden", "是否隐藏版本信息; 0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** SEO 设置 ***
    SEO_PRODUCT_ENABLE("seoProductEnable", "是否开启商品SEO", ValueTypeEnum.INTEGER, null),
    SEO_CATEGORY_ENABLE("seoCategoryEnable", "是否开启商品类目SEO", ValueTypeEnum.INTEGER, null),
    SHOP_TITLE_SUFFIX("shopTitleSuffix", "SEO标题后缀", ValueTypeEnum.STRING, null),
    SHOP_TITLE("shopTitle", "首页SEO标题", ValueTypeEnum.STRING, null),
    SHOP_KEYWORDS("shopKeywords", "首页SEO关键词", ValueTypeEnum.STRING, null),
    SHOP_DESC("shopDesc", "首页SEO描述", ValueTypeEnum.STRING, null),
    ICO_IMG("icoImg", "商城ico", ValueTypeEnum.STRING, null),
    DEFAULT_AVATAR("defaultAvatar", "会员默认头像", ValueTypeEnum.STRING, null),

    // *** 域名设置 ***
    CLIENT_DEFAULT_USE("clientDefaultUse", "前台默认跳转", ValueTypeEnum.INTEGER, null),
    PC_DOMAIN("pcDomain", "PC端域名", ValueTypeEnum.STRING, null),
    H5_DOMAIN("h5Domain", "H5端域名", ValueTypeEnum.STRING, null),
    ADMIN_DOMAIN("adminDomain", "后台域名", ValueTypeEnum.STRING, null),
    AUTO_REDIRECT("autoRedirect", "自动跳转H5；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),

    // *** 上传设置 ***
    UPLOAD_MAX_SIZE("uploadMaxSize", "上传文件最大尺寸，单位：MB", ValueTypeEnum.INTEGER, null),
    IMAGE_UPLOAD_MAX_SIZE("imageUploadMaxSize", "图片上传最大尺寸，单位：MB", ValueTypeEnum.DECIMAL, "20"),
    MATERIAL_UPLOAD_NAME_TYPE("materialUploadNameType", "素材上传命名方式；0-系统随机命名，1-保留原素材名", ValueTypeEnum.INTEGER, "0"),

    // *** 搜索设置 ***
    SEARCH_KEYWORDS("searchKeywords", "搜索热门关键字", ValueTypeEnum.STRING, null),
    MSG_HACK_WORD("msgHackWord", "敏感词屏蔽", ValueTypeEnum.STRING, null),
    IS_OPEN_PSCWS("isOpenPscws", "搜索关键词分词；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    SELECT_SEARCH_TYPE("selectSearchType", "搜索方式；es 或 mysql", ValueTypeEnum.STRING, "mysql"),

    // *** 地区设置 ***
    SHOP_DEFAULT_REGIONS("shopDefaultRegions", "商城默认地区", ValueTypeEnum.STRING, null),
    DEFAULT_COUNTRY("defaultCountry", "默认国家", ValueTypeEnum.INTEGER, null),

    // *** ICO 图标库 ***
    ICO_DEFINED_CSS("icoDefinedCss", "自定义ico图标库", ValueTypeEnum.STRING, null),

    // *** 存储设置 ***
    STORAGE_TYPE("storageType", "存储类型；0-本地存储，1-阿里云 OSS 存储，2-腾讯云 COS 存储，3-七牛云 Kodo 存储", ValueTypeEnum.INTEGER, null),
    STORAGE_LOCAL_URL("storageLocalUrl", "本地 图片访问域名", ValueTypeEnum.STRING, null),
    STORAGE_OSS_URL("storageOssUrl", "OSS 图片访问域名", ValueTypeEnum.STRING, null),
    STORAGE_OSS_ACCESS_KEY_ID("storageOssAccessKeyId", "OSS AccessKeyId", ValueTypeEnum.STRING, null),
    STORAGE_OSS_ACCESS_KEY_SECRET("storageOssAccessKeySecret", "OSS AccessKeySecret", ValueTypeEnum.STRING, null),
    STORAGE_OSS_BUCKET("storageOssBucket", "OSS 空间名称", ValueTypeEnum.STRING, null),
    STORAGE_OSS_REGION("storageOssRegion", "OSS 空间地域节点", ValueTypeEnum.STRING, null),
    STORAGE_COS_URL("storageCosUrl", "COS 图片访问域名", ValueTypeEnum.STRING, null),
    STORAGE_COS_SECRET_ID("storageCosSecretId", "COS SecretId", ValueTypeEnum.STRING, null),
    STORAGE_COS_SECRET_KEY("storageCosSecretKey", "COS KeySecret", ValueTypeEnum.STRING, null),
    STORAGE_COS_BUCKET("storageCosBucket", "COS 空间名称", ValueTypeEnum.STRING, null),
    STORAGE_COS_REGION("storageCosRegion", "COS 空间地域节点", ValueTypeEnum.STRING, null),
    STORAGE_QINIU_URL("storageQiniuUrl", "七牛云 图片访问域名", ValueTypeEnum.STRING, null),
    STORAGE_QINIU_ACCESS_KEY("storageQiniuAccessKey", "七牛云 AccessKey", ValueTypeEnum.STRING, null),
    STORAGE_QINIU_SECRET_KEY("storageQiniuSecretKey", "七牛云 SecretKey", ValueTypeEnum.STRING, null),
    STORAGE_QINIU_BUCKET("storageQiniuBucket", "七牛云 空间名称", ValueTypeEnum.STRING, null),
    STORAGE_QINIU_REGION("storageQiniuRegion", "七牛云 空间地域节点", ValueTypeEnum.STRING, null),
    STORAGE_S3_URL("storageS3Url", "AWS S3 图片访问域名", ValueTypeEnum.STRING, null),
    STORAGE_S3_ACCESS_KEY("storageS3AccessKey", "AWS S3 AccessKey", ValueTypeEnum.STRING, null),
    STORAGE_S3_SECRET_KEY("storageS3SecretKey", "AWS S3 SecretKey", ValueTypeEnum.STRING, null),
    STORAGE_S3_BUCKET("storageS3Bucket", "AWS S3 存储桶名称", ValueTypeEnum.STRING, null),
    STORAGE_S3_REGION("storageS3Region", "AWS S3 区域", ValueTypeEnum.STRING, null),
    STORAGE_S3_ENDPOINT("storageS3Endpoint", "AWS S3 Endpoint(可选,用于S3兼容存储)", ValueTypeEnum.STRING, null),

    // *** 多语言设置 ***
    LANG_ON("langOn", "是否开启多语言；0-否，1-是", ValueTypeEnum.INTEGER, null),
    LANG_TYPE("langType", "接口翻译", ValueTypeEnum.INTEGER, null),
    LANG_VOLCENGINE_ACCESS_KEY("langVolcengineAccessKey", "火山翻译AssessKey", ValueTypeEnum.STRING, null),
    LANG_VOLCENGINE_SECRET("langVolcengineSecret", "火山翻译SecretKey", ValueTypeEnum.STRING, null),

    // *** WorkBuddy MCP 设置 ***
    WORKBUDDY_API_KEY("workbuddyApiKey", "WorkBuddy API 密钥", ValueTypeEnum.STRING, null),
    WORKBUDDY_API_KEY_EXPIRES_AT("workbuddyApiKeyExpiresAt", "WorkBuddy API 密钥有效期（Unix 秒）", ValueTypeEnum.LONG, null),

    // *** AI图片处理（蜂鸟AI） ***

    // *** 多币种汇率自动更新（Open Exchange Rates，D1124）***

    // *** 系统设置-登录设置 ***

    // *** 微信登录 ***
    OPEN_WECHAT_OAUTH("openWechatOauth", "开启公众号授权登录；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    WECHAT_APP_ID("wechatAppId", "微信APPID", ValueTypeEnum.STRING, null),
    WECHAT_APP_SECRET("wechatAppSecret", "微信AppSecret", ValueTypeEnum.STRING, null),
    WECHAT_SERVER_TOKEN("wechatServerToken", "微信服务器Token", ValueTypeEnum.STRING, null),
    WECHAT_SERVER_SECRET("wechatServerSecret", "微信服务器消息加密密钥", ValueTypeEnum.STRING, null),
    OPEN_WECHAT_REGISTER("openWechatRegister", "是否开启小程序快捷登录；0-否，1-是", ValueTypeEnum.INTEGER, null),
    WECHAT_SHARE_NEED_LOGIN("wechatShareNeedLogin", "微信小程序分享是否需要登录；0-否，1-是", ValueTypeEnum.INTEGER, "0"),
    WECHAT_REGISTER_BIND_PHONE("wechatRegisterBindPhone", "微信注册是否绑定手机；0-否，1-是", ValueTypeEnum.INTEGER, null),
    OPEN_EMAIL_REGISTER("openEmailRegister", "开启邮箱注册；0-否，1-是", ValueTypeEnum.INTEGER, null),
    OPEN_WECHAT_PC_LOGIN("openWechatPcLogin", "是否开启微信注册；0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** Facebook 登录 ***
    FACEBOOK_LOGIN_ON("facebookLoginOn", "是否开启Facebook登陆；0-否，1-是", ValueTypeEnum.INTEGER, null),
    FACEBOOK_CLIENT_ID("facebookClientId", "应用APP ID", ValueTypeEnum.STRING, null),
    FACEBOOK_CLIENT_SECRET("facebookClientSecret", "应用APP SECRET", ValueTypeEnum.STRING, null),

    // *** Google 登录 ***
    GOOGLE_LOGIN_ON("googleLoginOn", "是否开启Google登陆；0-否，1-是", ValueTypeEnum.INTEGER, null),
    GOOGLE_CLIENT_ID("googleClientId", "应用APP ID", ValueTypeEnum.STRING, null),
    GOOGLE_CLIENT_SECRET("googleClientSecret", "应用APP SECRET", ValueTypeEnum.STRING, null),

    // *** X / Twitter 登录 ***
    TWITTER_LOGIN_ON("twitterLoginOn", "是否开启X/Twitter登陆；0-否，1-是", ValueTypeEnum.INTEGER, null),
    TWITTER_CLIENT_ID("twitterClientId", "应用Client ID", ValueTypeEnum.STRING, null),
    TWITTER_CLIENT_SECRET("twitterClientSecret", "应用Client Secret", ValueTypeEnum.STRING, null),

    // *** 基础设置 ***
    IS_OPEN_MOBILE_AREA_CODE("isOpenMobileAreaCode", "是否开启手机区号选择；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    USERNAME_PREFIX("usernamePrefix", "自动注册用户名前缀", ValueTypeEnum.STRING, null),

    // *** 系统设置-支付设置 ***

    // *** 基础设置 ***
    USE_SURPLUS("useSurplus", "是否启用余额支付；0-不支持，1-支持", ValueTypeEnum.INTEGER, null),
    USE_POINTS("usePoints", "是否启用积分支付；0-不支持，1-支持", ValueTypeEnum.INTEGER, null),
    USE_COUPON("useCoupon", "是否启用优惠券；0-不支持，1-支持", ValueTypeEnum.INTEGER, null),
    PAYMENT_ORDER("paymentOrder", "支付顺序，多个用逗号隔开，如：wechat,alipay,paypal,stripe,huifu,yabanpayWechat,yabanpayAlipay,yunpayWechat,yunpayAlipay,offline（huifu 为汇付父级，映射 huifuWechat/huifuAlipay）", ValueTypeEnum.STRING, null),
    USE_CREDIT("useCredit", "是否启用账期支付；0-不支持，1-支持", ValueTypeEnum.INTEGER, null),

    // *** 微信支付设置 ***
    USE_WECHAT("useWechat", "是否启用微信支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    WECHAT_MCHID_TYPE("wechatMchidType", "微信商户号类型；1-普通商户模式，2-服务商模式", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_MCHID("wechatPayMchid", "微信商户号", ValueTypeEnum.STRING, null),
    WECHAT_PAY_SUB_MCHID("wechatPaySubMchid", "微信子商户号", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_KEY("wechatPayKey", "商户API密钥", ValueTypeEnum.STRING, null),
    WECHAT_PAY_V2_KEY("wechatPayV2Key", "商户APIV2密钥", ValueTypeEnum.STRING, null),
    WECHAT_PAY_SERIAL_NO("wechatPaySerialNo", "商户证书序列号", ValueTypeEnum.STRING, null),
    WECHAT_PAY_CERTIFICATE("wechatPayCertificate", "商户API证书", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_PRIVATE_KEY("wechatPayPrivateKey", "商户API证书密钥", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_CHECK_TYPE("wechatPayCheckType", "验证微信支付方式；1-平台证书，2-微信支付公钥", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_PLATFORM_CERTIFICATE("wechatPayPlatformCertificate", "平台证书", ValueTypeEnum.INTEGER, null),
    WECHAT_PAY_PUBLIC_KEY_ID("wechatPayPublicKeyId", "微信支付公钥ID", ValueTypeEnum.STRING, null),
    WECHAT_PAY_PUBLIC_KEY("wechatPayPublicKey", "微信支付公钥文件", ValueTypeEnum.INTEGER, null),

    // *** 微信小程序支付 ***
    WECHAT_MINI_PROGRAM_APP_ID("wechatMiniProgramAppId", "微信小程序AppID", ValueTypeEnum.STRING, null),
    WECHAT_MINI_PROGRAM_SECRET("wechatMiniProgramSecret", "微信小程序AppSecret", ValueTypeEnum.STRING, null),

    // *** 小程序头像图片内容安全（鉴黄鉴违）***
    MINI_IMG_SEC_CHECK("miniImgSecCheck", "小程序头像图片鉴黄鉴违；0-关闭，1-开启", ValueTypeEnum.INTEGER, "0"),

    // *** 微信 APP 支付 ***
    WECHAT_PAY_APP_ID("wechatPayAppId", "应用APP ID", ValueTypeEnum.STRING, null),
    WECHAT_PAY_APP_SECRET("wechatPayAppSecret", "应用APP SECRET", ValueTypeEnum.STRING, null),

    // *** 支付宝支付设置 ***
    USE_ALIPAY("useAlipay", "是否启用支付宝支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    ALIPAY_APPID("alipayAppid", "支付宝APPID", ValueTypeEnum.STRING, null),
    ALIPAY_MOBILE_APPID("alipayMobileAppid", "支付宝移动端AppID", ValueTypeEnum.STRING, null),
    ALIPAY_RSA_PRIVATE_KEY("alipayRsaPrivateKey", "应用私钥", ValueTypeEnum.STRING, null),
    ALIPAY_RSA_PUBLIC_KEY("alipayRsaPublicKey", "支付宝公钥", ValueTypeEnum.STRING, null),

    // *** 易班支付设置 ***
    USE_YABANPAY("useYabanpay", "是否启用YaBand支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    USE_YABANPAY_WECHAT("useYabanpayWechat", "是否启用YaBand微信支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    USE_YABANPAY_ALIPAY("useYabanpayAlipay", "是否启用YaBand支付宝支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    YABANPAY_CURRENCY("yabanpayCurrency", "YaBand支付货币类型", ValueTypeEnum.STRING, null),
    YABANPAY_UID("yabandpayUid", "YaBand支付UID", ValueTypeEnum.STRING, null),
    YABANPAY_SECRET_KEY("yabandpaySecretKey", "YaBand支付密钥", ValueTypeEnum.STRING, null),

    // *** 线下支付设置 ***
    USE_OFFLINE("useOffline", "是否启用线下支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    OFFLINE_PAY_BANK_ENABLE("offlinePayBankEnable", "银行汇款是否启用(0-关闭，1-开启)", ValueTypeEnum.INTEGER, null),
    OFFLINE_PAY_BANK("offlinePayBank", "银行汇款", ValueTypeEnum.STRING, null),
    OFFLINE_PAY_COMPANY_ENABLE("offlinePayCompanyEnable", "企业汇款是否启用(0-关闭，1-开启)", ValueTypeEnum.INTEGER, null),
    OFFLINE_PAY_COMPANY("offlinePayCompany", "企业汇款", ValueTypeEnum.STRING, null),
    THIRD_PARTY_PAYMENT_TRANSFER_ENABLE("thirdPartyPaymentTransferEnable", "第三方支付转账是否启用(0-关闭，1-开启)", ValueTypeEnum.INTEGER, null),
    THIRD_PARTY_PAYMENT_TRANSFER("thirdPartyPaymentTransfer", "第三方支付转账", ValueTypeEnum.STRING, null),
    OTHER_PAYMENT_REMITTANCE_ENABLE("otherPaymentRemittanceEnable", "其他支付转账是否启用(0-关闭，1-开启)", ValueTypeEnum.INTEGER, null),
    OTHER_PAYMENT_REMITTANCE("otherPaymentRemittance", "其他支付转账", ValueTypeEnum.STRING, null),

    // *** Paypal 支付设置 ***
    USE_PAYPAL("usePaypal", "是否启用PayPal支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    PAYPAL_CURRENCY("paypalCurrency", "PayPal货币类型", ValueTypeEnum.STRING, null),
    PAYPAL_CLIENT_ID("paypalClientId", "PayPal客户端ID", ValueTypeEnum.STRING, null),
    PAYPAL_SECRET("paypalClientSecret", "PayPal密钥", ValueTypeEnum.STRING, null),

    // *** Stripe 支付设置 ***
    USE_STRIPE("useStripe", "是否启用Stripe支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    STRIPE_CURRENCY("stripeCurrency", "Stripe货币类型", ValueTypeEnum.STRING, "usd"),
    STRIPE_SECRET_KEY("stripeSecretKey", "Stripe密钥", ValueTypeEnum.STRING, null),
    STRIPE_PUBLISHABLE_KEY("stripePublishableKey", "Stripe公钥", ValueTypeEnum.STRING, null),
    STRIPE_WEBHOOK_SECRET("stripeWebhookSecret", "Stripe Webhook密钥", ValueTypeEnum.STRING, null),
    STRIPE_CURRENCY_LIST("stripeCurrencyList", "Stripe货币列表", ValueTypeEnum.STRING, ""),

    // *** Apple Pay 苹果支付设置（全版本可见，收单通道待选型） ***
    USE_APPLE_PAY("useApplePay", "是否启用Apple Pay支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, "0"),
    APPLE_PAY_MERCHANT_ID("applePayMerchantId", "Apple商户标识（Merchant ID，形如 merchant.com.xxx）", ValueTypeEnum.STRING, null),
    APPLE_PAY_DISPLAY_NAME("applePayDisplayName", "Apple Pay支付面板展示的商户名称", ValueTypeEnum.STRING, null),
    APPLE_PAY_COUNTRY_CODE("applePayCountryCode", "收单商户注册地国家码（ISO 3166 两位）", ValueTypeEnum.STRING, "CN"),
    APPLE_PAY_COUNTRY_LIST("applePayCountryList", "Apple Pay国家/地区列表（隐藏配置，SQL预置JSON，供下拉选项）", ValueTypeEnum.STRING, ""),
    APPLE_PAY_CURRENCY("applePayCurrency", "结算币种（ISO 4217 三位）", ValueTypeEnum.STRING, "CNY"),
    APPLE_PAY_CURRENCY_LIST("applePayCurrencyList", "Apple Pay货币列表（隐藏配置，SQL预置JSON）", ValueTypeEnum.STRING, ""),
    APPLE_PAY_SUPPORTED_NETWORKS("applePaySupportedNetworks", "支持卡组织（逗号分隔：chinaUnionPay/visa/masterCard/amex/jcb/discover）", ValueTypeEnum.STRING, "chinaUnionPay,visa,masterCard"),
    APPLE_PAY_MERCHANT_IDENTITY_CERT("applePayMerchantIdentityCert", "商户身份证书已上传状态；0-未上传，1-已上传", ValueTypeEnum.INTEGER, "0"),
    APPLE_PAY_MERCHANT_IDENTITY_KEY("applePayMerchantIdentityKey", "商户身份证书私钥已上传状态；0-未上传，1-已上传", ValueTypeEnum.INTEGER, "0"),
    APPLE_PAY_PROCESSING_PRIVATE_KEY("applePayProcessingPrivateKey", "支付处理证书私钥已上传状态；0-未上传，1-已上传", ValueTypeEnum.INTEGER, "0"),
    APPLE_PAY_ACQUIRER_PROVIDER("applePayAcquirerProvider", "收单机构：ums(银联商务)/stripe/adyen/generic(通用)", ValueTypeEnum.STRING, "generic"),
    APPLE_PAY_UMS_SIGN_TYPE("applePayUmsSignType", "银联商务签名算法：SHA256/SM3", ValueTypeEnum.STRING, "SHA256"),
    APPLE_PAY_ACQUIRER_GATEWAY("applePayAcquirerGateway", "收单网关地址", ValueTypeEnum.STRING, null),
    APPLE_PAY_ACQUIRER_MERCHANT_NO("applePayAcquirerMerchantNo", "收单商户号", ValueTypeEnum.STRING, null),
    APPLE_PAY_ACQUIRER_KEY("applePayAcquirerKey", "收单密钥", ValueTypeEnum.STRING, null),

    // *** Antom（蚂蚁国际）支付设置 ***
    // 接入文档：https://docs.antom.com/ac/aba/hosted_checkout
    USE_ANTOM("useAntom", "是否启用Antom支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    ANTOM_CURRENCY("antomCurrency", "Antom默认结算货币", ValueTypeEnum.STRING, "usd"),
    ANTOM_CLIENT_ID("antomClientId", "Antom客户端ID（Client ID）", ValueTypeEnum.STRING, null),
    ANTOM_MERCHANT_PRIVATE_KEY("antomMerchantPrivateKey", "Antom商户私钥（签名用，RSA2048 PKCS8 Base64）", ValueTypeEnum.STRING, null),
    ANTOM_PUBLIC_KEY("antomPublicKey", "Antom公钥（验签用，由Antom Dashboard提供）", ValueTypeEnum.STRING, null),
    ANTOM_GATEWAY_URL("antomGatewayUrl", "Antom网关地址（默认 https://open-sea-global.alipay.com，沙箱用 https://open-sea-global.alipay.com 路径会自动带 /ams/sandbox）", ValueTypeEnum.STRING, "https://open-sea-global.alipay.com"),
    ANTOM_SANDBOX("antomSandbox", "Antom是否启用沙箱环境；0-生产，1-沙箱", ValueTypeEnum.INTEGER, "0"),
    ANTOM_CURRENCY_LIST("antomCurrencyList", "Antom货币列表", ValueTypeEnum.STRING, ""),

    // *** Xendit（东南亚聚合）支付设置 ***
    // 接入文档：https://developers.xendit.co/api-reference/#create-invoice
    // 备用通道：仅当客户主体可注册 Xendit（SG/ID/PH/MY/TH/VN）且专攻单一东南亚国家时启用。
    // 与 Antom 互补：Xendit 在 IDR/PHP 本地深度（QRIS/OVO/DANA/ShopeePay）上更强，Antom 在 Alipay+ 钱包网络更全。
    USE_XENDIT("useXendit", "是否启用Xendit支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    XENDIT_CURRENCY("xenditCurrency", "Xendit默认结算货币", ValueTypeEnum.STRING, "idr"),
    XENDIT_SECRET_KEY("xenditSecretKey", "Xendit Secret Key（服务端API调用）", ValueTypeEnum.STRING, null),
    XENDIT_PUBLIC_KEY("xenditPublicKey", "Xendit Public Key（前端嵌入Checkout用，可选）", ValueTypeEnum.STRING, null),
    XENDIT_WEBHOOK_TOKEN("xenditWebhookToken", "Xendit Webhook Verification Token", ValueTypeEnum.STRING, null),
    XENDIT_BASE_URL("xenditBaseUrl", "Xendit API 基地址（默认 https://api.xendit.co）", ValueTypeEnum.STRING, "https://api.xendit.co"),
    XENDIT_SANDBOX("xenditSandbox", "Xendit是否使用test key（0-生产，1-沙箱；仅展示用，真实环境由key前缀决定）", ValueTypeEnum.INTEGER, "0"),
    XENDIT_CURRENCY_LIST("xenditCurrencyList", "Xendit货币列表", ValueTypeEnum.STRING, ""),
    XENDIT_PAYMENT_METHODS("xenditPaymentMethods", "Xendit启用的支付方式（逗号分隔，留空表示全部）", ValueTypeEnum.STRING, ""),
    XENDIT_INVOICE_DURATION("xenditInvoiceDuration", "Xendit支付链接有效期（秒）", ValueTypeEnum.INTEGER, "86400"),

    // *** 云支付设置 ***
    USE_YUNPAY("useYunpay", "是否启用云支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    YUNPAY_UID("yunpayUid", "商户号", ValueTypeEnum.STRING, null),
    YUNPAY_SECRET_KEY("yunpaySecretKey", "商户秘钥", ValueTypeEnum.STRING, null),

    // *** 汇付斗拱支付设置（境内聚合收单，微信/支付宝共用一套服务商凭证） ***
    // 接入文档：https://paas.huifu.com/docs/lightning/  官方SDK：com.huifu.bspay.sdk:dg-java-sdk
    USE_HUIFU("useHuifu", "是否启用汇付支付；0-关闭，1-开启（开启后微信/支付宝均可用）", ValueTypeEnum.INTEGER, null),
    HUIFU_SYS_ID("huifuSysId", "汇付系统号（服务商sys_id）", ValueTypeEnum.STRING, null),
    HUIFU_PRODUCT_ID("huifuProductId", "汇付产品号（product_id）", ValueTypeEnum.STRING, null),
    HUIFU_MERCHANT_ID("huifuMerchantId", "汇付商户号（平台服务商huifu_id，一期收款主体）", ValueTypeEnum.STRING, null),
    HUIFU_RSA_PRIVATE_KEY("huifuRsaPrivateKey", "汇付商户RSA私钥（加签用）", ValueTypeEnum.STRING, null),
    HUIFU_PUBLIC_KEY("huifuPublicKey", "汇付公钥（验签用）", ValueTypeEnum.STRING, null),
    HUIFU_GATEWAY("huifuGateway", "汇付API网关地址", ValueTypeEnum.STRING, null),
    HUIFU_PROFIT_SHARING_ENABLED("huifuProfitSharingEnabled", "是否启用汇付分账；0-关闭，1-开启", ValueTypeEnum.INTEGER, "0"),
    HUIFU_DELAY_ACCT_ENABLED("huifuDelayAcctEnabled", "汇付是否启用延时冻结；0-否，1-是", ValueTypeEnum.INTEGER, "0"),
    HUIFU_PROFIT_SHARING_NOTIFY_URL("huifuProfitSharingNotifyUrl", "汇付分账回调地址", ValueTypeEnum.STRING, null),
    HUIFU_PROFIT_SHARING_MAX_RATIO("huifuProfitSharingMaxRatio", "汇付默认最大分账比例(%)", ValueTypeEnum.STRING, "20.00"),
    HUIFU_PROFIT_SHARING_RULE_ORIGIN("huifuProfitSharingRuleOrigin", "汇付分账规则来源；01-接口发起，02-控台配置", ValueTypeEnum.STRING, "01"),
    ALIPAY_PROFIT_SHARING_ENABLED("alipayProfitSharingEnabled", "是否启用支付宝分账；0-关闭，1-开启", ValueTypeEnum.INTEGER, "0"),
    ALIPAY_DELAY_ACCT_ENABLED("alipayDelayAcctEnabled", "支付宝是否启用延时冻结；0-否，1-是", ValueTypeEnum.INTEGER, "0"),
    ALIPAY_PROFIT_SHARING_NOTIFY_URL("alipayProfitSharingNotifyUrl", "支付宝分账回调地址", ValueTypeEnum.STRING, null),
    ALIPAY_PROFIT_SHARING_MAX_RATIO("alipayProfitSharingMaxRatio", "支付宝默认最大分账比例(%)，与支付宝官方上限一致", ValueTypeEnum.STRING, "30.00"),
    ALIPAY_PROFIT_SHARING_RULE_ORIGIN("alipayProfitSharingRuleOrigin", "支付宝分账规则来源；01-接口发起，02-控台配置", ValueTypeEnum.STRING, "01"),
    ALIPAY_PROFIT_SHARING_PLATFORM_ID("alipayProfitSharingPlatformId", "支付宝平台分账收款账户(PID/UID)", ValueTypeEnum.STRING, null),
    ALIPAY_APP_AUTH_TOKEN("alipayAppAuthToken", "支付宝服务商代调授权令牌(app_auth_token)", ValueTypeEnum.STRING, null),
    HUIFU_HOSTING_PROJECT_ID("huifuHostingProjectId", "汇付统一收银台托管项目号（project_id，PC 收银台用；留空则不下发）", ValueTypeEnum.STRING, null),

    // *** 拉卡拉支付设置 ***
    // 接入文档：https://o.lakala.com/#/home/document ；官方 Java SDK 1.0.8（内置于 repository/）
    // 一期定位：平台统一收款 + 退款，覆盖 PC 收银台 / H5 / 微信公众号 JSAPI / 小程序 / APP / 被扫付款码
    USE_LAKALA("useLakala", "是否启用拉卡拉支付；0-关闭，1-开启", ValueTypeEnum.INTEGER, "0"),
    LAKALA_APP_ID("lakalaAppId", "拉卡拉开放平台 AppId", ValueTypeEnum.STRING, null),
    LAKALA_MERCHANT_NO("lakalaMerchantNo", "拉卡拉商户号（merchantNo）", ValueTypeEnum.STRING, null),
    LAKALA_TERM_NO("lakalaTermNo", "拉卡拉终端号（termNo）", ValueTypeEnum.STRING, null),
    LAKALA_SERIAL_NO("lakalaSerialNo", "拉卡拉商户证书序列号（serialNo）", ValueTypeEnum.STRING, null),
    LAKALA_MERCHANT_PRIVATE_KEY("lakalaMerchantPrivateKey", "拉卡拉商户私钥（字符串，PKCS8，签名用）", ValueTypeEnum.STRING, null),
    LAKALA_PLATFORM_CERT("lakalaPlatformCert", "拉卡拉开放平台证书（字符串，.cer 内容，用于验签及通知验签）", ValueTypeEnum.STRING, null),
    LAKALA_SM4_KEY("lakalaSm4Key", "拉卡拉报文对称加密 SM4 密钥（敏感报文加解密用，可选）", ValueTypeEnum.STRING, null),
    LAKALA_GATEWAY_URL("lakalaGatewayUrl", "拉卡拉网关地址（生产 https://s2.lakala.com，测试 https://test.wsmsd.cn/sit）", ValueTypeEnum.STRING, "https://s2.lakala.com"),
    LAKALA_SANDBOX("lakalaSandbox", "拉卡拉是否启用沙箱/测试环境；0-生产，1-沙箱", ValueTypeEnum.INTEGER, "0"),
    LAKALA_WX_SUB_APPID("lakalaWxSubAppid", "拉卡拉微信子 AppId（公众号 JSAPI 场景用）", ValueTypeEnum.STRING, null),
    LAKALA_WX_MINI_APPID("lakalaWxMiniAppid", "拉卡拉微信小程序 AppId（小程序场景用）", ValueTypeEnum.STRING, null),

    // *** 易宝支付（YOP 开放平台）设置（境内聚合收单：JSAPI 微信/支付宝 + PC 收银台） ***
    // 接入文档：https://open.yeepay.com/docs  官方SDK：com.yeepay.yop.sdk:yop-java-sdk（RSA2048-SHA256）
    // 说明：响应/异步通知的平台验签由 SDK 自动拉取并缓存易宝平台证书完成，无需单独配置平台公钥。
    USE_YEEPAY("useYeepay", "是否启用易宝支付；0-关闭，1-开启（开启后微信/支付宝均可用）", ValueTypeEnum.INTEGER, "0"),
    YEEPAY_APP_KEY("yeepayAppKey", "易宝应用 AppKey（app_xxx）", ValueTypeEnum.STRING, null),
    YEEPAY_PARENT_MERCHANT_NO("yeepayParentMerchantNo", "易宝发起方商户编号（平台/服务商 parentMerchantNo）", ValueTypeEnum.STRING, null),
    YEEPAY_MERCHANT_NO("yeepayMerchantNo", "易宝收款商户编号（merchantNo）", ValueTypeEnum.STRING, null),
    YEEPAY_ISV_PRIVATE_KEY("yeepayIsvPrivateKey", "易宝商户 RSA 私钥（加签/信封解密用，PKCS#8 Base64）", ValueTypeEnum.STRING, null),
    YEEPAY_SERVER_ROOT("yeepayServerRoot", "易宝网关地址（留空用 SDK 默认生产地址；沙箱联调时填沙箱地址）", ValueTypeEnum.STRING, null),

    // *** 系统设置-邮箱服务器 ***

    MAIL_SERVICE("mailService", "邮件服务；0-本地邮件服务器，1-SMTP服务", ValueTypeEnum.INTEGER, null),
    SMTP_SSL("smtpSsl", "是否启用SSL；0-否，1-是", ValueTypeEnum.INTEGER, null),
    SMTP_HOST("smtpHost", "发送邮件服务器地址(SMTP)", ValueTypeEnum.STRING, null),
    SMTP_PORT("smtpPort", "SMTP服务器端口", ValueTypeEnum.STRING, null),
    SMTP_USER("smtpUser", "邮件发送帐号", ValueTypeEnum.STRING, null),
    SMTP_PASS("smtpPass", "帐号密码", ValueTypeEnum.STRING, null),
    SMTP_MAIL("smtpMail", "邮件回复地址", ValueTypeEnum.STRING, null),
    MAIL_CHARSET("mailCharset", "邮件编码；UTF8-国际编码(UTF8)，GB2312-简体中文(GB2312)，BIG5-繁体中文(BIG5)", ValueTypeEnum.STRING, null),
    TEST_MAIL_ADDRESS("testMailAddress", "测试邮件地址", ValueTypeEnum.STRING, null),

    // *** 消息设置-通知设置 ***

    // *** 短信设置 ***
    SMS_PROVIDER("smsProvider", "短信服务商；1-阿里云，2-腾讯云，3-华为云，4-火山引擎", ValueTypeEnum.INTEGER, "1"),
    SMS_KEY_ID("smsKeyId", "短信帐户用户名", ValueTypeEnum.STRING, null),
    SMS_KEY_SECRET("smsKeySecret", "短信帐户密码", ValueTypeEnum.STRING, null),
    SMS_SIGN_NAME("smsSignName", "短信签名", ValueTypeEnum.STRING, null),
    SMS_SHOP_MOBILE("smsShopMobile", "商家短信号码", ValueTypeEnum.STRING, null),

    // *** 腾讯云短信 ***
    SMS_TENCENT_SECRET_ID("smsTencentSecretId", "腾讯云SecretId", ValueTypeEnum.STRING, null),
    SMS_TENCENT_SECRET_KEY("smsTencentSecretKey", "腾讯云SecretKey", ValueTypeEnum.STRING, null),
    SMS_TENCENT_SDK_APP_ID("smsTencentSdkAppId", "腾讯云SDK AppID", ValueTypeEnum.STRING, null),
    SMS_TENCENT_SIGN_NAME("smsTencentSignName", "腾讯云短信签名", ValueTypeEnum.STRING, null),
    SMS_TENCENT_SHOP_MOBILE("smsTencentShopMobile", "腾讯云商家短信号码", ValueTypeEnum.STRING, null),

    // *** 华为云短信 ***
    SMS_HUAWEI_APP_KEY("smsHuaweiAppKey", "华为云APP Key", ValueTypeEnum.STRING, null),
    SMS_HUAWEI_APP_SECRET("smsHuaweiAppSecret", "华为云APP Secret", ValueTypeEnum.STRING, null),
    SMS_HUAWEI_CHANNEL("smsHuaweiChannel", "华为云短信通道号", ValueTypeEnum.STRING, null),
    SMS_HUAWEI_SIGN_NAME("smsHuaweiSignName", "华为云短信签名", ValueTypeEnum.STRING, null),
    SMS_HUAWEI_ENDPOINT("smsHuaweiEndpoint", "华为云接入地址", ValueTypeEnum.STRING, null),
    SMS_HUAWEI_SHOP_MOBILE("smsHuaweiShopMobile", "华为云商家短信号码", ValueTypeEnum.STRING, null),

    // *** 火山引擎短信 ***
    SMS_VOLCENGINE_ACCESS_KEY("smsVolcengineAccessKey", "火山引擎AccessKey ID", ValueTypeEnum.STRING, null),
    SMS_VOLCENGINE_SECRET_KEY("smsVolcengineSecretKey", "火山引擎SecretAccessKey", ValueTypeEnum.STRING, null),
    SMS_VOLCENGINE_SMS_ACCOUNT("smsVolcengineSmsAccount", "火山引擎短信应用ID", ValueTypeEnum.STRING, null),
    SMS_VOLCENGINE_SIGN_NAME("smsVolcengineSignName", "火山引擎短信签名", ValueTypeEnum.STRING, null),
    SMS_VOLCENGINE_SHOP_MOBILE("smsVolcengineShopMobile", "火山引擎商家短信号码", ValueTypeEnum.STRING, null),

    // *** 阿里云短信通道开关（仅跨境版；1-+86国内 2-其他海外，英文逗号分隔多选） ***
    SMS_ALIYUN_CHANNEL("smsAliyunChannel", "阿里云短信通道；1-+86国内，2-其他海外，多选英文逗号分隔", ValueTypeEnum.STRING, "1"),

    // *** 阿里云国际短信 SenderID（SendMessageToGlobe 的 From，需控制台报备） ***
    SMS_SENDER_ID("smsSenderId", "阿里云国际短信发送方标识SenderID", ValueTypeEnum.STRING, null),

    // *** 邮箱通知 ***
    SERVICE_EMAIL("serviceEmail", "客服收件地址", ValueTypeEnum.STRING, null),
    SEND_CONFIRM_EMAIL("sendConfirmEmail", "提交订单发送邮件; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    ORDER_PAY_EMAIL("orderPayEmail", "订单支付发送邮件; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    SEND_SERVICE_EMAIL("sendServiceEmail", "下单给客服发邮件; 0-否，1-是", ValueTypeEnum.INTEGER, null),
    SEND_SHIP_EMAIL("sendShipEmail", "发货时发送邮件; 0-否，1-是", ValueTypeEnum.INTEGER, null),

    // *** 会员认证 ***

    CLOSE_AUTH("closeAuth", "是否关闭；1-否，0-是", ValueTypeEnum.INTEGER, "0"),
    TYPE("type", "认证类型；0-个人认证 + 企业认证，1-企业认证", ValueTypeEnum.INTEGER, null),
    IS_IDENTITY("isIdentity", "是否实名后才能查看价格；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    IS_ENQUIRY("isEnquiry", "是否开启询价功能；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    SMS_NOTE("smsNote", "是否短信通知；0-关闭，1-开启", ValueTypeEnum.INTEGER, null),
    TIPS("tips", "审核周期", ValueTypeEnum.STRING, null),
    BULK_PURCHASE("bulkPurchase", "是否批量采购；0-隐藏，1-显示", ValueTypeEnum.INTEGER, "0"),

    // *** 售后设置 ***
    TEMPLATE_CONTENT("templateContent", "售后服务", ValueTypeEnum.STRING, null),
    //是否展示售后服务
    SHOW_TEMPLATE_CONTENT("showTemplateContent", "是否展示售后服务", ValueTypeEnum.BOOLEAN, "true"),

    // *** 授权信息 ***

    LICENSE("license", "授权码", ValueTypeEnum.STRING, null),
    LICENSED_TYPE("licensedType", "授权类型", ValueTypeEnum.STRING, null),
    LICENSED_TYPE_NAME("licensedTypeName", "授权类型名称", ValueTypeEnum.STRING, null),
    IS_ENTERPRISE("isEnterprise", "是否企业用户", ValueTypeEnum.INTEGER, null),
    DE_COPYRIGHT("deCopyright", "去版权", ValueTypeEnum.INTEGER, null),
    AUTHORIZED_DOMAIN("authorizedDomain", "授权域名", ValueTypeEnum.STRING, null),

    ORDER_ID("orderId", "订单 ID", ValueTypeEnum.STRING, null),
    HOLDER("holder", "持有者", ValueTypeEnum.STRING, null),
    LICENSED_ID("licensedId", "授权 ID 列表", ValueTypeEnum.STRING, null),
    RELEASE_TIME("releaseTime", "发布时间", ValueTypeEnum.STRING, null),
    EXPIRATION_TIME("expirationTime", "过期时间", ValueTypeEnum.STRING, null),

    // *** 主题风格设置 ***

    THEME_STYLE("themeStyle", "主题类型", ValueTypeEnum.STRING, null),

    // *** 分页页装修设置 ***

    PRODUCT_CATEGORY_DECORATE_TYPE("productCategoryDecorateType", "分类页装修类型；1-样式一，2-样式二，3-样式三", ValueTypeEnum.INTEGER, null),

    // *** 商品详情页风格设置 ***

    PRODUCT_DETAIL_STYLE_TYPE("productDetailStyleType", "商品详情页风格；1-样式一，2-样式二", ValueTypeEnum.INTEGER, null),

    // *** 搜索页风格设置 ***

    SEARCH_STYLE_TYPE("searchStyleType", "搜索页风格；1-样式一，2-样式二", ValueTypeEnum.INTEGER, null),

    // *** 移动端搜索风格设置 ***

    MOBILE_SEARCH_STYLE_TYPE("mobileSearchStyleType", "移动端搜索风格；1-普通商品搜索，2-门店聚合搜索", ValueTypeEnum.INTEGER, "1"),

    USE_NEW_MEMBER_CENTER("useNewMemberCenter", "是否使用新版个人中心页；0-旧版离散装修，1-新版整页装修", ValueTypeEnum.INTEGER, "0"),

    // *** 买家秀设置 ***
    BUYER_SHOW_ENABLED("buyerShowEnabled", "是否开启买家秀；0-否，1-是", ValueTypeEnum.INTEGER, null),
    BUYER_SHOW_MOBILE_SHOW_NAME("buyerShowMobileShowName", "买家秀手机端展示名称", ValueTypeEnum.STRING, null),
    BUYER_SHOW_GUIDANCE_CONTENT("buyerShowGuidanceContent", "买家秀评论引导文案", ValueTypeEnum.STRING, null),
    BUYER_SHOW_TEMPLATE_STYLE("buyerShowTemplateStyle", "买家秀模板样式：0-双栏列表，1-单栏列表", ValueTypeEnum.INTEGER, null),


    // *** 店铺设置 ***

    SHOP_PRODUCT_NEED_CHECK("shopProductNeedCheck", "店铺商品是否需要审核；0-否，1-是", ValueTypeEnum.INTEGER, null),
    MAX_RECOMMEND_PRODUCT_COUNT("maxRecommendProductCount", "最多推荐商品数量", ValueTypeEnum.STRING, null),
    MAX_SUB_ADMINISTRATOR("maxSubAdministrator", "最大管理员数", ValueTypeEnum.STRING, null),
    DEFAULT_SHOP_NAME("defaultShopName", "店铺名称", ValueTypeEnum.STRING, null),

    // *** 供应商设置 ***

    VENDOR_PRODUCT_NEED_CHECK("vendorProductNeedCheck", "供应商商品是否需要审核；0-否，1-是", ValueTypeEnum.INTEGER, null),
    VENDOR_MAX_SUB_ADMINISTRATOR("vendorMaxSubAdministrator", "最大管理员数", ValueTypeEnum.INTEGER, null),
    VENDOR_SET_PRICE_TYPE("vendorSetPriceType", "供应商设价方式(1按比例，2-按固定数值加价，3-默认售价)", ValueTypeEnum.INTEGER, null),
    VENDOR_SET_PRICE_AUTO_VALUE("vendorSetPriceAutoValue", "智能设价（百分比或固定数值）", ValueTypeEnum.STRING, null),
    VENDOR_AGREEMENT("vendorAgreement", "供应商协议", ValueTypeEnum.STRING, null),

    // *** 商户设置 ***

    PERSON_APPLY_ENABLED("personApplyEnabled", "是否开启个人申请；0-否，1-是", ValueTypeEnum.INTEGER, null),
    MERCHANT_APPLY_NEED_CHECK("merchantApplyNeedCheck", "商户申请是否需要审核；0-否，1-是", ValueTypeEnum.INTEGER, null),
    MAX_SHOP_COUNT("maxShopCount", "最大店铺数量", ValueTypeEnum.STRING, null),
    SHOP_AGREEMENT("shopAgreement", "店铺协议", ValueTypeEnum.STRING, null),

    // *** 默认数据 ***
    DEFAULT_TECH_SUPPORT("defaultTechSupport", "默认技术支持", ValueTypeEnum.STRING, "/static/mini/images/common/default_tech_support.png"),
    PAYPAL_CURRENCY_LIST("paypalCurrencyList", "paypal货币列表", ValueTypeEnum.STRING, ""),


    // *** app version（菜单关闭） ***
    ANDROID_VERSION("androidVersion", "Android版本号", ValueTypeEnum.STRING, null),
    IOS_VERSION("iosVersion", "iOS版本号", ValueTypeEnum.STRING, null),
    IOS_LINK("iosLink", "iOS下载链接", ValueTypeEnum.STRING, null),
    ANDROID_LINK("androidLink", "Android下载链接", ValueTypeEnum.STRING, null),
    HOT_UPDATE_LINK("hotUpdateLink", "热更新包链接", ValueTypeEnum.STRING, null),
    HOT_UPDATE_TYPE("hotUpdateType", "热更新类型（0：关闭，1：开启）", ValueTypeEnum.STRING, null),


    // *** 登录协议 ***
    TERMS_OF_SERVICE_SHOW("termsOfServiceShow", "服务协议展示", ValueTypeEnum.INTEGER, null),
    TERMS_OF_SERVICE("termsOfService", "服务协议", ValueTypeEnum.STRING, null),
    PRIVACY_POLICY_SHOW("privacyPolicyShow", "隐私政策展示", ValueTypeEnum.INTEGER, null),
    PRIVACY_POLICY("privacyPolicy", "隐私政策", ValueTypeEnum.STRING, null),
    AFTER_SALES_SERVICE_SHOW("afterSalesServiceShow", "售后服务展示", ValueTypeEnum.INTEGER, null),
    AFTER_SALES_SERVICE("afterSalesService", "售后服务", ValueTypeEnum.STRING, null),

    // *** 提现设置 ***

    // *** 分账设置 ***
    // *** 对账与结算设置 ***
    BILLING_NODE("billingNode", "出账节点，1-下单成功，2-售后申请", ValueTypeEnum.INTEGER, "1"),
    COLLECTION_NODE("collectionNode", "入账节点，1-订单完成，2-确认收货", ValueTypeEnum.INTEGER, "1"),
    COLLECTION_TIME_SETTING("collectionTimeSetting", "入账时间（单位：天）", ValueTypeEnum.INTEGER, "0"),
    COLLECTION_METHOD("collectionMethod", "入账方式，1-自动入账，2-手动入账", ValueTypeEnum.INTEGER, "1"),
    COLLECTION_ACCOUNT_TYPE("collectionAccountType", "入账账户类型，1-余额", ValueTypeEnum.INTEGER, "1"),
    PROFIT_SHARING_ENABLED("profitSharingEnabled", "分账总开关（三渠道级联父开关），0-关闭，1-开启；默认开，仅显式关闭时三渠道分账全部不生效", ValueTypeEnum.INTEGER, "1"),
    SPLIT_PAYMENT_METHOD("splitPaymentMethod", "分账方式，1-线下分账，2-线上分账", ValueTypeEnum.INTEGER, "1"),
    PROFIT_SHARING_ONLINE_CHANNEL("profitSharingOnlineChannel", "线上分账渠道（单选，同一时间只允许一个生效），wechat-微信，huifu-汇付，alipay-支付宝", ValueTypeEnum.STRING, "wechat"),
    // *** 微信线上分账设置 ***
    PROFIT_SHARING_COOLING_DAYS("profitSharingCoolingDays", "分账冷却期天数（线上分账）", ValueTypeEnum.INTEGER, "0"),
    AUTO_PROFIT_SHARING_ENABLED("autoProfitSharingEnabled", "是否启用自动分账，0-不启用，1-启用（线上分账）", ValueTypeEnum.INTEGER, "0"),
    AUTO_PROFIT_SHARING_DAYS("autoProfitSharingDays", "自动分账等待天数（线上分账）", ValueTypeEnum.INTEGER, "7"),

    // *** 服务费与提现费率设置 ***
    STORE_GENERAL_SERVICE_FEE_RATE("storeGeneralServiceFeeRate", "店铺通用平台服务费率（%）", ValueTypeEnum.DECIMAL, "0"),
    STORE_WITHDRAWAL_FEE_RATE("storeWithdrawalFeeRate", "店铺提现手续费率（%）", ValueTypeEnum.DECIMAL, "0"),
    STOREFRONT_GENERAL_SERVICE_FEE_RATE("storefrontGeneralServiceFeeRate", "门店通用平台服务费率（%）", ValueTypeEnum.DECIMAL, "0"),
    STOREFRONT_WITHDRAWAL_FEE_RATE("storefrontWithdrawalFeeRate", "门店提现手续费率（%）", ValueTypeEnum.DECIMAL, "0"),
    SUPPLIER_GENERAL_SERVICE_FEE_RATE("supplierGeneralServiceFeeRate", "供应商通用平台服务费率（%）", ValueTypeEnum.DECIMAL, "0"),
    SUPPLIER_WITHDRAWAL_FEE_RATE("supplierWithdrawalFeeRate", "供应商提现手续费率（%）", ValueTypeEnum.DECIMAL, "0"),

    // *** 提现设置 ***
    WITHDRAWAL_RECEIPT_METHOD("withdrawalReceiptMethod", "线下收款方式，数组：1-银行卡，2-支付宝，3-微信", ValueTypeEnum.JSON, JSON.toJSONString(Arrays.asList(1, 2, 3))),
    WITHDRAWAL_ENABLED("withdrawalEnabled", "是否开通提现，0-不开通，1-开通", ValueTypeEnum.INTEGER, "0"),
    WITHDRAWAL_MIN_AMOUNT("minAmount", "提现限额-最小金额", ValueTypeEnum.DECIMAL, "0"),
    WITHDRAWAL_MAX_AMOUNT("maxAmount", "提现限额-最大金额", ValueTypeEnum.DECIMAL, "0"),
    WITHDRAWAL_FREQUENCY_UNIT("withdrawalFrequencyUnit", "提现频次单位，1-每日，2-每月，3-每年", ValueTypeEnum.INTEGER, "1"),
    WITHDRAWAL_FREQUENCY_COUNT("withdrawalFrequencyCount", "提现频次数量", ValueTypeEnum.INTEGER, "1"),
    WITHDRAWAL_REVIEW_METHOD("withdrawalReviewMethod", "提现审核方式，1-人工审核", ValueTypeEnum.INTEGER, "1"),
    WITHDRAWAL_DESCRIPTION("withdrawalDescription", "提现说明", ValueTypeEnum.STRING, ""),

    // *** 线上提现设置（三渠道共用一套，CR-20260722-114500）***
    ONLINE_WITHDRAW_ENABLED("onlineWithdrawEnabled", "线上提现开关（开通提现），0-关，1-开", ValueTypeEnum.INTEGER, "0"),
    ONLINE_WITHDRAWAL_MIN_AMOUNT("onlineMinAmount", "线上提现限额-最小金额", ValueTypeEnum.DECIMAL, "0"),
    ONLINE_WITHDRAWAL_MAX_AMOUNT("onlineMaxAmount", "线上提现限额-最大金额", ValueTypeEnum.DECIMAL, "0"),
    ONLINE_WITHDRAWAL_FREQUENCY_UNIT("onlineWithdrawalFrequencyUnit", "线上提现频次单位，1-每日，2-每月，3-每年", ValueTypeEnum.INTEGER, "1"),
    ONLINE_WITHDRAWAL_FREQUENCY_COUNT("onlineWithdrawalFrequencyCount", "线上提现频次数量", ValueTypeEnum.INTEGER, "1"),
    ONLINE_WITHDRAWAL_REVIEW_METHOD("onlineWithdrawalReviewMethod", "线上提现审核方式，1-人工审核", ValueTypeEnum.INTEGER, "1"),
    ONLINE_WITHDRAWAL_DESCRIPTION("onlineWithdrawalDescription", "线上提现说明", ValueTypeEnum.STRING, ""),
    ONLINE_WITHDRAW_CHANNELS("onlineWithdrawChannels", "线上收款方式（启用渠道，多选），JSON 数组：WECHAT/HUIFU/ALIPAY", ValueTypeEnum.JSON, JSON.toJSONString(Arrays.asList("WECHAT"))),

    // *** 会员余额提现设置（与分账/佣金提现独立，D1107）***
    MEMBER_WITHDRAWAL_ENABLED("memberWithdrawalEnabled", "会员余额提现开关，0-关闭，1-开通；默认关闭", ValueTypeEnum.INTEGER, "0"),
    MEMBER_WITHDRAWAL_RECEIPT_METHOD("memberWithdrawalReceiptMethod", "会员余额提现到账方式，数组：1-银行卡，2-支付宝，3-微信", ValueTypeEnum.JSON, JSON.toJSONString(Arrays.asList(1, 2, 3))),
    MEMBER_WITHDRAWAL_DESCRIPTION("memberWithdrawalDescription", "会员余额提现说明", ValueTypeEnum.STRING, ""),

    // *** 分销佣金提现设置（独立于会员余额提现，D1107）***
    COMMISSION_WITHDRAWAL_ENABLED("commissionWithdrawalEnabled", "分销佣金提现开关，0-关闭，1-开通；默认关闭", ValueTypeEnum.INTEGER, "0"),
    COMMISSION_WITHDRAWAL_RECEIPT_METHOD("commissionWithdrawalReceiptMethod", "佣金提现到账方式，数组：1-银行卡，2-支付宝，3-微信", ValueTypeEnum.JSON, JSON.toJSONString(Arrays.asList(1, 2, 3))),
    COMMISSION_WITHDRAWAL_DESCRIPTION("commissionWithdrawalDescription", "分销佣金提现说明", ValueTypeEnum.STRING, ""),

    // *** 门店设置 ***
    STORE_POST_ALLOCATION_STATUS("storePostAllocationStatus", "商品分配后状态", ValueTypeEnum.INTEGER, null),
    STORE_INDEPENDENT_GOODS("storeIndependentGoods", "门店独立商品", ValueTypeEnum.INTEGER, null),
    STORE_ASSIGN_PRODUCT_NAME("storeAssignProductName", "分配商品名称", ValueTypeEnum.INTEGER, null),
    STORE_ASSIGN_PRODUCT_PRICE("storeAssignProductPrice", "分配商品价格", ValueTypeEnum.INTEGER, null),
    STORE_ASSIGN_PRODUCT_IMAGE("storeAssignProductImage", "门店可修改商品主图、详情页、相册", ValueTypeEnum.INTEGER, "0"),
    STORE_USE_SOLO_PRODUCT_STOCK("storeUseSoloProductStock", "使用门店独立库存", ValueTypeEnum.INTEGER, null),
    STORE_USE_TOTAL_PRODUCT_STOCK("storeUseTotalProductStock", "使用总库存", ValueTypeEnum.INTEGER, null),
    STORE_CUSTOM_SUBMIT_SHIPPING_TYPE("storeCustomSubmitShippingType", "客户下单配送方式", ValueTypeEnum.INTEGER, null),
    STORE_SHOW_OTHER_CITY_STORE("storeShowOtherCityStore", "显示地区其他门店", ValueTypeEnum.INTEGER, null),
    STORE_ASSIGN_DELIVERY_SERVICE_FEE("storeAssignDeliveryServiceFee", "分配商品配送员服务费", ValueTypeEnum.INTEGER, "0"),
    DELIVERY_SERVICE_FEE_ENABLED("deliveryServiceFeeEnabled", "配送员服务费总开关 0=不可配置 1=可配置", ValueTypeEnum.INTEGER, "1"),
    DEFAULT_DELIVERY_SERVICE_FEE_RATE("defaultDeliveryServiceFeeRate", "默认配送员服务费百分比（SKU 未单独设置时回退使用）", ValueTypeEnum.DECIMAL, "0.00"),
    DELIVERY_STAFF_WITHDRAWAL_ENABLED("deliveryStaffWithdrawalEnabled", "配送员提现开关", ValueTypeEnum.INTEGER, "1"),
    DELIVERY_STAFF_WITHDRAWAL_MIN_AMOUNT("deliveryStaffWithdrawalMinAmount", "配送员最低提现金额", ValueTypeEnum.DECIMAL, "100.00"),
    DELIVERY_STAFF_WITHDRAWAL_FREQUENCY_UNIT("deliveryStaffWithdrawalFrequencyUnit", "配送员提现频次单位 day/week/month", ValueTypeEnum.STRING, "day"),
    DELIVERY_STAFF_WITHDRAWAL_FREQUENCY_COUNT("deliveryStaffWithdrawalFrequencyCount", "配送员每周期可申请提现次数", ValueTypeEnum.INTEGER, "1"),
    DELIVERY_STAFF_WITHDRAWAL_REVIEW_METHOD("deliveryStaffWithdrawalReviewMethod", "配送员提现审核方式 1=人工审核", ValueTypeEnum.INTEGER, "1"),
    DELIVERY_STAFF_WITHDRAWAL_DESCRIPTION("deliveryStaffWithdrawalDescription", "配送员提现说明（≤800字）", ValueTypeEnum.STRING, ""),
    DELIVERY_STAFF_WITHDRAWAL_RECEIPT_METHOD("deliveryStaffWithdrawalReceiptMethod", "配送员提现到账方式 JSON 数组 1=银行卡 2=支付宝 3=微信", ValueTypeEnum.STRING, "[1,2,3]"),

    // *** 服务员（service_staff.type=2）服务费 + 提现设置 ***
    STORE_ASSIGN_SERVER_SERVICE_FEE("storeAssignServerServiceFee", "分配商品服务员服务费", ValueTypeEnum.INTEGER, "0"),
    SERVER_SERVICE_FEE_ENABLED("serverServiceFeeEnabled", "服务员服务费总开关 0=不可配置 1=可配置", ValueTypeEnum.INTEGER, "1"),
    DEFAULT_SERVER_SERVICE_FEE_RATE("defaultServerServiceFeeRate", "默认服务员服务费百分比（SKU 未单独设置时回退使用）", ValueTypeEnum.DECIMAL, "0.00"),
    SERVER_STAFF_WITHDRAWAL_ENABLED("serverStaffWithdrawalEnabled", "服务员提现开关", ValueTypeEnum.INTEGER, "1"),
    SERVER_STAFF_WITHDRAWAL_MIN_AMOUNT("serverStaffWithdrawalMinAmount", "服务员最低提现金额", ValueTypeEnum.DECIMAL, "100.00"),
    SERVER_STAFF_WITHDRAWAL_FREQUENCY_UNIT("serverStaffWithdrawalFrequencyUnit", "服务员提现频次单位 day/week/month", ValueTypeEnum.STRING, "day"),
    SERVER_STAFF_WITHDRAWAL_FREQUENCY_COUNT("serverStaffWithdrawalFrequencyCount", "服务员每周期可申请提现次数", ValueTypeEnum.INTEGER, "1"),
    SERVER_STAFF_WITHDRAWAL_REVIEW_METHOD("serverStaffWithdrawalReviewMethod", "服务员提现审核方式 1=人工审核", ValueTypeEnum.INTEGER, "1"),
    SERVER_STAFF_WITHDRAWAL_DESCRIPTION("serverStaffWithdrawalDescription", "服务员提现说明（≤800字）", ValueTypeEnum.STRING, ""),
    SERVER_STAFF_WITHDRAWAL_RECEIPT_METHOD("serverStaffWithdrawalReceiptMethod", "服务员提现到账方式 JSON 数组 1=银行卡 2=支付宝 3=微信", ValueTypeEnum.STRING, "[1,2,3]"),

    STORE_SELF_OPERATED_NAME("storeSelfOperatedName", "平台门店设置-门店名称", ValueTypeEnum.STRING, ""),
    STORE_SELF_OPERATED_LOGO("storeSelfOperatedLogo", "平台门店设置-门店Logo", ValueTypeEnum.STRING, ""),
    STORE_SELF_OPERATED_REGION("storeSelfOperatedRegion", "平台门店设置-门店地域", ValueTypeEnum.STRING, ""),
    STORE_SELF_OPERATED_ADDRESS("storeSelfOperatedAddress", "平台门店设置-详细地址", ValueTypeEnum.STRING, ""),
    STORE_SELF_OPERATED_LONGITUDE("storeSelfOperatedLongitude", "平台门店设置-经度", ValueTypeEnum.STRING, ""),
    STORE_SELF_OPERATED_LATITUDE("storeSelfOperatedLatitude", "平台门店设置-维度", ValueTypeEnum.STRING, ""),
    STORE_SELF_OPERATED_PHONE("storeSelfOperatedPhone", "平台门店设置-门店电话", ValueTypeEnum.STRING, ""),
    STORE_SELF_OPERATED_DESCRIPTION("storeSelfOperatedDescription", "平台门店设置-门店介绍", ValueTypeEnum.STRING, ""),
    STORE_SELF_OPERATED_CONTACT_JSON("storeSelfOperatedContactJson", "平台门店设置-联系方式JSON", ValueTypeEnum.STRING, "[]"),
    STORE_SELF_OPERATED_STATUS("storeSelfOperatedStatus", "平台门店设置-营业状态 1开业 4暂停运营 10关店", ValueTypeEnum.INTEGER, "1"),
    STORE_SELF_OPERATED_OPEN_CLOSE_JSON("storeSelfOperatedOpenCloseJson", "平台门店设置-营业时间JSON", ValueTypeEnum.STRING, ""),
    STORE_SELF_OPERATED_TIPS_JSON("storeSelfOperatedTipsJson", "平台门店设置-门店标签ID列表JSON", ValueTypeEnum.STRING, "[]"),


    AMAP_WEB_KEY("amapWebKey", "web 高德地图key", ValueTypeEnum.STRING, null),
    AMAP_WEB_SECRET("amapWebSecret", "web 高德地图secret", ValueTypeEnum.STRING, null),
    AMAP_WEB_JS_KEY("amapWebJsKey", "webJs 高德地图key", ValueTypeEnum.STRING, null),
    AMAP_WEB_JS_SECRET("amapWebJsSecret", "webJs 高德地图secret", ValueTypeEnum.STRING, null),
    AMAP_WEB_MINI_KEY("amapWebMiniKey", "webMini 高德地图key", ValueTypeEnum.STRING, null),
    AMAP_WEB_MINI_SECRET("amapWebMiniSecret", "webMini 高德地图secret", ValueTypeEnum.STRING, null),

    // *** 地图服务商 ***
    // amap-高德地图（默认，保证存量行为不变） tianditu-天地图
    MAP_PROVIDER("mapProvider", "地图服务商", ValueTypeEnum.STRING, "amap"),
    // 天地图使用 tk 鉴权，无需 secret 签名。
    // 天地图控制台只有「浏览器端 / 服务器端 / 安卓端」三种应用类型，没有小程序类型；
    // 小程序场景按官方建议走后端中转，复用服务器端 tk。
    TIANDITU_WEB_TK("tiandituWebTk", "服务器端 天地图tk", ValueTypeEnum.STRING, null),
    TIANDITU_JS_TK("tiandituJsTk", "浏览器端 天地图tk", ValueTypeEnum.STRING, null),

    // *** 收银台-订单设置 ***
    // 0关闭 1开启
    AUTO_ERASE_ZERO("autoEraseZero", "店内收银自动抹零", ValueTypeEnum.INTEGER, "0"),
    AUTO_DEDUCTION_POINTS("autoDeductionPoints", "收银积分基于订单自动抵扣", ValueTypeEnum.INTEGER, "0"),
    // (默认)1-现金支付 2-扫码字符 3-余额支付 4-积分支付
    DEFAULT_COLLECTION_METHOD("defaultCollectionMethod", "收银机默认收款方式", ValueTypeEnum.INTEGER, "1"),
    AUTO_ADD_CART_AFTER_SCAN("autoAddCartAfterScan", "扫码自动加入购物车", ValueTypeEnum.INTEGER, "1"),
    AUTO_PRINT_ONLINE_ORDER("autoPrintOnlineOrder", "线上订单自动打印", ValueTypeEnum.INTEGER, "1"),
    AUTO_PRINT_OFFLINE_ORDER("autoPrintOfflineOrder", "线下订单自动打印", ValueTypeEnum.INTEGER, "1"),
    SHOP_COMMON_REMARKS("shopCommonRemarks", "店铺常用备注列表", ValueTypeEnum.JSON_ARRAY, "[]"),
    // 商品来源：0-全部商品，其他值为JSON数组，包含：1-门店自建商品，2-平台分配商品，3-供应商商品
    POS_PRODUCT_SOURCE("posProductSource", "收银台商品来源", ValueTypeEnum.JSON_ARRAY, "[0]"),

    // *** 删除的设置 ***
    SELF_STORE_NAME("selfStoreName", "自营名称", ValueTypeEnum.STRING, null),
    SHOW_CAT_LEVEL("showCatLevel", "商城分类级数；0-三级或以上，1-二级", ValueTypeEnum.STRING, null),
    UNIQUE_OPENED("uniqueOpened", "菜单是否手风琴展开；0-否，1-是", ValueTypeEnum.STRING, null),
    IS_MULTI_LABEL("isMultiLabel", "是否多标签；0-否，1-是", ValueTypeEnum.STRING, null),
    WECHAT_SERVER_URL("wechatServerUrl", "微信服务器接口地址", ValueTypeEnum.STRING, null),
    IS_CHANGE_ORDER_STATUS("isChangeOrderStatus", "订单流转设置；0-关闭，1-开启", ValueTypeEnum.INTEGER, "0"),


    // *** 未知 ***
    SHOP_COMPANY_DUPLICATE("shopCompanyDuplicate", "shopCompanyDuplicate", ValueTypeEnum.STRING, null),
    ADMIN_DARK_LOGO("adminDarkLogo", "adminDarkLogo", ValueTypeEnum.STRING, null),
    DEFAULT_COMPANY("defaultCompany", "默认公司", ValueTypeEnum.STRING, null),
    COMPANY_ADDRESS("companyAddress", "公司地址", ValueTypeEnum.STRING, null),
    COMMENT_CHECK("commentCheck", "commentCheck", ValueTypeEnum.STRING, null),
    COMPANY_DATA_TYPE("companyDataType", "企业信息收集类型", ValueTypeEnum.STRING, null),
    COMPANY_DATA_TIPS("companyDataTips", "企业信息提示内容", ValueTypeEnum.STRING, null),
    ICO_TIG_CSS("icoTigCss", "官方图标库地址", ValueTypeEnum.STRING, null),
    AFTER_SALES_SETTING("afterSalesSetting", "售后设置", ValueTypeEnum.STRING, null),
    SHOP_RANK_DATE_RAGE("shopRankDateRage", "店铺排名时间范围（天）", ValueTypeEnum.STRING, null),
    ENABLED_COMMISSION_ORDER("enabledCommissionOrder", "是否开启分佣订单；0-否，1-是", ValueTypeEnum.STRING, null),
    DEFAULT_ADMIN_PREFIX("defaultAdminPrefix", "默认管理员前缀", ValueTypeEnum.STRING, null),
    GROW_UP_SETTING("growUpSetting", "growUpSetting", ValueTypeEnum.STRING, null),


    // 钉钉
    DING_CORP_ID("dingCorpCorpId","企业唯一标识", ValueTypeEnum.STRING, null),
    DING_AGENT_ID("dingAgentId","应用标识", ValueTypeEnum.STRING, null),
    DING_APP_KEY("dingAppKey","应用Key", ValueTypeEnum.STRING, null),
    DING_APP_SECRET("dingAppSecret","应用密钥", ValueTypeEnum.STRING, null),
    DING_TALK_ROBOT_TOKEN("dingTalkRobotToken","钉钉访问token", ValueTypeEnum.STRING, null),
    DING_TALK_SECRET("dingTalkSecret","钉钉密钥", ValueTypeEnum.STRING, null),
    //企业微信
    WXWORK_CORP_ID("wxworkCorpCorpId","企业唯一标识", ValueTypeEnum.STRING, null),
    WXWORK_AGENT_ID("wxworkAgentId","应用标识", ValueTypeEnum.STRING, null),
    WXWORK_APP_KEY("wxworkAppKey","应用Key", ValueTypeEnum.STRING, null),
    WXWORK_APP_SECRET("wxworkAppSecret","应用密钥", ValueTypeEnum.STRING, null),
    WXWORK_WEBHOOK_URL("wxworkWebhookUrl","群机器人消息推送地址", ValueTypeEnum.STRING, null),


    // 翻译版本
    TRANSLATION_VERSION("translationVersion", "翻译版本", ValueTypeEnum.LONG, "1"),

    //搜本店

    SEARCH_SELF_STORE("searchSelfStore", "搜本店", ValueTypeEnum.INTEGER, "1"),

    //首页搜店铺
    INDEX_SEARCH_SHOP("indexSearchShop", "搜店铺", ValueTypeEnum.INTEGER, "0"),

    // *** 小程序直播/视频号 独立小程序配置 ***
    WECHAT_LIVE_MINI_APP_ID("wechatLiveMiniAppId", "小程序直播AppID", ValueTypeEnum.STRING, null),
    WECHAT_LIVE_MINI_APP_SECRET("wechatLiveMiniAppSecret", "小程序直播AppSecret", ValueTypeEnum.STRING, null),

    // *** 进店规则设置（o2o 专属）***

    // *** B2B 批发商体系（CR-20260602-134800：bizCode 加 wholesale* 前缀，与 config / API JSON 三方同名）***
    WHOLESALE_ENABLED("wholesaleEnabled", "批发功能总开关；0-关闭 1-启用", ValueTypeEnum.INTEGER, "0"),
    WHOLESALE_AUTO_REGISTER("wholesaleAutoRegister", "新会员注册自动转批发商；0-否 1-是", ValueTypeEnum.INTEGER, "0"),
    WHOLESALE_DEFAULT_GRADE_ID("wholesaleDefaultGradeId", "自动转批发的默认等级ID；0=未配置", ValueTypeEnum.INTEGER, "0"),
    WHOLESALE_GIVE_POINTS("wholesaleGivePoints", "批发订单是否赠送积分；0-否 1-是", ValueTypeEnum.INTEGER, "0"),

    // *** 平台级同城配送总控（O2O 设置-配送设置-同城配送页；控制运力能力是否可用）***
    INTRA_CITY_DELIVERY_ENABLED("intraCityDeliveryEnabled", "同城配送功能总开关；0-关闭 1-启用", ValueTypeEnum.INTEGER, "1"),
    INTRA_CITY_SELF_DELIVERY_ENABLED("intraCitySelfDeliveryEnabled", "启用商家自配送（自建配送员运力）；0-关闭 1-启用", ValueTypeEnum.INTEGER, "1"),
    INTRA_CITY_THIRD_PARTY_DELIVERY_ENABLED("intraCityThirdPartyDeliveryEnabled", "启用第三方配送（美团等第三方运力）；0-关闭 1-启用", ValueTypeEnum.INTEGER, "0"),


    // *** 店铺会员（SHOP-MEMBER-20260708）：平台总开关 + 店铺客户设置（CR-20260708-1530）***
    SHOP_MEMBER_ENABLED("shopMemberEnabled", "店铺会员功能总开关；0-关闭 1-启用", ValueTypeEnum.INTEGER, "0"),
    SHOP_MEMBER_DESENSITIZE("shopMemberDesensitize", "店铺客户信息脱敏；0-关闭 1-启用（开启后店铺端客户电话/姓名脱敏）", ValueTypeEnum.INTEGER, "0"),
    SHOP_MEMBER_ALLOW_ADD("shopMemberAllowAdd", "店铺添加客户；0-关闭 1-启用", ValueTypeEnum.INTEGER, "0"),
    SHOP_MEMBER_ALLOW_IMPORT("shopMemberAllowImport", "店铺导入客户；0-关闭 1-启用", ValueTypeEnum.INTEGER, "0"),
    PREMIUM_MEMBER_BENEFIT_STACK_MODE("premiumMemberBenefitStackMode", "付费会员权益叠加策略；best-取更优，multiply-相乘叠加，override-付费卡覆盖等级", ValueTypeEnum.STRING, "best"),
    PREMIUM_MEMBER_RENEW_SMS_TEMPLATE("premiumMemberRenewSmsTemplate", "付费会员续费提醒短信模板ID；为空不发送", ValueTypeEnum.STRING, ""),
    ;


    @Schema(description = "业务代码")
    private final String bizCode;

    @Schema(description = "业务描述")
    private final String bizDesc;

    @Schema(description = "值类型")
    private final ValueTypeEnum valueType;

    @Schema(description = "默认值")
    private final String defaultValue;

    private static final Map<String, SettingsEnum> SETTINGS_MAP = new HashMap<>();

    static {
        for (SettingsEnum setting : values()) {
            SETTINGS_MAP.put(setting.bizCode, setting);
        }
    }

    public static SettingsEnum fromKey(String key) {
        return SETTINGS_MAP.get(key);
    }


    public enum ValueTypeEnum {
        STRING,
        INTEGER,
        LONG,
        BOOLEAN,
        DECIMAL,
        JSON,
        JSON_ARRAY,
        JSON_OBJECT,
    }

}
