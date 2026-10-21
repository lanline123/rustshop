import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
// 默认值与旧 View.vue 的 defaultModule 单一来源（defaultFrame 取旧集中常量，契约冻结）
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { WhiteLineModule } from "./types";

/** 与旧 mobile/whiteLine/View.vue 的 defaultModule 逐字段一致（契约） */
export const whiteLineDefaults = {
    lineType: 1,
    lineStyle: 1,
    frame: defaultFrame
};

registerModule({
    type: "whiteLine",
    label: "分割线",
    icon: '<i class="iconfont-admin icon-white_line"></i>',
    url: "mobile",
    platform: "mobile",
    order: 7,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(whiteLineDefaults)
});
