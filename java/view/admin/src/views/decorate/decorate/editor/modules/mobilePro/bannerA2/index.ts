import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro, isStore } from "@/utils/version";

export type { BannerA2Module } from "./types";

const picList = [
    {
        picId: 1414,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101tXK31aimefqoUsJSrM.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picName: "FjfhKDiUzqW_lqfJqncKt3QJplx4"
    },
    {
        picId: 1415,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101j8COoqtfDJCWrSts6b.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picName: "Fv5iP2IHByVFUlGrLkXtPkClndla"
    },
    {
        picId: 1413,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731044101D4owQpnWiu0DcTqmKO.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731301927a0N31LV7yy1i6Zlk4T.webp",
        picName: "FhHfxuZP5Z5P_Hc7SHBzvm6RwHud"
    }
];

/** 与旧 mobilePro/bannerA2/View.vue 的 defaultModule 逐字段一致（契约） */
export const bannerA2Defaults = {
    bannerContent: {
        //轮播内容
        picList: picList, //图片列表
        picRadius: 0, //图片圆角

        picPageType: 1, //指示器样式
        swiperPageColor: "", //指示器选中颜色
        indicatorColor: "" //指示器默认颜色
    },
    searchContent: {
        //搜索框设置
        showSearch: 1, //是否显示搜索框 1显示 0不显示
        searchText: "搜索商品" //搜索框文字
    },
    // navContent: {
    //     //导航文字设置
    //     // showNav: 1, //是否显示导航 1显示 0不显示
    //     navList: navList ///导航列表
    // },
    bannerStyle: {
        //样式设置
        effectType: "ceiling", //搜索框/导航效果 ceiling 滚动至顶部固定  default 正常模式
        searchBackgroundColor: "#ffffff", //搜索框背景颜色
        searchFontColor: "#999999", //搜索框文本颜色
        searchIconColor: "#999999", //搜索框图标颜色
        searchText: "搜索", //搜索框文字
        searchHeight: 34, //搜索框高度
        searchRadius: 20 //搜索框圆角
    },
    moduleStyle: defaultModuleStyle // 模块样式
};

registerModule({
    type: "bannerA2",
    label: "首焦轮播A2",
    icon: '<i class="iconfont-admin icon-banner"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 17,
    enabled: () => isPro() && isStore(),
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(bannerA2Defaults)
});
