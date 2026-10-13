import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { NoticeA2Module } from "./types";

/** 与旧 mobilePro/noticeA2/View.vue 的 defaultModule 逐字段一致（契约） */
export const noticeA2Defaults = {
    noticeContent: "本店新用户首单0元起送，更多大牌好物低至6.6折起！！", // 公告内容
    nociceLink: {
        // 公告链接
        link: {
            link: "",
            title: ""
        }
    },
    nociceEffect: "scroll", // 公告滚动效果 滚动 scroll  静止 still
    scrollSpeed: 13, // 滚动效果为 scroll 时有效, 公告滚动耗时  13 秒
    textAlignment: "center", // 滚动效果 still 时有效, 公告内容对齐方式 左对齐 left  居中 center  右对齐 right
    noticeIconType: "default", // 公告图标类型  default 默认 hide 隐藏  custom 自定义
    iconPic: {
        picUrl: "",
        picThumb: ""
    }, // 自定义公告图标
    contentColor: "#333333", // 公告文字颜色
    closeIcon: 0, // 关闭图标 1 显示 0 隐藏
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "noticeA2",
    label: "公告",
    icon: '<i class="iconfont-admin icon-notice"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 11,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(noticeA2Defaults)
});
