/**
 * 模块自注册入口
 *
 * 每个模块目录的 index.ts 内执行 registerModule，此处副作用导入触发注册。
 * 全量 93 个模块已迁移完毕（pcPageHeader 为死目录未迁移）。
 */

// ==================== mobile 基础 ====================
import "./mobile/imageAd";
import "./mobile/imageSquareAd";
import "./mobile/imageHotarea";
import "./mobile/product";
import "./mobile/whiteLine";
import "./mobile/whiteBlank";
import "./mobile/imageNav";
import "./mobile/titleAd";
import "./mobile/notice";
import "./mobile/searchBar";
import "./mobile/catNav";
// 不进工具栏（visible: false），仅保证老数据可渲染
import "./mobile/article";

// ==================== mobilePromotion 营销 ====================
import "./mobilePromotion/coupon";
import "./mobilePromotion/seckill";
import "./mobilePromotion/groupon";
import "./mobilePromotion/shopInfo";
import "./mobilePromotion/live";
import "./mobilePromotion/videoNumber";

// ==================== mobilePro 高级 ====================
import "./mobilePro/imageTextComponent";
import "./mobilePro/imageShowcase";
import "./mobilePro/productGrouping";
import "./mobilePro/groupSliding";
import "./mobilePro/countdown";
import "./mobilePro/categoryRec";
import "./mobilePro/categoryRecA1";
import "./mobilePro/productA1";
import "./mobilePro/mixedProductDisplay";
import "./mobilePro/specialOffer";
import "./mobilePro/noticeA2";
import "./mobilePro/mixedNotice";
import "./mobilePro/comboSuggestions";
import "./mobilePro/slideLargeImage";
import "./mobilePro/slidingImage";
import "./mobilePro/banner";
import "./mobilePro/bannerA2";
import "./mobilePro/categoryNav";
import "./mobilePro/categoryNavA1";
import "./mobilePro/categoryNavA2";
import "./mobilePro/categoryNavA3";
import "./mobilePro/tagNav";
import "./mobilePro/shopTips";
import "./mobilePro/noticeA3";
import "./mobilePro/newcomerCombo";
import "./mobilePro/limitedSpecial";
import "./mobilePro/productSlide";
import "./mobilePro/productCarousel";
// 不进工具栏（visible: false），仅保证老数据可渲染
import "./mobilePro/customProduct";

// ==================== mobileMember 会员页 ====================
import "./mobileMember/userInfo";
import "./mobileMember/userInfoV2";
import "./mobileMember/userInfo3";
import "./mobileMember/orderEntry";
import "./mobileMember/myWallet";
import "./mobileMember/myWalletV2";
import "./mobileMember/adImage";
import "./mobileMember/myService";
import "./mobileMember/account";
// 固定模块（ensureFixedModules 注入，visible: false 不进工具栏）
import "./mobileMember/fixedFunctionalArea";

// ==================== mobileCateCenter 分类中心 ====================
import "./mobileCateCenter/cateCenterV1";
import "./mobileCateCenter/cateCenterV2";

// ==================== mobileProductDetail 商品详情 ====================
import "./mobileProductDetail/productDetailA1";
import "./mobileProductDetail/productDetailA2";
import "./mobileProductDetail/productDetailA3";

// ==================== mobileLogin 登录页 ====================
import "./mobileLogin/mobileLogin";

// ==================== pcHome PC 首页可视化（新套，对齐 Vort，kebab-case type） ====================
// 基础模块
import "./pcHome/pc-image-ad";
import "./pcHome/pc-image-nav";
import "./pcHome/pc-image-square-ad";
import "./pcHome/pc-image-hotarea";
import "./pcHome/pc-product";
import "./pcHome/pc-divider";
import "./pcHome/pc-spacer";
import "./pcHome/pc-notice";
import "./pcHome/pc-title-ad";
// 首页独有模块
import "./pcHome/pc-home-hero";
import "./pcHome/pc-category-nav";
import "./pcHome/pc-home-quick-nav";
import "./pcHome/pc-product-group";
import "./pcHome/pc-home-coupon";
import "./pcHome/pc-home-groupon";
import "./pcHome/pc-home-seckill";

// ==================== pc PC 首页（旧套，占位预览） ====================
import "./pc/pcBanner";
import "./pc/pcSeckill";
import "./pc/pcCoupon";
import "./pc/pcCatProductNormal";
import "./pc/pcCatProductWide";
import "./pc/pcCatWide";
import "./pc/pcCatProductSimple";
import "./pc/pcImageAd";
import "./pc/pcRecommendProduct";
import "./pc/pcCustomAd1";
import "./pc/pcCustomAd2";
import "./pc/pcCategoryNav";
import "./pc/pcCategoryNav3";
import "./pc/pcGroupon";

// ==================== pcCustomPage PC 自定义页 ====================
import "./pcCustomPage/pcImageAd";
import "./pcCustomPage/pcImageSquareAd";
import "./pcCustomPage/pcImageHotarea";
import "./pcCustomPage/pcProduct";
import "./pcCustomPage/pcWhiteLine";
import "./pcCustomPage/pcWhiteBlank";
import "./pcCustomPage/pcImageNav";
import "./pcCustomPage/pcTitleAd";
import "./pcCustomPage/pcNotice";
import "./pcCustomPage/pcCoupon";
import "./pcCustomPage/pcSeckill";
import "./pcCustomPage/pcGroupon";

// ==================== mobileSearch 移动端搜索页 ====================
import "./mobileSearch/mobileSearchResult";

// ==================== pcSearch PC 搜索页 ====================
import "./pcSearch/pcSearchResult";

// ==================== pcLogin PC 登录页 ====================
import "./pcLogin/pcLogin";
