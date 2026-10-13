import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { PcImageHotareaModule } from "./types";

/** 与旧 pcCustomPage/pcImageHotarea/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcImageHotareaDefaults = {
    picList: [],
    imgPadding: 0,
    frame: defaultFrame,
    title: defaultTitle
};

registerModule({
    type: "pcImageHotarea",
    label: "热图绘制",
    icon: '<i class="iconfont-admin icon-image_hotarea"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 3,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcImageHotareaDefaults)
});
