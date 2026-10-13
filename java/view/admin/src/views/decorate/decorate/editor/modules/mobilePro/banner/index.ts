import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { BannerModule } from "./types";

const navList = [
    {
        path: "",
        label: "",
        name: "",
        data: {
            name: "",
            path: ""
        },
        picTitle: "新品",
        picLink: {}
    },
    {
        path: "",
        label: "",
        name: "",
        data: {
            name: "",
            path: ""
        },
        picTitle: "百货"
    },
    {
        path: "",
        label: "",
        name: "",
        data: {
            name: "",
            path: ""
        },
        picTitle: "零食"
    },
    {
        path: "",
        label: "",
        name: "",
        data: {
            name: "",
            path: ""
        },
        picTitle: "折扣"
    },
    {
        path: "",
        label: "",
        name: "",
        data: {
            name: "",
            path: ""
        },
        picTitle: "猜你喜欢"
    }
];
const picList = [
    {
        picId: 1414,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101tXK31aimefqoUsJSrM.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picName: "FjfhKDiUzqW_lqfJqncKt3QJplx4",
        gradientColorA: "#62a05c",
        gradientColorB: "#72b76c"
    },
    {
        picId: 1415,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101j8COoqtfDJCWrSts6b.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picName: "Fv5iP2IHByVFUlGrLkXtPkClndla",
        gradientColorA: "#62a05c",
        gradientColorB: "#72b76c"
    },
    {
        picId: 1413,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101D4owQpnWiu0DcTqmKO.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301927a0N31LV7yy1i6Zlk4T.webp",
        picName: "FhHfxuZP5Z5P_Hc7SHBzvm6RwHud",
        gradientColorA: "#62a05c",
        gradientColorB: "#72b76c"
    }
];
const logo = {
    picUrl: "http://oss.tigshop.com/img/gallery/202409/1727056331nhxqtK8Mm5u8MXokoI.png",
    picThumb: "https://oss.tigshop.com/img/gallery/202409/1727056331nhxqtK8Mm5u8MXokoI.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picId: 1221,
    picName: "logoWhite"
};

/** 与旧 mobilePro/banner/View.vue 的 defaultModule 逐字段一致（契约） */
export const bannerDefaults = {
    logoPic: logo,
    logoHeight: 100, //logo高度
    bannerContent: {
        //轮播内容
        picList: picList, //图片列表
        picRadius: 6, //轮播图片圆角
        height: 200, //弧度背景高度
        backgroundRadius: 50, //弧度背景圆角
        gradientType: "upDown", //弧度背景方向
        picPageType: 1, //指示器样式
        swiperPageColor: "" //指示器颜色
    },
    searchContent: {
        //搜索框设置
        showSearch: 1, //是否显示搜索框 1显示 0不显示
        searchText: "搜索商品", //搜索框文字
        menuIcon: 1, //菜单图标选项
        menuLink: "", //菜单链接
        menuPic: {
            //自定义菜单图标
            picUrl: "",
            picThumb: ""
        },
        signIcon: 1, //签到图标选项
        signLink: "", //签到链接
        signPic: {
            //自定义签到图标
            picUrl: "",
            picThumb: ""
        }
    },
    navContent: {
        //导航文字设置
        showNav: 1, //是否显示导航 1显示 0不显示
        navList: navList ///导航列表
    },
    bannerStyle: {
        //样式设置
        effectType: "ceiling", //搜索框/导航效果 ceiling 滚动至顶部固定  default 正常模式
        searchBackgroundColor: "#ffffff", //搜索框背景颜色
        searchFontColor: "#999999", //搜索框文本颜色
        showSearchBtn: 1, //是否显示搜索按钮 1显示 0不显示
        searchText: "搜索", //搜索框文字
        searchHeight: 34, //搜索框高度
        searchRadius: 20, //搜索框圆角
        searchIconSize: 24, //搜索图标大小
        navPrefix: "热搜:", //导航文字前缀
        navFontColor: "#ffffff", //导航文字颜色
        navBackgroundColor: "rgba(255, 255, 255, 0.3)", //导航背景颜色
        navPrefixColor: "#ffffff", //导航前缀颜色
        textAlignment: "center", //导航文字对齐方式 left center right
        navFontBold: 400, //导航文字粗细
        navLabelMargin: 5, //导航文字间距
        navFontSize: 12 //导航文字大小
    },
    moduleStyle: defaultModuleStyle // 模块样式
};

registerModule({
    type: "banner",
    label: "首焦轮播",
    icon: '<i class="iconfont-admin icon-banner"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 16,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(bannerDefaults)
});
