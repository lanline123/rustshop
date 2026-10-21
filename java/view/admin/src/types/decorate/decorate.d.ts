import { AnyObject } from "ant-design-vue/es/_util/type";
import { ComputedRef } from "vue";
// 列表查询时筛选参数类型
export interface ModulesType {
    moduleList: ModuleType[];
    pageModule: ModuleType;
}

export interface EditResult {
    data: {
        moduleList?: ModuleType[];
        pageModule?: ModuleType;
    };
    children?: any[];
    draftData?: any;
    decorateTitle?: string;
    hasDraftData: boolean;
    decorateId?: any;
}
export type backgroundDefault = 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 0
export type ModuleTypeStyle = 1 | 2 | 3 | 4
export interface ModuleType {
    module: {};
    nociceEffect?: ScrollSpeedType;
    moduleIndex?: number;
    baseStyle?: number;
    isShow?: boolean;
    active?: boolean;
    fixed?: boolean;
    // 页面标题是否沉浸式（仅个人中心页 pageModule 使用）：开启后标题栏透明，首个用户信息样式 2/3 的渐变背景溢出到页面顶部
    immersive?: boolean;
    type: string;
    url: string;
    text?: string;
    moreLink?: string;
    style: number | ModuleTypeStyle;
    lineType?: number;
    backgroundDefault?: number | backgroundDefault;
    lineStyle?: number;
    blankHeight?: number;
    backgroundColor?: string;
    customPic?: string;
    backgroundImage?: string;
    backgroundRepeat: string;
    backgroundSize: string;
    products?: ModuleProductsType;
    title?: ModuleTitleType;
    subTitle?: ModuleSubTitleType;
    frame?: ModuleFrameType;
    icoPic: ModuleIcoPicType;
    swiperPageColor?: string;
    swiperPageStyle?: number;
}
//分类导航结构
export interface ModuleCatNavType {
    navBackgroundPic?: ModuleIcoPicType;
    logoPic?: ModuleIcoPicType;
    itemWidth?: number;
    textColor?: string;
    backgroundColor?: string;
    itemBackgroundColor?: string;
    searchTextColor?: string;
    isGanged?: number;
    logoHeight?: number;
    searchText?: string;
    boxPadding?: number;
    itemRadius?: number;
    boxPaddingTop?: number;
    boxPaddingBottom?: number;
}
//优惠券结构
export interface ModuleCouponType {
    showTitle?: number;
    title?: string;
    desc?: string;
    colorStyle?: number;
    itemBackgroundColor?: string;
    backgroundColor?: string;
    itemPadding?: number;
    boxRadius?: number;
    boxPadding?: number;
    boxMargin?: number;
    boxPaddingTop?: number;
    boxPaddingBottom?: number;
    titleColor?: string;
}
//活动结构
export interface ModuleActivityType {
    style?: number;
    listStyleType?: string;
    goodsStyle?: number;
    goodsRadius?: number;
    goodsRadioStyle?: number;
    textAlign?: number;
    textWeight?: number;
    goodsNameRow?: number;
    goodsNamePadding?: number;
    showName?: number;
    showBrief?: number;
    showPrice?: number;
    goodsPadding?: number;
    buyBtnStyle?: number;
    imageBackgroundColor?: string;
    backgroundColor?: string;
    boxRadius?: number;
    innerPadding?: number;
    boxPadding?: number;
    boxPaddingTop?: number;
    boxPaddingBottom?: number;
    title?: ModuleTitleType;
    waterfall?: number;
    groupType?: number;
    crossedOutPrice?: number;
    grouponActivityItem?: any;// 活动内容
    cardStyle?: number;
}
// 结构
export interface ModuleFrameType {
    textColor?: string;
    itemBackgroundColor?: string;
    itemRadius?: string;
    itemHeight?: string;
    backgroundColor?: string;
    innerPadding?: number;
    innerPaddingLeftRight?: number;
    innerPaddingTopBottom?: number;
    boxRadius?: number;
    boxPadding?: number;
    boxPaddingTop?: number;
    boxPaddingBottom?: number;
    leftAndRightPadding?: number;
    format?: {
        backgroundColor?: string;
        innerPadding?: string;
        boxRadius?: string;
        boxPadding?: string;
        boxPaddingTop?: string;
        boxPaddingBottom?: string;
    };
}

type GradientType = 'upDown' | 'leftRight' | 'diagonal' | 'purity';
type PicFillType = 'cover' | 'contain';
type TitleFontSize = 12 | 14 | 16
type BigTitleFontSize = 14 | 16 | 18
type TitleFontBaold = 400 | 700
type PicPageType = 1 | 2

export interface ModuleStyle {
    boxPaddingTopBottom: number;
    boxPaddingTop: number;
    boxPaddingBottom: number;
    boxPadding: number;
    boxMargin?: number;
    backgroundColor: string;
    backgroundPic: {
        picUrl: string;
        picThumb: string;
    };
    backgroundPicFillType: PicFillType;
}
export interface ModuleContentStyle {
    paddingTopBottom: number;
    paddingTop: number;
    paddingBottom: number;
    paddingLeftRight: number;
    padding: number;
    boxRadiusTop: number;
    boxRadiusBottom: number;
    boxRadius: number;
    gradientType: GradientType;
    gradientColorA: string;
    gradientColorB: string;
    backgroundColor: string;
    backgroundPic: {
        picUrl: string;
        picThumb: string;
    };
    backgroundPicFillType: PicFillType;
    boxShadow: number;
}

export interface ModuleSubTitle {
    showTitle?: number;
    titleFontSize?: number;
    titleFontBold?: number;
    titleColor?: string;
}

// 标题

export interface ModuleTitleType {
    showTitle?: number;
    titleStyle?: number;
    titleAlign?: number;
    titleBackground?: string;
    titleBackground2?: string;
    titleBackgroundPic?: {
        picUrl?: string;
        picThumb?: string;
    };
    titleRadius?: number;
    titleText?: string;
    titleColor?: string;
    titleWeight?: string;
    descText?: string;
    descColor?: string;
    showMore?: number;
    /** 标题右侧内容类型：1=显示更多链接，2=显示倒计时。未设置时由各模块的老语义推导 */
    moreType?: number;
    /** 倒计时前缀文字（与"更多"的 moreLink.title 相互独立） */
    countdownLabel?: string;
    moreLink?: {
        link?: string;
        title?: string;
    };
    moreColor?: string;
    format?: {
        titleBackground?: string;
        titleRadius?: string;
    };
}

/**
 * 移动端登录页装修模块（decorateType=1, pageType=5, type=mobileLogin）
 * 仅颜色 + 边距；全部可空，空值由 uniapp 登录页兜底（CSS 变量 fallback）。
 */
export interface MobileLoginType {
    /** 主标题色 / 跨境 Tab 选中色 */
    titleColor?: string;
    /** 次要文字色（Tab未选/提示/说明） */
    subTextColor?: string;
    /** 输入框文字色 */
    inputTextColor?: string;
    /** 占位符色 */
    placeholderColor?: string;
    /** 强调 / 协议链接色 */
    linkColor?: string;
    /** 主按钮色 */
    buttonColor?: string;
    /** 分割线 / 输入下划线色 */
    dividerColor?: string;
    /** 内容区左右边距（px，Admin 直接用 px，uniapp 按 px*2 rpx 换算） */
    contentPaddingX?: number;
    /** 内容区顶部间距（px，Admin 直接用 px，uniapp 按 px*2 rpx 换算） */
    contentPaddingTop?: number;
}

export interface ModuleImageType {
    picType: number;
    picPageType: number;
    navType?: number;
    navStyle?: number;
    rowNum?: number;
    colNum?: number;
    radioStyle?: number;
    swiperPageColor?: string;
    swiperPreView?: number;
    imgPadding?: number;
    imgRadius?: number;
    isFluxWidth?: number;
    picRadioStyle?: number;
    boxPaddingBottom?: number;
    boxPaddingTop?: number;
    picList: ModulePicListType[];
    showIndicator?: number;
    format?: {
        imgPadding?: string;
    };
}
// 图片橱窗 imageShowcase
// 比例字面量；'default' 表示不锁比例，按图片原始尺寸
export type ImageShowcasePicRatio = 'default' | '3:2' | '1:1' | '3:4' | '2:1' | '16:9';
export type ImageShowcaseListStyle = 'fixed' | 'scroll';
export type ImageShowcasePicFillType = 'cover' | 'contain';

export interface ImageShowcaseType {
    picList: ModulePicListType[];
    rowNum: 1 | 2 | 3 | 4 | 5 | 6;
    listStyle: ImageShowcaseListStyle;
    picRatio: ImageShowcasePicRatio;
    picFillType: ImageShowcasePicFillType;
    picRadius: number; // 圆角大小 0-30；0 等同直角
    imgPadding: number;
    boxShadow: 0 | 1;
    // 与既有 mobilePro 模块（customProduct/specialOffer 等）的字段命名约定一致
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}

// 热图
export interface ModuleImageHotareaType {
    picList?: ModuleImageHotareaPicListType[];
    imgPadding: number;
    picRadioStyle?: number;
    format?: {
        imgPadding?: string;
    };
}
export interface ModuleImageHotareaPicListType {
    picTitle?: string;
    picUrl?: string;
    picDesc?: string;
    hotarea?: HotareaType[];
}

export interface HotareaType {
    link?: LinkType;
    title?: string;
    width?: number;
    height?: number;
    left?: number;
    top?: number;
    position?: string;
}

// 商品
export interface ModuleProductsType {
    productSelectType?: number;
    productIds?: number[];
    productCategoryId?: number;
    productNumber?: number;
    productTag?: string;
}
export interface ModulePicListType {
    picUrl?: string;
    picThumb?: string;
    picLink?: {
        link?: string;
        title?: string;
    };
    picTitle?: string;
    picDesc?: string;
    gradientColorA?: string;
    gradientColorB?: string;
    isShow?: boolean;
}
export interface ModuleLinkListType {
    link?: {
        link?: string;
        title?: string;
    };
    linkTitle?: string;
    picTitle?: string;
}

//公告图标
export interface ModuleIcoPicType {
    picUrl?: string;
    picThumb?: string;
    picId?: number;
    picName?: string;
}

export interface FilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    isShow?: number;
}

// 获取列表返回参数类型
export interface FilterResult {
    records: FilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface FilterState {
    exampleId?: number;
    exampleName?: string;
    exampleLogo?: string;
    isShow?: boolean;
    sortOrder?: number;
}

// 获取详情返回参数类型
export interface FormResult {
    item: FormState;
}
export interface FormState {
    exampleName?: string;
    exampleLogo?: string;
    exampleDesc?: string;
    sortOrder?: string;
    isShow?: number;
}
// PC端
export interface PcPageHeaderType {
    logo?: {
        picUrl?: string;
        picThumb?: string;
    };
    headerStyle?: number;
}
export interface PcCatProductType {
    categoryId?: number;
    productSelectType?: number;
    productCategoryId?: number;
    productTag?: number;
    baseStyle?: number;
    title?: string;
    moreLink?: LinkType;
    shortTitle?: string;
    enTitle?: string;
    color?: string;
    color2?: string;
    picList?: ModulePicListType[];
    picList2?: ModulePicListType[];
    picList3?: ModulePicListType[];
    linkList?: ModuleLinkListType[];
    productIds?: number[];
    catIds?: number[];
}
export interface PcBannerType {
    logo?: {
        picUrl?: string;
        picThumb?: string;
    };
    bannerStyle?: number;
    showCat?: number;
    picList?: ModulePicListType[];
    picList2?: ModulePicListType[];
    picList3?: ModulePicListType[];
    number?: number;
}

export interface PcCustomAd1Type {
    picList?: ModulePicListType[];
    picList2?: ModulePicListType[];
    picList3?: ModulePicListType[];
}
export interface PcCustomAd2Type {
    linkList?: ModuleLinkListType[];
    picList?: ModulePicListType[];
    picList2?: ModulePicListType[];
    picList3?: ModulePicListType[];
}
export interface PcRecommendProduct {
    titleName?: string;
    products?: ModuleProductsType;
}
export interface PcSeckillType {
    seckillTitle?: string;
}

export interface LinkType {
    path?: string;
    name?: string;
    label?: string;
    id?: number;
    sn?: string;
    link?: string;
    appLink?: string;
    data?: AnyObject;
}

export interface PcImageAd {
    picList?: ModulePicListType[];
    isFluxWidth?: number;
    boxPaddingTop?: number;
    boxPaddingBottom?: number;
}

// 店铺
export interface ShopInfo {
    customPic: ShopInfoCustomPic;
    backgroundDefault: number;
    style: number;
    frame: ShopInfoFrame;
}

export interface ShopInfoCustomPic {
    picUrl: string;
    picThumb: string;
    picId: number;
    picName: string;
}

export interface ShopInfoFrame {
    textColor: string;
    itemBackgroundColor: string;
    backgroundColor: string;
    innerPadding: number;
    itemHeight: number;
    itemRadius: number;
    boxRadius: number;
    boxPadding: number;
    boxPaddingTop: number;
    boxPaddingBottom: number;
}

export interface ModuleCategoryType {
    listStyleType?: string | number;
    categoryList: ModulePicListType[];
    rowNum: number;
    animation?: AnimationType;
    picFillType?: PicFillType;
    picPercent?: number;
    rowPicPercent: number;
    barWidth: number;
    barHeight: number;
    radius?: number;
    indicatorBar?: number;
    categoryPadding?: number;
    barColorOn: string;
    barColor: string;
    subTitle?: ModuleSubTitleType;
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}
export interface ModuleSubTitleType {
    showTitle?: number;
    titleColor?: string;
    titleFontSize?: number;
    titleFontBold?: number;
}
export interface ModuleStyleType {
    boxPaddingTopBottom?: number;
    boxPaddingTop?: number;
    boxPaddingBottom?: number;
    boxPadding?: number;
    boxMargin?: number;
    bannerMarginTop?: number;
    backgroundColor?: string;
    backgroundPic?: {
        picUrl?: string;
        picThumb?: string;
    };
    backgroundPicFillType?: PicFillType;
}
export interface ContentStyleType {
    padding?: number;
    paddingTopBottom?: number;
    paddingTop?: number;
    paddingBottom?: number;
    paddingLeftRight?: number;
    boxRadiusTop?: number;
    boxRadiusBottom?: number;
    boxRadius?: number;
    gradientType?: GradientType;
    gradientColorA?: string;
    gradientColorB?: string;
    backgroundColor?: string;
    backgroundPic?: {
        picUrl?: string;
        picThumb?: string;
    };
    backgroundPicFillType?: string | number;
    boxShadow?: string;
}

// 图片环东效果
export interface AnimationType {
    type?: string;
    duration?: number;
    delay?: number;
    range?: number;
}
export interface ModuleCategoryA1Type {
    logoPic?: ModuleIcoPicType;
    listStyleType?: string;
    imageBackgroundColor?: string;
    categoryList: ModulePicListType[];
    rowNum: number;
    animation?: AnimationType;
    picFillType?: string | number;
    picPercent?: number;
    rowPicPercent: number;
    radius?: number;
    categoryPadding?: number;
    subTitle?: ModuleSubTitleType;
    showSplitLine?: number;
    radiusStyle?: number;
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}

export interface ModuleCategoryNav3Type {
    logoPic?: ModuleIcoPicType;
    listStyleType?: string;
    imageBackgroundColor?: string;
    categoryList: ModulePicListType[];
    rowNum: number;
    animation?: AnimationType;
    picFillType?: string | number;
    picPercent?: number;
    rowPicPercent: number;
    radius?: number;
    categoryPadding?: number;
    subTitle?: ModuleSubTitleType;
    showSplitLine?: number;
    radiusStyle?: number;
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
    picList?: ModulePicListType[];
    label: string;
    titleText: string;
    showMore: number;
    showMoreText: string;
}

export interface ModuleCategoryA2Type {
    listStyleType?: string | number;
    categoryList?: ModulePicListType[];
    rowNum?: number | string;
    rowPicPercent: number;
    animation?: AnimationType;
    picFillType?: string | number;
    picPercent?: number;
    radius?: number;
    categoryPadding?: number;
    subTitle?: ModuleSubTitleType;
    indicatorBar?: number;
    barWidth?: number;
    barHeight?: number;
    barWidthOn?: number;
    barColor?: string;
    barColorOn?: string;
    imageBackgroundColor?: string;
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}


export interface ModuleBannerType {
    logoPic?: ModuleIcoPicType;
    logoHeight?: number;
    bannerContent: BannerContentType;
    searchContent?: BannerSearchContent;
    navContent?: BannerNavContent;
    bannerStyle?: BannerStyleType;
    moduleStyle?: ModuleStyleType;
}
export interface BannerContentType {
    picList: ModulePicListType[];
    picRadius?: number;
    height?: number;
    backgroundRadius?: number;
    gradientType?: GradientType;
    picPageType?: PicPageType;
    swiperPageColor?: string;
    indicatorColor?: string;
}
type menuIcon = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 'custom'
type signIcon = 0 | 1 | 2 | 3 | 4 | 'custom'
export interface BannerSearchContent {
    showSearch?: number;
    searchText?: string;
    menuIcon?: menuIcon;
    menuLink?: {
        name?: string;
        path?: string;
        label?: string;
    };
    menuPic?: ModulePicListType;
    signIcon?: signIcon;
    signLink?: {
        name?: string;
        path?: string;
        label?: string;
    };
    signPic?: ModulePicListType;
}

export interface BannerNavContent {
    showNav?: number;
    navList?: ModuleLinkListType[];
}
type EffectType = "default" | "ceiling"
type TextAlignmentType = "left" | "center" | "right"
type ProductListStyleType = 1 | 2 | 3 | 4 | 5
export interface BannerStyleType {
    effectType?: EffectType;
    searchBackgroundColor?: string;
    searchIconColor?: string;
    searchFontColor?: string;
    areaTextColor?: string;
    areaSwitchColor?: string;
    showSearchBtn?: number;
    searchText?: string;
    searchHeight?: number;
    searchRadius?: number;
    searchIconSize?: number;
    navPrefix?: string;
    navFontColor?: string;
    navBackgroundColor?: string;
    navPrefixColor?: string;
    textAlignment?: TextAlignmentType;
    navFontBold?: number;
    navLabelMargin?: number;
    navFontSize?: number;
}

export interface ModuleSlidingImageType {
    picList?: ModulePicListType[];
    backgroundGradientType?: GradientType;
    picFillType?: PicFillType;
    picMargin?: number;
    picPadding?: number;
    picRadius?: number;
    picReducePercent?: number;
    subTitle?: ModuleSubTitleType;
    showDesc?: number;
    descColor?: string;
    moduleStyle?: ModuleStyleType;
    autoPlay?: number;
    interval?: number;
    picWidth: number;
}
type SlidingEffectType = "auto" | "manual"
type FontPositionType = "internal" | "external"
export interface ModuleSlidingLargeImageType {
    picList: ModulePicListType[];
    rowNum: number;
    slidingEffect?: SlidingEffectType;
    slidingSpeed?: number;
    picMarginTop?: number;
    picMargin?: number;
    picRadius?: number;
    picFillType?: PicFillType;
    picTitleDesc?: TitleDescType;
    moduleTitle?: SlidingLargeImageTitleType;
    moduleStyle: ModuleStyleType;
    contentStyle?: ContentStyleType;
}
export interface TitleDescType {
    show?: number;
    titleColor?: string;
    titleFontSize?: number;
    titleFontWeight?: number;
    descColor?: string;
    descFontSize?: number;
    descFontWeight?: number;
    fontPosition?: FontPositionType,
    textAlignment?: TextAlignmentType;
    marginTop?: number;
    marginBottom?: number;
    marginLeft?: number;
    backgroundColor?: string;
}
type TitlePosition = "top" | "bottom"
export interface SlidingLargeImageTitleType {
    titleText?: string;
    subTitleText?: string;
    titlePosition?: TitlePosition;
    textAlignment?: TextAlignmentType;
    titleMarginTop?: number;
    titleMarginBottom?: number;
    titleMarginLeft?: number;
    titleColor?: string;
    titleFontSize?: number;
    titleFontWeight?: number;
    subFontTitleColor?: string;
    subFontTitleSize?: number;
    subFontTitleWeight?: number;
}
type SalePriceDecimal = 1 | 2 | 0
export interface ModuleComboSuggestionsEditType {
    comboGroups?: ComboGroupType[];
    regularPriceColor?: string;
    regularPriceBgColor?: string;
    regularPriceRadius?: number;
    regularPriceSize?: number;
    saleTagBackgroundColorA?: string;
    saleTagBackgroundColorB?: string;
    saleTagFontColor?: string;
    salePriceBackgroundColor?: string;
    salePriceFontColor?: string;
    salePriceDecimal?: SalePriceDecimal;
    imageRadius?: number;
    soldOutIcon?: number;
    backgroundGradientType?: GradientType;
    gradientColorA?: string;
    gradientColorB?: string;
    groupTitleColor?: string;
    bannerTittleColor: "#ffffff",
    bannerDotColor: "#ffffff",
    groupRadius?: number;
    groupShadow?: number;

    moduleStyle?: ModuleStyleType;
    // carouselDirection? : string;  //轮播方向  横向 horizontal  纵向 Vertical
    autoPlay?: number;
    interval: number;

}
type ComboType = "image" | "product"
export interface PruductList {
    productId?: number;
    productSn?: string;
    productName?: string;
    productDesc?: string;
    productPrice?: number;
    marketPrice?: number;
    picThumb?: string;
}
export interface ComboGroupType {
    titleImage: ModulePicListType;
    groupTitle?: string;
    groupSubTitle?: string;
    groupDesc?: string;
    groupColor?: string;
    groupType?: ComboType;
    picList?: ModulePicListType[];
    pruductList?: PruductList[];
    moreLink?: ModuleLinkListType;
}
export interface ComboGroupNoticeType {
    notices: ModulePicListType[];
}

type ScrollSpeedType = "scroll" | "still";
type NoticeIconType = "default" | "hide" | "custom";
export interface ModuleNoticeA2Type {
    noticeContent?: string;
    nociceLink?: {
        name?: string;
        path?: string;
        label?: string;
    };
    nociceEffect?: ScrollSpeedType;
    scrollSpeed?: number;
    textAlignment?: TextAlignmentType;
    noticeIconType?: NoticeIconType;
    iconPic?: ModuleIcoPicType;
    contentColor?: string;
    closeIcon?: number;
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}
export interface ProductGroupsType {
    productGroupId?: number;
    productGroupName?: string;
    menuTitle?: string;
    productIds?: number[];
    productNumType?: string;
    productNum?: number | string;
    title?: string;
    subTitle?: string;
    bannerPic?: ModulePicListType;
    bannerLink?: ModuleLinkListType;
}
type ProductSourceType = "products" | "productGroup"
type ModuleColorStyle = "orange" | "red" | "blue" | "green" | "purple" | "grey" | "chocolate" | "brown"
type CountdownType = "dailyCycle" | "specifyTime"
type PicScaleType = "3:2" | "1:1" | "3:4" | "16:9"

export interface ModuleSpecialOfferType {
    productSource?: ProductSourceType;
    productGroups: ProductGroupsType[];
    productList: PruductList[];
    moduleColorStyle?: ModuleColorStyle;
    prodcutTitles: any[];
    showProdcutTitles?: number;
    moduleTitle: {
        title?: string;
        subTitle?: string;
        countdownTitle?: string;
        countdownType?: CountdownType;
        dailyTime: number[];
        specifyTime?: number;
        topRightType?: SpecialOfferTopRightType;
        buttonText?: string;
        buttonLink?: LinkType;
    };
    picScale?: PicScaleType;
    picFillType?: PicFillType;
    productWidth?: number;
    productRadius?: number;
    showProductName?: number;
    productNameSize?: number;
    productNameWeight?: number;
    showProductPrice?: number;
    priceDecimal?: SalePriceDecimal;
    priceWeight?: number;
    showSellingPoints?: number;
    soldOutIcon?: number;
    moduleColor: {
        titleColor?: string;
        subTitleColor?: string;
        countdownColor?: string;
        countdownTitleColor?: string;
        productNameColor?: string;
        sellingPointsColor?: string;
        buttonTextColor?: string;
        buttonGradientColorA?: string;
        buttonGradientColorB?: string;
    };
    moduleStyle?: ModuleStyleType;
    contentStyle: ContentStyleType;
}

export type SpecialOfferTopRightType = "countdown" | "button";
type MorePosition = "top" | "auto"
export interface ModuleMixedProductDisplayType {
    productGroups: ComboGroupType[];
    rightPic?: ModulePicListType;
    showCountdown?: number;
    countdownType?: CountdownType;
    dailyTime: number[];
    specifyTime?: number;
    timeContent?: string[];

    picScale?: PicScaleType;
    picFillType?: PicFillType;
    productRadius?: number;
    morePosition?: MorePosition;

    showProductPrice?: number;
    priceWeight?: number;
    crossedOutPrice?: number;
    productSales?: number;

    moduleColor: {
        titleColor?: string;
        sellingPointColor?: string;
        countdownBackgroundColor?: string;
        countdownColor?: string;

        priceBackgroundColor?: string;
        priceColor?: string;
        crossedOutPriceColor?: string;
        lineColor?: string;
    },

    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}

export interface ModuleMixedNoticeDisplayType {
    noticeGroups: ComboGroupNoticeType[];

    picScale?: PicScaleType;
    picFillType?: PicFillType;

    moduleColor: {
        titleColor?: string;
        sellingPointColor?: string;
    },

    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}

export interface CategoryRecItem {
    title?: string;
    link?: ModuleLinkListType,
    titleColor?: string;
    backgroundColor?: string;
    catPic?: ModulePicListType;
}
export interface ModuleCategoryRecType {
    categoryList?: CategoryRecItem[];
    animation?: AnimationType;
    categoryPadding?: number;
    radius?: number;
    subTitle?: ModuleSubTitleType;
    backgroundColor?: string;
    catBorder?: number;
    catBorderWidth?: number;
    catBorderColor?: string;
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}
export interface CategoryRecA1Type {
    picList?: ModulePicListType[];
    picFillType?: 'cover' | 'contain';
    picRadius?: number;
    subTitle?: ModuleSubTitleType;
    subTitleColor?: string;
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}
export interface ModuleCountdownType {
    pruductList?: PruductList[];
    moduleColorStyle?: ModuleColorStyle;
    moduleTitle: {
        title?: string;
        subTitle?: string;
        moreText?: string;
        titleLink?: LinkType;
    };
    countdownData: {
        showCountdown?: number;
        countdownBackgroundPic?: ModulePicListType;
        countdownType?: CountdownType;
        dailyTime: number | Date;
        specifyTime?: number;
        timeContent?: string[];
        marginTop?: number;
        countdownPicBottom?: number;
    };
    rowNum: number;
    picScale?: PicScaleType;
    picFillType?: PicFillType;
    textAlignment?: TextAlignmentType;
    productPadding?: number;
    productRadius?: number;
    picBottomRadius?: number;
    showProductName?: number;
    showProductPrice?: number;
    headlines: number;
    priceWeight?: number;
    crossedOutPrice?: number;
    productSales?: number;
    moduleColor: {
        titleColor?: string;
        subTitleColor?: string;
        tipsColor?: string;
        productNameColor?: string;
        productBackgroundColor?: string;
        productPriceColor?: string;
        crossedOutPriceColor?: string;
        countdownColor?: string;
    }

    moduleStyle?: ModuleStyleType;
    contentStyle: ContentStyleType;
}

export interface ModuleGroupsType {
    title?: string;
    subTitle?: string;
    titleLink?: ModuleLinkListType;
    backgroundColor?: string;
    label: string;
    productList?: ProductGroupsType[];
    // 分组首格自定义图片（单图）：productGrouping 模块消费；存在时画布第一格替换为该图片（跳转链接存放在 bannerPic.picLink 中，由 PicList 内置编辑）
    bannerPic?: ModulePicListType;
    // @deprecated 早期独立链接字段，已合并到 bannerPic.picLink；保留以兼容历史草稿数据
    bannerLink?: ModuleLinkListType;
    moreLink?: ModuleLinkListType;
}
export interface ModuleGroupSlidingType {
    groupList?: ModuleGroupsType[]; //分组数据
    groupWidth?: number; //分组宽度
    groupRadius?: number; //分组上下圆角
    productPicRatio?: number; //商品图片占比
    productPicRadius?: number; //商品图片圆角
    productPicRightRadius?: number; //商品图片右侧圆角 0为无圆角 1为圆角

    showRanking?: number; //是否显示排名 0为不显示 1为显示
    showProductLabel?: number; //是否显示商品标签 0为不显示 1为显示

    showProductName?: number; //是否显示商品标题 0为不显示 1为显示
    productNameRow: number; //商品标题行数 1为单行 2为双行
    productNameWeight?: number; //商品标题字体粗细
    productNameSize?: number; //商品标题字体大小

    showProductPrice?: number; //是否显示商品价格 0为不显示 1为显示
    productPriceWeight?: number; //商品价格字体粗细
    productPriceSize?: number; //商品价格字体大小
    showDiscountLabel?: number; //是否显示商优惠价标签 0为不显示 1为显示
    productPriceMarginTop?: number; //商品价格上距

    crossedOutPrice?: number; // 商品划线价 0?:不显示 1?:显示

    buyBtnStyle: BuyButtonStyleType; //购买按钮样式 0:不显示  1: 按钮样式1 2: 按钮样式2

    moduleColor: {
        titleSize?: number; // 模块大标题字体大小
        subTitleSize?: number; // 模块小标题字体大小
        titleColor?: string; // 模块大标题颜色
        subTitleColor?: string; // 模块小标题颜色
        titlePic?: ModulePicListType; // 模块大标题图标

        productBackgroundColor?: string; // 商品背景颜色
        productNameColor?: string; // 商品背景颜色
        labelBackgroundColor?: string; // 商品标签背景颜色
        labelTextColor?: string; // 商品标签文字颜色
        productPriceColor?: string; // 商品价格颜色
        crossedOutPriceColor?: string; // 商品划线价颜色
    };
    moduleStyle?: ModuleStyleType; // 模块样式
}

// 标签导航（mobilePro 桶；横滑彩色水滴形文字标签，每标签 = 标题 + 链接）
export interface ModuleTagNavGroupType {
    title?: string; // 标签标题
    link?: ModuleLinkListType; // 跳转链接
}
export interface ModuleTagNavColorType {
    colorA?: string; // 渐变起始色
    colorB?: string; // 渐变结束色
}
export interface ModuleTagNavType {
    groupList?: ModuleTagNavGroupType[]; // 标签列表（标题 + 链接）
    textWeight?: number; // 文本样式 400 常规 / 700 加粗
    groupGap?: number; // 标签间距
    groupPadding?: number; // 标签内部左右内边距
    categoryColors?: ModuleTagNavColorType[]; // 第 1~6 个分类的渐变色，标签按 index%6 循环取色
    titleColor?: string; // 标签标题文字色
    moduleStyle?: ModuleStyleType; // 模块样式
}

export interface GroupList {
    productGroupId?: number;  //商品分组id
    productGroupName?: string;  //商品分组名称
    title?: string;  //分组标题
    subTitle?: string;  //分组副标题
    menuTitle?: string;  //分组菜单标题
    productIds?: number[];  //分组商品id列表
    productNumType?: string;  //商品数量类型
    productNum?: number | string;  //商品数量
    bannerPic?: ModulePicListType;  //分组关联海报
    bannerLink?: ModuleLinkListType;  //分组关联链接
    productList: PruductList[];
}

type ProductCardStyleType = 1 | 2 | 3 | 4;
type BottomMoreBtnType = "text" | "image" | "close"
export interface ModuleProductGroupingType {
    productSource?: ProductSourceType;
    productGroups?: ProductGroupsType[];
    groupList: GroupList[];
    productCardStyle?: ProductCardStyleType;
    listStyle: number;
    picScale?: PicScaleType;
    picFillType?: PicFillType;
    textAlignment?: TextAlignmentType;
    cardMargin: number;
    cardPadding?: number;
    cardRadius?: number;
    picBottomRadius?: number;
    showProductName?: number;
    productNameRow: number;
    productNameWeight?: number;
    productNameSize?: number;
    showProductDesc?: number;
    showProductPrice?: number;
    productPriceWeight?: number;
    productPriceSize?: number;
    productPricePic?: ModulePicListType;
    productPriceMarginTop?: number;
    crossedOutPrice?: number;
    buyBtnStyle?: BuyButtonStyleType;
    bottomMoreBtn?: BottomMoreBtnType;
    bottomMoreBtnText?: string;
    bottomMoreBtnImage?: ModulePicListType;
    productColor: {
        infoTopPadding?: number;
        infoBottomPadding?: number;
        infoLeftRightPadding?: number;
        backgroundColor?: string;
        titleColor?: string;
        priceColor?: string;
        crossedOutPriceColor?: string;
        moreBtnBgColor?: string;
        moreBtnBorderColor?: string;
        moreBtnTextColor?: string;
    };
    groupTitle: {
        effectType?: EffectType;
        marginLeftRight?: number;
        showTitle?: number;
        bigTitleFontSize?: number;
        titleColor?: string;
        activeTitleColor?: string;
        showSubTitle?: number;
        titleFontSize?: number;
        subTitleColor?: string;
        activeSubTitleColor?: string;
        subTitleGradientType?: GradientType;
        gradientColorA?: string;
        gradientColorB?: string;
        subTitleRadius?: number;
        splitLineColor?: string;
        ceilingBgColor?: string;
    };
    moduleStyle?: ModuleStyleType;
    contentStyle: ContentStyleType;
}

export interface BuyButtonStyleType {
    showBtn?: number;
    btnType?: number; // 0:不显示 1~4:内置iconfont图标 5:自定义图片
    iconImage?: ModulePicListType; // btnType===5 时使用的自定义图片图标
    iconSize?: number; // 图标/图片尺寸（px），缺省回退 14
    text?: string;
    textColor?: string;
    textSize?: number; // 文字字号（px），缺省回退 12
    iconColor?: string;
    gradientType?: GradientType;
    gradientColorA?: string;
    gradientColorB?: string;
    radius?: number;
    paddingTopBottom?: number;
    paddingLeftRight?: number;
    wrap?: number; // 按钮换行显示 0:不换行 1:换行
    wrapAlign?: number; // 换行后对齐 1:居左 2:居中 3:居右
}

export interface ModuleCustomProductType {
    pruductList?: PruductList[];
    picUrl?: ModulePicListType;
    picLocation?: TextAlignmentType;
    animation?: AnimationType;
    picFillType?: PicFillType;
    productRadius?: number;
    picBottomRadius?: number;
    crossedOutPrice?: number;
    moduleColor: {
        productBackgroundColor?: string;
        productNameColor?: string;
        productPriceColor?: string;
        crossedOutPriceColor?: string;
    }
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}

export interface ModuleArticleType {
    style?: number;
    articleCategoryId?: number[];
    articleNum?: number;
    showTime?: number;
    showPageView?: number;
    showLike?: number;
    picRadius?: number;
    articleNameWeight?: TitleFontBaold;
    moduleColor: {
        articleNameColor?: string;
        articleTimeColor?: string;
        articleViewColor?: string;
        articleLikeColor?: string;
        statisticColor?: string;
    };
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}
export interface ModuleImageTextComponentType {
    picUrl?: ModulePicListType;
    title?: string;
    description?: string;
    picBorder: number;
    picBorderColor?: string;
    picRadius?: number;
    titTextAlignment?: TextAlignmentType;
    titMarginBottom?: number;
    titColor?: string;
    descTextAlignment?: TextAlignmentType;
    descColor?: string;
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}


export interface ModulestoreNavType {
    borderRadius?: number;
    subTitle?: ModuleSubTitleType;
    /** 门店标题样式（颜色/大小/粗细） */
    storeTitleStyle?: ModuleSubTitleType;
    /** 标题上下间距（样式一） */
    titlePaddingY?: number;
    /** 标题左右间距（样式一） */
    titlePaddingX?: number;
    showDesc?: number;
    showFollow?: number;
    showRating?: number;
    /** 是否显示起送价 0:不显示, 1:显示（样式二） */
    showMinOrder?: number;
    /** 是否显示配送费 0:不显示, 1:显示（样式二） */
    showDeliveryFee?: number;
    /** 是否显示距离 0:不显示, 1:显示（样式二） */
    showDistance?: number;
    /** 门店项间距 */
    storeGap?: number;
    /** 门店背景颜色 */
    storeBackgroundColor?: string;
    /** 门店上下内边距 */
    storePaddingY?: number;
    /** 门店左右内边距 */
    storePaddingX?: number;
    /** 门店圆角半径 */
    storeRadius?: number;
    /** 商品项间距（样式二） */
    productGap?: number;
    /** 是否显示商品名称 0:不显示, 1:显示（样式二） */
    showProductName?: number;
    /** 商品名称行数 1/2（样式二） */
    productNameRow?: number;
    /** 是否显示商品价格 0:不显示, 1:显示（样式二） */
    showProductPrice?: number;
    descColor?: string;
    moduleStyle?: ModuleStyleType;
    contentStyle?: ContentStyleType;
}

export interface ProductA1Type {
    showProductLabel?: number;
    tagList?: string[];
    labelBackgroundColor?: string;
    labelTextColor?: string;
    labelRadius?: number;
    labelRadiusStyle?: number;
    labelFontSize?: number;
    labelPosition?: "top-left" | "top-right" | "bottom-left" | "bottom-right";
    labelOffsetX?: number;
    labelOffsetY?: number;
    showMarketPrice?: number;
    nameAlign?: number;
    priceAlign?: number;
    briefList?: string[];
    briefStyle?: ModuleSubTitleType;
    briefBackgroundColor?: string;
    itemGap?: number;
    buyBtnStyle?: BuyButtonStyleType; // 购买按钮（复用共享 BuyBtnEdit / CommonBuyButton 系统）
}

export interface NewcomerCouponItem {
    couponId: number;
    btnText?: string;
}

export interface NewcomerComboType {
    showHeader?: number;
    headerTitle?: string;
    headerSubTitle?: string;

    showSection1?: number;
    section1Badge?: string;
    section1Title?: string;
    section1MoreLink?: LinkType;
    products?: ModuleProductsType;
    productBriefList?: string[];
    productImgRadius?: number;
    productPicRatio?: "default" | "1:1" | "3:4" | "3:2";
    productPicFillType?: "cover" | "contain";
    productPercent?: number;
    showProductName?: number;
    productItemGap?: number;
    productItemPadding?: number;
    showMarketPrice?: number;
    buyBtnStyle?: number;

    showSection2?: number;
    section2Badge?: string;
    section2Title?: string;
    section2MoreLink?: LinkType;
    couponPerRow?: 1 | 2 | 3 | 4;
    coupons?: NewcomerCouponItem[];

    productTitleColor?: string;
    productBriefColor?: string;
    productPriceColor?: string;
    productMarketPriceColor?: string;
    productBriefGradientStart?: string;
    productBriefGradientEnd?: string;
    productBriefTextColor?: string;
    buyBtnGradientStart?: string;
    buyBtnGradientEnd?: string;
    buyBtnTextColor?: string;

    couponBgGradientStart?: string;
    couponBgGradientEnd?: string;
    couponAmountColor?: string;
    couponConditionColor?: string;
    couponBtnTextColor?: string;

    boxGradientStart?: string;
    boxGradientEnd?: string;
    containerBgImage?: {
        picUrl?: string;
        picThumb?: string;
    };
    contentBgColor?: string;
    decorationBgColor?: string;

    frame?: ModuleFrameType;
}

// 限时特惠（mobilePromotion 桶；整体背景图容器 + 右侧横滑商品橱窗）
export interface LimitedSpecialType {
    // 容器
    containerBgImage?: {
        picUrl?: string;
        picThumb?: string;
    };
    boxGradientStart?: string;
    boxGradientEnd?: string;
    productAreaLeft?: number;
    productAreaPaddingY?: number;
    moreLink?: LinkType;

    // 商品数据
    products?: ModuleProductsType;

    // 商品展示
    productPercent?: number;
    productItemGap?: number;
    productItemPadding?: number;
    productPicRatio?: "default" | "1:1" | "3:4" | "3:2";
    productPicFillType?: "cover" | "contain";
    productImgRadius?: number;
    cardRadius?: number;
    textAlign?: "left" | "center" | "right";
    textWeight?: number;
    priceFontSize?: number;
    priceBold?: number;

    // 显隐
    showProductName?: number;
    showPrice?: number;
    showMarketPrice?: number;
    showSales?: number;
    showComment?: number;
    buyBtnStyle?: number;
    showBrief?: number;
    productBriefList?: string[];

    // 配色
    cardBgColor?: string;
    productTitleColor?: string;
    productPriceColor?: string;
    productMarketPriceColor?: string;
    productSalesColor?: string;
    briefGradientStart?: string;
    briefGradientEnd?: string;
    briefTextColor?: string;
    buyBtnGradientStart?: string;
    buyBtnGradientEnd?: string;
    buyBtnTextColor?: string;

    frame?: ModuleFrameType;
}

export interface ProductSlideType {
    // 商品数据（手选为主）
    products?: ModuleProductsType;

    // 逐卡主题（按手选 productIds 顺序 index 对应）
    cardTitleList?: string[]; // 逐卡自定义主标题（服饰专场），始终显示
    cardGradientStartList?: string[]; // 逐卡背景渐变起
    cardGradientEndList?: string[]; // 逐卡背景渐变终

    // 卡片 / 图片展示
    cardStyle?: "noBorderWhite" | "shadow" | "border" | "transparent"; // 商品层叠样式
    productPicFillType?: "cover" | "contain";
    productImgRadius?: number;
    showImagePlatform?: number; // 图片下平台
    productPicRatio?: "default" | "1:1" | "3:4" | "3:2";
    textWeight?: number;
    textAlign?: "left" | "center" | "right";
    layerGap?: number; // 商品层间距（卡内层间距）
    productItemGap?: number; // 商品间间距（卡与卡间距）
    productPercent?: number;
    cardRadius?: number;

    // 显隐
    showProductName?: number;
    showFeature?: number;
    showBrief?: number;
    showPrice?: number;
    showSales?: number;
    showMarketPrice?: number;
    buyBtnStyle?: number;
    showSlidePeek?: number; // 划入查看（admin 仅存配置）
    detailJumpMode?: "default" | "native"; // 商详跳转模式（admin 仅存配置）

    // 价格细节
    priceFontSize?: number;
    priceBold?: number;

    // 配色（全局）
    cardTitleColor?: string; // 商品标题色（hero 服饰专场）
    briefColor?: string; // 商品卖点色
    descColor?: string; // 商品描述文字色（含商品名称）
    productPriceColor?: string;
    productMarketPriceColor?: string;
    productSalesColor?: string;
    badgeColor?: string; // 角标色
    buyBtnTextColor?: string;
    buyBtnGradientStart?: string;
    buyBtnGradientEnd?: string;

    // 模块设置
    innerBgColor?: string; // 模块内部背景色（卡轨道容器底色）
    moduleBgImage?: {
        picUrl?: string;
        picThumb?: string;
    };
    frame?: ModuleFrameType;
}

// 商品轮播（mobilePro 桶；真·Swiper 露邻轮播：选商品自动带信息 + 拖拽排序 的经典白卡）
export type ProductCarouselCardStyle = "noBorderWhite" | "shadow" | "border" | "transparent";
export type ProductCarouselPicRatio = "default" | "1:1" | "3:4" | "3:2";
export type ProductCarouselBgMode = "normal" | "repeat" | "fixed";

export interface ProductCarouselType {
    productList?: PruductList[]; // 商品列表（复用 ProductInfoSelect / 组合推荐 选品组件，支持拖拽排序、自动带商品信息）

    // 模块标题
    showModuleTitle?: number; // 1 显示 / 0 隐藏
    moduleTitle?: string;
    moduleTitleColor?: string;
    titleContentGap?: number; // 标题与内容间距

    // 轮播
    autoplay?: number; // 0 不自动 / 1 自动播放
    cardWidthPercent?: number; // 中间卡宽度占比（控制两侧露出量）

    // 展示样式
    cardStyle?: ProductCarouselCardStyle; // 无边白底/卡片投影/描边白底/无边透底
    goodsRadius?: number; // 商品圆角
    picBottomRadius?: number; // 图片下圆角 0/1
    productPicRatio?: ProductCarouselPicRatio; // 图片比例
    textWeight?: number; // 文本样式 400/700
    textAlign?: "left" | "center" | "right"; // 文本对齐
    itemGap?: number; // 商品外间距（卡与卡间距）
    innerPadding?: number; // 商品内间距（卡内边距）

    // 显隐
    showName?: number; // 商品名称
    showDesc?: number; // 商品描述
    showPrice?: number; // 商品价格
    showMarketPrice?: number; // 商品原价（划线）
    showSales?: number; // 商品销量
    buyBtnStyle?: BuyButtonStyleType; // 购买按钮（复用「商品分组」的 BuyBtnEdit / CommonBuyButton 共享系统）

    // 配色
    productTitleColor?: string;
    descColor?: string;
    priceColor?: string;
    marketPriceColor?: string;
    salesColor?: string;
    badgeColor?: string;

    // 模块参数
    moduleBgImage?: {
        picUrl?: string;
        picThumb?: string;
    };
    bgImageMode?: ProductCarouselBgMode; // 背景图模式：常规充满/平铺/固定
    frame?: ModuleFrameType; // 左右/上/下边距 + 背景色
}
