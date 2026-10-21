package com.tigshop.bean.vo.config;

import com.alibaba.fastjson.JSON;
import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.user.UserRankConfig;
import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 初始化配置项
 *
 * @author kidd
 * @since 2025/4/9 09:12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitConfigSettingsVO {

    // *** 商城信息 ***

    @ConfigItemField(SettingsEnum.STORE_CUSTOM_SUBMIT_SHIPPING_TYPE)
    @Schema(description = "客户下单配送方式 0自提优先，1配送优先 2同城配送优先")
    private Integer storeCustomSubmitShippingType;

    @ConfigItemField(value = SettingsEnum.SHOP_LOGO)
    @Schema(description = "商城Logo")
    private String shopLogo;

    @ConfigItemField(SettingsEnum.LIGHT_SHOP_LOGO)
    @Schema(description = "lightShopLogo")
    private String lightShopLogo;

    @ConfigItemField(value = SettingsEnum.SHOP_NAME)
    @Schema(description = "商城名称")
    private String shopName;

    @ConfigItemField(value = SettingsEnum.POWERED_BY_STATUS)
    @Schema(description = "Powered by 状态")
    private Integer poweredByStatus;

    @ConfigItemField(value = SettingsEnum.POWERED_BY)
    @Schema(description = "Powered by 文案")
    private Integer poweredBy;

    @ConfigItemField(value = SettingsEnum.POWERED_BY_LOGO)
    @Schema(description = "Powered by 图标")
    private String poweredByLogo;

    @ConfigItemField(value = SettingsEnum.SHOP_COMPANY)
    @Schema(description = "版权信息; 1-系统默认，0-自定义")
    private Integer shopCompany;

    @ConfigItemField(value = SettingsEnum.DEFAULT_COPYRIGHT)
    @Schema(description = "默认版权")
    private String deCopyright;

    @ConfigItemField(value = SettingsEnum.SHOP_COMPANY_TXT)
    @Schema(description = "版权信息自定义值")
    private String shopCompanyTxt;

    @ConfigItemField(value = SettingsEnum.INTEGRAL_NAME)
    @JsonTranslate
    @Schema(description = "积分名称")
    private String integralName;

    // *** SEO 设置 ***

    @ConfigItemField(SettingsEnum.SEO_CATEGORY_ENABLE)
    @Schema(description = "商品类目SEO 0 基础链接，1 SEO链接")
    private Integer seoCategoryEnable;

    @ConfigItemField(SettingsEnum.SEO_PRODUCT_ENABLE)
    @Schema(description = "商品SEO 0 基础链接，1 SEO链接")
    private Integer seoProductEnable;

    @ConfigItemField(value = SettingsEnum.SHOP_TITLE_SUFFIX)
    @Schema(description = "商城标题后缀")
    private String shopTitleSuffix;

    @ConfigItemField(value = SettingsEnum.SHOP_TITLE)
    @Schema(description = "商城标题")
    private String shopTitle;

    @ConfigItemField(value = SettingsEnum.SHOP_KEYWORDS)
    @Schema(description = "商城关键词")
    private String shopKeywords;

    @ConfigItemField(value = SettingsEnum.SHOP_DESC)
    @Schema(description = "商城描述")
    private String shopDesc;

    @ConfigItemField(value = SettingsEnum.ICO_IMG)
    @Schema(description = "ICO图片")
    private String icoImg;

    // *** 商品货币设置 ***

    @ConfigItemField(value = SettingsEnum.DOLLAR_SIGN)
    @Schema(description = "货币符号")
    private String dollarSign;

    @ConfigItemField(value = SettingsEnum.DOLLAR_SIGN_CN)
    @Schema(description = "货币符号中文")
    private String dollarSignCn;

    // *** 域名设置 ***

    @ConfigItemField(value = SettingsEnum.AUTO_REDIRECT)
    @Schema(description = "是否自动跳转")
    private Integer autoRedirect;

    @ConfigItemField(value = SettingsEnum.H5_DOMAIN)
    @Schema(description = "H5域名")
    private String h5Domain;

    @ConfigItemField(value = SettingsEnum.PC_DOMAIN)
    @Schema(description = "PC域名")
    private String pcDomain;

    @ConfigItemField(value = SettingsEnum.ADMIN_DOMAIN)
    @Schema(description = "后台域名")
    private String adminDomain;

    // *** 微信登录 ***

    @ConfigItemField(value = SettingsEnum.OPEN_WECHAT_OAUTH)
    @Schema(description = "是否开启微信OAuth")
    private Integer openWechatOauth;

    @ConfigItemField(value = SettingsEnum.OPEN_WECHAT_REGISTER)
    @Schema(description = "是否开启小程序快捷登录；0-否，1-是")
    private Integer openWechatRegister;

    @ConfigItemField(value = SettingsEnum.WECHAT_SHARE_NEED_LOGIN)
    @Schema(description = "微信小程序分享是否需要登录；0-否，1-是")
    private Integer wechatShareNeedLogin;

    @ConfigItemField(value = SettingsEnum.OPEN_WECHAT_PC_LOGIN)
    @Schema(description = "是否开启pc扫码登录；0-否，1-是")
    private Integer openWechatPcLogin;

    @ConfigItemField(value = SettingsEnum.WECHAT_REGISTER_BIND_PHONE)
    @Schema(description = "微信注册是否绑定手机；0-否，1-是")
    private Integer wechatRegisterBindPhone;

    @ConfigItemField(SettingsEnum.OPEN_EMAIL_REGISTER)
    @Schema(description = "开启邮箱注册；0-否，1-是")
    private Integer openEmailRegister;

    // *** 商户设置 ***

    @ConfigItemField(value = SettingsEnum.PERSON_APPLY_ENABLED)
    @Schema(description = "是否开启个人申请")
    private Integer personApplyEnabled;

    // *** 备案信息

    @ConfigItemField(value = SettingsEnum.SHOP_ICP_NO)
    @Schema(description = "ICP备案号")
    private String shopIcpNo;

    @ConfigItemField(value = SettingsEnum.SHOP_ICP_NO_URL)
    @Schema(description = "ICP备案跳转链接")
    private String shopIcpNoUrl;

    @ConfigItemField(value = SettingsEnum.SHOP_110_NO)
    @Schema(description = "公安备案号")
    private String shop110No;

    @ConfigItemField(value = SettingsEnum.SHOP_110_LINK)
    @Schema(description = "公安备案跳转链接")
    private String shop110Link;

    // *** 经营许可证 ***

    @ConfigItemField(value = SettingsEnum.BUSINESS_LICENSE_ENABLED)
    @Schema(description = "经营许可证显示开关；1-开启，0-关闭")
    private Integer businessLicenseEnabled;

    @ConfigItemField(value = SettingsEnum.BUSINESS_LICENSE)
    @Schema(description = "增值电信业务经营许可证(ICP)证号")
    private String businessLicense;

    @ConfigItemField(value = SettingsEnum.EDI_LICENSE)
    @Schema(description = "EDI经营许可证号")
    private String ediLicense;

    @ConfigItemField(value = SettingsEnum.BUSINESS_LICENSE_TYPE)
    @Schema(description = "经营许可证类型，可多选：icp、edi")
    private List<String> businessLicenseType;

    // *** 买家秀设置 ***

    @ConfigItemField(SettingsEnum.BUYER_SHOW_ENABLED)
    @Schema(description = "是否开启买家秀；0-否，1-是")
    private Integer buyerShowEnabled;

    @ConfigItemField(SettingsEnum.BUYER_SHOW_MOBILE_SHOW_NAME)
    @Schema(description = "买家秀手机端展示名称")
    private String buyerShowMobileShowName;

    @ConfigItemField(SettingsEnum.BUYER_SHOW_GUIDANCE_CONTENT)
    @Schema(description = "买家秀评论引导文案")
    private String buyerShowGuidanceContent;

    @ConfigItemField(SettingsEnum.BUYER_SHOW_TEMPLATE_STYLE)
    @Schema(description = "买家秀模板样式：0-双栏列表，1-单栏列表")
    private Integer buyerShowTemplateStyle;

    // *** 客服设置 ***

    @ConfigItemField(value = SettingsEnum.KEFU_TYPE)
    @Schema(description = "客服类型")
    private Integer kefuType;

    // *** 客服信息 ***

    @Sensitive(type = SensitiveType.PHONE)
    @ConfigItemField(value = SettingsEnum.KEFU_PHONE)
    @Schema(description = "客服电话")
    private String kefuPhone;

    @ConfigItemField(value = SettingsEnum.KEFU_TIME)
    @Schema(description = "客服时间")
    private String kefuTime;

    @ConfigItemField(value = SettingsEnum.KEFU_ADDRESS)
    @JsonTranslate
    @Schema(description = "kefuAddress")
    private String kefuAddress;

    // *** 授权信息 ***

    @ConfigItemField(value = SettingsEnum.IS_ENTERPRISE)
    @Schema(description = "是否企业版")
    private Integer isEnterprise;

    // *** 分页页装修设置 ***

    @ConfigItemField(value = SettingsEnum.PRODUCT_CATEGORY_DECORATE_TYPE)
    @Schema(description = "分类装修模式")
    private Integer categoryDecorateType;

    @ConfigItemField(value = SettingsEnum.USE_NEW_MEMBER_CENTER)
    @Schema(description = "是否使用新版个人中心页；0-旧版，1-新版")
    private Integer useNewMemberCenter;

    // *** 发票设置 ***

    @ConfigItemField(value = SettingsEnum.CAN_INVOICE)
    @Schema(description = "是否能开发票; 0-否，1-是")
    private Integer canInvoice;

    @ConfigItemField(value = SettingsEnum.INVOICE_ADDED)
    @Schema(description = "是否支持增值税专用发票; 0-否，1-是")
    private Integer invoiceAdded;

    // *** 店铺设置 ***

    @ConfigItemField(value = SettingsEnum.DEFAULT_SHOP_NAME)
    @Schema(description = "默认店铺名")
    private String defaultShopName;

    // *** 基础设置 ***

    @ConfigItemField(value = SettingsEnum.IS_OPEN_MOBILE_AREA_CODE)
    @Schema(description = "是否开启国际区号")
    private Integer isOpenMobileAreaCode;

    // *** 商品信息显示设置 ***

    @ConfigItemField(value = SettingsEnum.SHOW_SELLED_COUNT)
    @Schema(description = "是否显示销量")
    private Integer showSelledCount;

    @ConfigItemField(value = SettingsEnum.SHOW_MARKETPRICE)
    @Schema(description = "是否显示市场价")
    private Integer showMarketprice;

    @ConfigItemField(SettingsEnum.ENABLE_ATTRIBUTE_FILTER)
    @Schema(description = "属性筛选：0表示关闭属性筛选，1表示开启属性筛选")
    private Integer enableAttributeFilter;

    // *** 商品评价设置 ***

    @ConfigItemField(value = SettingsEnum.PRODUCT_COMMENT_ENABLED)
    @Schema(description = "是否开启商品评价; 0-关闭，1-开启（D1146）")
    private Integer productCommentEnabled;

    @ConfigItemField(value = SettingsEnum.ANONYMOUS_COMMENT_ENABLED)
    @Schema(description = "是否开启匿名评价; 0-关闭，1-开启（D1146）")
    private Integer anonymousCommentEnabled;

    // *** 基础设置 ***

    @ConfigItemField(value = SettingsEnum.USE_SURPLUS)
    @Schema(description = "是否启用余额")
    private Integer useSurplus;

    @ConfigItemField(value = SettingsEnum.USE_POINTS)
    @Schema(description = "是否启用积分")
    private Integer usePoints;

    @ConfigItemField(value = SettingsEnum.USE_COUPON)
    @Schema(description = "是否启用优惠券")
    private Integer useCoupon;

    @ConfigItemField(value = SettingsEnum.MEMBER_WITHDRAWAL_ENABLED)
    @Schema(description = "会员余额提现开关，0-关闭，1-开通（D1107）")
    private Integer memberWithdrawalEnabled;

    @ConfigItemField(value = SettingsEnum.MEMBER_WITHDRAWAL_RECEIPT_METHOD)
    @Schema(description = "会员余额/佣金提现到账方式，数组：1-银行卡，2-支付宝，3-微信（D1107，前端按此展示）")
    private java.util.List<Integer> memberWithdrawalReceiptMethod;

    @ConfigItemField(value = SettingsEnum.COMMISSION_WITHDRAWAL_ENABLED)
    @Schema(description = "分销佣金提现开关，0-关闭，1-开通（D1107）")
    private Integer commissionWithdrawalEnabled;

    // *** Unknown ***

    @ConfigItemField(value = SettingsEnum.COMPANY_ADDRESS)
    @Schema(description = "公司地址")
    private String companyAddress;

    @ConfigItemField(value = SettingsEnum.TYPE)
    @Schema(description = "企业信息收集类型")
    private Integer companyDataType;

    @ConfigItemField(value = SettingsEnum.COMPANY_DATA_TIPS)
    @Schema(description = "企业信息提示内容")
    private String companyDataTips;

    // *** 商城状态 ***

    @ConfigItemField(value = SettingsEnum.CLOSE_ORDER)
    @Schema(description = "关闭订单时间")
    private Integer closeOrder;

    @ConfigItemField(value = SettingsEnum.SHOP_REG_CLOSED)
    @Schema(description = "是否关闭注册")
    private Integer shopRegClosed;

    // *** 会员认证 ***

    @ConfigItemField(SettingsEnum.CLOSE_AUTH)
    @Schema(description = "是否关闭；1-否，0-是")
    private Integer closeAuth;

    @ConfigItemField(value = SettingsEnum.IS_IDENTITY)
    @Schema(description = "是否需要实名认证")
    private Integer isIdentity;

    @ConfigItemField(value = SettingsEnum.IS_ENQUIRY)
    @Schema(description = "是否开启询价")
    private Integer isEnquiry;

    @ConfigItemField(SettingsEnum.BULK_PURCHASE)
    @Schema(description = "是否批量采购；0-隐藏，1-显示")
    private Integer bulkPurchase;

    @ConfigItemField(SettingsEnum.TYPE)
    @Schema(description = "认证类型；0-个人认证 + 企业认证，1-企业认证")
    private Integer type;

    // *** Google 登录 ***

    @ConfigItemField(SettingsEnum.GOOGLE_LOGIN_ON)
    @Schema(description = "是否开启Google登陆；0-否，1-是")
    private Integer googleLoginOn;

    // *** Facebook 登录 ***

    @ConfigItemField(SettingsEnum.FACEBOOK_LOGIN_ON)
    @Schema(description = "是否开启Facebook登陆；0-否，1-是")
    private Integer facebookLoginOn;

    // *** X / Twitter 登录 ***

    @ConfigItemField(SettingsEnum.TWITTER_LOGIN_ON)
    @Schema(description = "是否开启X/Twitter登陆；0-否，1-是")
    private Integer twitterLoginOn;

    // *** 个性化设置 ***
    @ConfigItemField(SettingsEnum.IS_GUEST_LIKE_GOODS)
    @Schema(description = "是否展示猜你喜欢")
    private Integer isGuestLikeGoods;

    @ConfigItemField(SettingsEnum.GUEST_LIKE_GOODS_NAME)
    @Schema(description = "猜你喜欢名称")
    private String guestLikeGoodsName;

    // *** Other ***

    @Schema(description = "静态资源地址")
    private String storageUrl;

    @Schema(description = "是否展示客服")
    private Integer showService;

    @Schema(description = "版本类型")
    private String versionType;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "成长值设置")
    private GrowUpSetting growUpSetting;

    @Schema(description = "装修页面配置")
    private DecoratePageConfig decoratePageConfig;

    @Schema(description = "默认头部样式")
    private Integer defaultHeaderStyle;

    @ConfigItemField(SettingsEnum.DEFAULT_AVATAR)
    @Schema(description = "会员默认头像")
    private String defaultAvatar;

    @ConfigItemField(SettingsEnum.TRANSLATION_VERSION)
    @Schema(description = "翻译版本")
    private Long translationVersion;

    //高德地图
    @ConfigItemField(SettingsEnum.AMAP_WEB_KEY)
    @Schema(description = "高德地图Key")
    private String amapWebKey;
    @ConfigItemField(SettingsEnum.AMAP_WEB_SECRET)
    @Schema(description = "高德地图SECRET")
    private String amapWebSecret;
    @ConfigItemField(SettingsEnum.AMAP_WEB_JS_KEY)
    @Schema(description = "高德地图Key")
    private String amapJsKey;
    @ConfigItemField(SettingsEnum.AMAP_WEB_JS_SECRET)
    @Schema(description = "高德地图SECRET")
    private String amapJsSecret;
    @ConfigItemField(SettingsEnum.AMAP_WEB_MINI_KEY)
    @Schema(description = "高德地图Key")
    private String amapMiniKey;
    @ConfigItemField(SettingsEnum.AMAP_WEB_MINI_SECRET)
    @Schema(description = "高德地图SECRET")
    private String amapMiniSecret;

    // 地图服务商：前端据此决定加载哪家 JS SDK
    @ConfigItemField(SettingsEnum.MAP_PROVIDER)
    @Schema(description = "地图服务商：amap-高德地图 tianditu-天地图")
    private String mapProvider;
    @ConfigItemField(SettingsEnum.TIANDITU_JS_TK)
    @Schema(description = "天地图JS API tk")
    private String tiandituJsTk;

    @ConfigItemField(SettingsEnum.SEARCH_SELF_STORE)
    @Schema(description = "搜本店")
    private Integer searchSelfStore;

    @ConfigItemField(SettingsEnum.INDEX_SEARCH_SHOP)
    @Schema(description = "首页是否开启搜索店铺")
    private Integer indexSearchShop;


    @ConfigItemField(SettingsEnum.DEFAULT_LOGISTICS_NAME)
    @Schema(description = "默认物流名称")
    private String defaultLogisticsName;

    @Schema(description = "是否是新版本会员中心装修")
    private boolean isNewMemberCenterDecorate;

    @Data
    public static class GrowUpSetting {

        @Schema(description = "是否开启购买订单成长值")
        private Integer buyOrder;

        @Schema(description = "购买订单数量")
        private Integer buyOrderNumber;

        @Schema(description = "每笔订单成长值")
        private Integer buyOrderGrowth;

        @Schema(description = "是否开启评价成长值")
        private Integer evpi;

        @Schema(description = "评价成长值")
        private Integer evpiGrowth;

        @Schema(description = "是否绑定手机号")
        private Integer bindPhone;

        @Schema(description = "绑定手机号成长值")
        private Integer bindPhoneGrowth;
    }

    @Data
    public static class DecoratePageConfig {

        @Schema(description = "页面类型")
        private String type;

        @Schema(description = "模块列表")
        private List<Object> module;

        @Schema(description = "背景重复方式")
        private String backgroundRepeat;

        @Schema(description = "背景大小")
        private String backgroundSize;

        @Schema(description = "风格样式")
        private Integer style;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "标题颜色")
        private String titleColor;

        @Schema(description = "头部样式")
        private Integer headerStyle;

        @Schema(description = "标题背景颜色")
        private String titleBackgroundColor;

        @Schema(description = "背景图片")
        private PicInfo backgroundImage;

        @Schema(description = "背景颜色")
        private String backgroundColor;

        @Schema(description = "Logo图片")
        private PicInfo logo;

        @Schema(description = "是否启用")
        private Boolean active;

        @Data
        public static class PicInfo {

            @Schema(description = "图片原图地址")
            private String picUrl;

            @Schema(description = "图片缩略图地址")
            private String picThumb;
        }
    }

    public void assembleData(String storageUrl, UserRankConfig growConfig, String decorateData, String versionType,Integer defaultHeaderStyle) {

        if (this.kefuType != null) {
            this.showService = kefuType> 0 ? 1 : 0;
        } else {
            this.showService = 0;
        }

        this.versionType = versionType;

        this.storageUrl = storageUrl;

        this.growUpSetting = JSON.parseObject(growConfig.getData(), GrowUpSetting.class);

        this.decoratePageConfig = JSON.parseObject(decorateData, DecoratePageConfig.class);

        this.defaultHeaderStyle = defaultHeaderStyle;
    }

}
