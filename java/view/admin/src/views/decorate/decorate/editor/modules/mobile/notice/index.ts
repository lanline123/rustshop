import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultIcoPic } from "@/views/decorate/decorate/src/modules/index";

export type { NoticeModule } from "./types";

/** 与旧 mobile/notice/View.vue 的 defaultModule 逐字段一致（契约） */
export const noticeDefaults = {
    text: "公告内容",
    icoPic: defaultIcoPic,
    nociceEffect: "scroll", // 公告滚动效果 滚动 scroll  静止 still
    frame: defaultFrame
};

registerModule({
    type: "notice",
    label: "公告",
    icon: '<i class="iconfont-admin icon-notice"></i>',
    url: "mobile",
    platform: "mobile",
    order: 11,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => {
        // 旧 View.vue 渲染前先全局改写 defaultFrame.backgroundColor，再烧录；
        // 新链路取默认值时机即旧渲染时机，需复刻该副作用以保证 frame 字段一致（契约）
        defaultFrame.backgroundColor = "transparent";
        return cloneDeep(noticeDefaults);
    }
});
