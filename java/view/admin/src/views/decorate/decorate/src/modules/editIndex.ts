export { default as CommonFrameEdit } from "./src/commonFrame/Edit.vue";
import i18n from "@/i18n";

// 用 getter 保证每次读取时重新取值，语言切换后标签即时更新
const t = (key: string) => i18n.global.t(key);
export const selectLabel = {
    get boxBackgroundGradientType() {
        return {
            upDown: t("上下"),
            leftRight: t("左右"),
            diagonal: t("斜向"),
            purity: t("单色")
        };
    },
    get picFillType() {
        return {
            cover: t("填充"),
            contain: t("周边留白")
        };
    },
    get titleFontSize() {
        return {
            12: t("小(12号)"),
            14: t("中(14号)"),
            16: t("大(16号)")
        };
    },
    get bigTitleFontSize() {
        return {
            12: t("小(12号)"),
            14: t("小(14号)"),
            16: t("中(16号)"),
            18: t("大(18号)")
        };
    },
    get titleFontBold() {
        return {
            400: t("常规体"),
            700: t("加粗体")
        };
    },
    get picPageType() {
        return {
            1: t("样式一"),
            2: t("样式二")
        };
    },
    get effectType() {
        return {
            default: t("正常模式"),
            ceiling: t("滚动至顶部固定")
        };
    },
    get textAlignment() {
        return {
            left: t("左对齐"),
            center: t("居中"),
            right: t("右对齐")
        };
    },
    get rowNum() {
        return {
            1: t("1个"),
            1.2: t("1.2个"),
            1.5: t("1.5个"),
            2: t("2个"),
            2.2: t("2.2个"),
            2.5: t("2.5个"),
            3: t("3个"),
            3.2: t("3.2个"),
            3.5: t("3.5个")
        };
    },
    get slidingEffect() {
        return {
            auto: t("自动"),
            manual: t("手动")
        };
    },
    get productListStyle() {
        return {
            1: t("大图模式"),
            2: t("一行两个"),
            3: t("一行三个"),
            4: t("一行四个"),
            5: t("一大两小")
        };
    }
};
//模块主题默认数据
export const ModuleTheme = {
    orange: {
        textColor: "#FF3E02",
        gradientColorA: "#ff3e02",
        gradientColorB: "#ff7b19",
        countdownBackgroundPic: {
            picId: 1480,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908843qTonhlo5Iw9kRJdLN1.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731908843qTonhlo5Iw9kRJdLN1.png",
            picName: "orange"
        }
    },
    red: {
        textColor: "#FF1513",
        gradientColorA: "#ff1513",
        gradientColorB: "#ff856c",
        countdownBackgroundPic: {
            picId: 1487,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908847AN8bNRFsYO681hzol5.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731908847AN8bNRFsYO681hzol5.png",
            picName: "red"
        }
    },
    blue: {
        textColor: "#4499E6",
        gradientColorA: "#4499e6",
        gradientColorB: "#7faff5",
        countdownBackgroundPic: {
            picId: 1484,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/17319088431TGhqdiHX3OhNWUiJJ.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/17319088431TGhqdiHX3OhNWUiJJ.png",
            picName: "blue"
        }
    },
    green: {
        textColor: "#59c12d",
        gradientColorA: "#59c12d",
        gradientColorB: "#64d135",
        countdownBackgroundPic: {
            picId: 1485,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908843bO258L7j1eq3dPeJTM.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731908843bO258L7j1eq3dPeJTM.png",
            picName: "green"
        }
    },
    purple: {
        textColor: "#7649d7",
        gradientColorA: "#7649d7",
        gradientColorB: "#8456e3",
        countdownBackgroundPic: {
            picId: 1486,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908846cfUWqho3wrh0l9xD0A.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731908846cfUWqho3wrh0l9xD0A.png",
            picName: "purple"
        }
    },
    grey: {
        textColor: "#373737",
        gradientColorA: "#373737",
        gradientColorB: "#6d6d6d",
        countdownBackgroundPic: {
            picId: 1481,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908843sz1aJohktlFTfhDGsY.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731908843sz1aJohktlFTfhDGsY.png",
            picName: "grey"
        }
    },
    chocolate: {
        textColor: "#4e362e",
        gradientColorA: "#4e362e",
        gradientColorB: "#603929",
        countdownBackgroundPic: {
            picId: 1482,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908843dIqndLbjKR48vEYrS0.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731908843dIqndLbjKR48vEYrS0.png",
            picName: "chocolate"
        }
    },
    brown: {
        textColor: "#a67e5a",
        gradientColorA: "#a67e5a",
        gradientColorB: "#c19270",
        countdownBackgroundPic: {
            picId: 1483,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908843tbYlP36vnX8NbNyvHG.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731908843tbYlP36vnX8NbNyvHG.png",
            picName: "brown"
        }
    }
} as const;

export const ModuleTypeList = [
    { type: "article", url: "mobile" },
    { type: "imageAd", url: "mobile" },
    { type: "imageSquareAd", url: "mobile" },
    { type: "imageHotarea", url: "mobile" },
    { type: "product", url: "mobile" },
    { type: "whiteLine", url: "mobile" },
    { type: "whiteBlank", url: "mobile" },
    { type: "imageNav", url: "mobile" },
    { type: "titleAd", url: "mobile" },
    { type: "notice", url: "mobile" },
    { type: "coupon", url: "mobilePromotion" },
    { type: "live", url: "mobilePromotion" },
    { type: "videoNumber", url: "mobilePromotion" },
    { type: "searchBar", url: "mobile" },
    { type: "catNav", url: "mobile" },
    { type: "customProduct", url: "mobilePro" },
    { type: "imageShowcase", url: "mobilePro" },
    { type: "productGrouping", url: "mobilePro" },
    { type: "groupSliding", url: "mobilePro" },
    { type: "countdown", url: "mobilePro" },
    { type: "categoryRec", url: "mobilePro" },
    { type: "categoryRecA1", url: "mobilePro" },
    { type: "productA1", url: "mobilePro" },
    { type: "mixedProductDisplay", url: "mobilePro" },
    { type: "specialOffer", url: "mobilePro" },
    { type: "noticeA2", url: "mobilePro" },
    { type: "mixedNotice", url: "mobilePro" },
    { type: "comboSuggestions", url: "mobilePro" },
    { type: "slideLargeImage", url: "mobilePro" },
    { type: "slidingImage", url: "mobilePro" },
    { type: "banner", url: "mobilePro" },
    { type: "bannerA2", url: "mobilePro" },
    { type: "categoryNav", url: "mobilePro" },
    { type: "categoryNavA2", url: "mobilePro" },
    { type: "categoryNavA3", url: "mobilePro" },
    { type: "tagNav", url: "mobilePro" },
    { type: "shopTips", url: "mobilePro" },
    { type: "noticeA3", url: "mobilePro" },
    { type: "newcomerCombo", url: "mobilePro" },
    { type: "limitedSpecial", url: "mobilePro" },
    { type: "productSlide", url: "mobilePro" },
    { type: "productCarousel", url: "mobilePro" },
    { type: "seckill", url: "mobilePromotion" },
    { type: "shopInfo", url: "mobilePromotion" },
    { type: "userInfo", url: "mobileMember" },
    { type: "userInfoV2", url: "mobileMember" },
    { type: "userInfo3", url: "mobileMember" },
    { type: "orderEntry", url: "mobileMember" },
    { type: "myWallet", url: "mobileMember" },
    { type: "myWalletV2", url: "mobileMember" },
    { type: "adImage", url: "mobileMember" },
    { type: "myService", url: "mobileMember" },
    { type: "account", url: "mobileMember" },
    { type: "fixedFunctionalArea", url: "mobileMember" },
    { type: "cateCenterV1", url: "mobileCateCenter" },
    { type: "cateCenterV2", url: "mobileCateCenter" },
    { type: "mobileLogin", url: "mobileLogin" },
    { type: "pcBanner", url: "pc" },
    { type: "pcSeckill", url: "pc" },
    { type: "pcCoupon", url: "pc" },
    { type: "pcCatProductNormal", url: "pc" },
    { type: "pcCatProductWide", url: "pc" },
    { type: "pcCatWide", url: "pc" },
    { type: "pcCatProductSimple", url: "pc" },
    { type: "pcImageAd", url: "pc" },
    { type: "pcRecommendProduct", url: "pc" },
    { type: "pcCustomAd1", url: "pc" },
    { type: "pcCustomAd2", url: "pc" },
    { type: "pcSearchResult", url: "pcSearch" }
] as const;
