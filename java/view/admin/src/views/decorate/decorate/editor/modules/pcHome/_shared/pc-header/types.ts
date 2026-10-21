/**
 * PC 首页头部预览（编辑器侧）类型与默认值
 * 照搬 Vort-Admin editor/modules/_shared/pc-header/types.ts
 * 仅用于编辑器画布预览：config 驱动渲染，不可配置、不进保存数据
 */

/** 图片项（logo/购物车图/提示图等） */
export interface PicItem {
    picId?: string;
    picUrl?: string;
    picThumb?: string;
    picName?: string;
}

/** benefit bar 提示项 */
export interface PcHeaderNoticeItem {
    picId?: string;
    picThumb?: string;
    picUrl?: string;
    picName?: string;
    picTitle?: string;
    picDesc?: string;
    picLink?: {
        link?: string;
        name?: string;
    };
}

/** benefit bar 分组 */
export interface PcHeaderNoticeGroup {
    notices: PcHeaderNoticeItem[];
}

/** 会员中心下拉菜单项 */
export interface PcHeaderMemberCenterItem {
    picTitle?: string;
    picDesc?: string;
    picLink?: {
        link?: string;
        name?: string;
        label?: string;
    };
    /** 兼容旧版会员中心菜单字段 */
    title?: string;
    link?: string;
}

/** PC 头部配置（与 Vort 同构，预览只读消费） */
export interface PcHeaderConfig {
    /** 头部风格：1=简约风，2=跨境风 */
    styleType?: 1 | 2;
    /** 底部分隔样式：0=不展示，1=阴影，2=边框 */
    bottomDividerType?: 0 | 1 | 2;
    isWhite?: number;
    showTopMenuBar?: 0 | 1;
    /**
     * 顶部菜单文字颜色
     * 简约风：地区 / 会员菜单 / 站点导航
     * 跨境风：提示条右侧购物车 / 站点导航 + benefit 提示（主副标题以各自设置项优先）
     */
    topMenuTextColor?: string;
    /** 顶部菜单文字悬浮颜色，作用于两种风格的顶部菜单链接 */
    topMenuTextHoverColor?: string;
    showTopRegion?: 0 | 1;
    topRegionText?: string;
    showTopUserMenu?: 0 | 1;
    showTopMemberCenter?: 0 | 1;
    memberCenterItems?: PcHeaderMemberCenterItem[];
    showTopMemberName?: 0 | 1;
    showTopMessage?: 0 | 1;
    showTopOrders?: 0 | 1;
    showTopSiteNav?: 0 | 1;
    showTopAd?: 0 | 1;
    topAdPic?: PicItem;
    logoPic?: PicItem;
    logoAlign?: 1 | 2 | 3;
    logoWidth?: number;
    logoHeight?: number;
    rightCartShow?: 0 | 1;
    cartStyle?: 1 | 2;
    cartPic?: PicItem;
    topMenuCartShow?: 0 | 1;
    searchStyle?: 1 | 2 | 3;
    searchColorType?: 1 | 2;
    searchColor?: string;
    showCategory?: 0 | 1;
    /** 首页分类：0=隐藏，1=显示 */
    showHomeCategory?: 0 | 1;
    categoryMarginTop?: number;
    showMainNav?: 0 | 1;
    mainNavPaddingTop?: number;
    mainNavPaddingBottom?: number;
    showBenefitBar?: number;
    benefitBarBackgroundColor?: string;
    benefitTitleColor?: string;
    benefitDescColor?: string;
    benefitNoticeGroups?: PcHeaderNoticeGroup[];
}

export const pcHeaderDefaults: PcHeaderConfig = {
    styleType: 1,
    isWhite: 1,
    showTopMenuBar: 1,
    topMenuTextColor: "",
    topMenuTextHoverColor: "",
    showTopRegion: 1,
    topRegionText: "中国江西",
    showTopUserMenu: 1,
    showTopMemberCenter: 1,
    memberCenterItems: [
        { picTitle: "我的足迹", picDesc: "", picLink: { link: "/member/browsingHistory/list", name: "我的足迹", label: "我的足迹" } },
        { picTitle: "我的优惠券", picDesc: "", picLink: { link: "/member/coupon/list", name: "我的优惠券", label: "我的优惠券" } }
    ],
    showTopMemberName: 1,
    showTopMessage: 1,
    showTopOrders: 1,
    showTopSiteNav: 1,
    showTopAd: 1,
    topAdPic: { picUrl: "", picThumb: "" },
    logoPic: { picUrl: "", picThumb: "" },
    logoAlign: 1,
    logoWidth: 100,
    logoHeight: 25,
    rightCartShow: 1,
    cartStyle: 1,
    cartPic: { picUrl: "", picThumb: "" },
    topMenuCartShow: 1,
    searchStyle: 1,
    searchColorType: 1,
    searchColor: "",
    showCategory: 1,
    showHomeCategory: 1,
    categoryMarginTop: 0,
    showMainNav: 1,
    mainNavPaddingTop: 0,
    mainNavPaddingBottom: 0,
    showBenefitBar: 1,
    benefitBarBackgroundColor: "",
    benefitTitleColor: "",
    benefitDescColor: "",
    benefitNoticeGroups: [
        {
            notices: [
                {
                    picId: "0",
                    picThumb: "",
                    picUrl: "",
                    picName: "",
                    picTitle: "免运费",
                    picDesc: "前三单购物均可免运费！",
                    picLink: { link: "", name: "" }
                }
            ]
        },
        {
            notices: [
                {
                    picId: "0",
                    picThumb: "",
                    picUrl: "",
                    picName: "",
                    picTitle: "24*7客户支持",
                    picDesc: "随时随地提供保障",
                    picLink: { link: "", name: "" }
                }
            ]
        }
    ]
};
