import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { PcWhiteLineModule } from "./types";

/** 与旧 pcCustomPage/pcWhiteLine/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcWhiteLineDefaults = {
    lineType: 2,
    lineStyle: 1,
    frame: defaultFrame
};

registerModule({
    type: "pcWhiteLine",
    label: "分割线",
    icon: '<i class="iconfont-admin icon-white_line"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 5,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcWhiteLineDefaults)
});
