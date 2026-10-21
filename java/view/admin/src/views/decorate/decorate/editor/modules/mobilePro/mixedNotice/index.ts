import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { MixedNoticeModule } from "./types";

// 演示公告分组是 defaultModule 字面量的一部分（旧 View.vue 的 groups 常量，契约冻结）
const groups = [
    {
        notices: [
            {
                picId: 1428,
                picThumb: "https://oss.tigshop.com/img/gallery/202507/1752202673o7FOMjRwwsNOXB8OjP.png?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202507/1752202673o7FOMjRwwsNOXB8OjP.png",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: "免运费",
                picDesc: "前三单购物均可免运费！"
            }
        ]
    },
    {
        notices: [
            {
                picId: 1428,
                picThumb: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: "24*7客户支持",
                picDesc: "随时随地提供保障"
            },
            {
                picId: 1428,
                picThumb: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: "24*7客户支持",
                picDesc: "随时随地提供保障"
            },
            {
                picId: 1428,
                picThumb: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202507/1752202678mQIAJqOXqUO54YdFfj.png",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: "24*7客户支持",
                picDesc: "随时随地提供保障"
            }
        ]
    }
];

/** 与旧 mobilePro/mixedNotice/View.vue 的 defaultModule 逐字段一致（契约） */
export const mixedNoticeDefaults = {
    noticeGroups: groups, // 公告分组
    moduleColor: {
        // 模块颜色设置
        titleColor: "#171B1E", // 模块大标题颜色
        sellingPointColor: "#999999" // 卖点小标题颜色
    },

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "mixedNotice",
    label: "公告混排",
    icon: '<i class="iconfont-admin icon-notice"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 12,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(mixedNoticeDefaults)
});
