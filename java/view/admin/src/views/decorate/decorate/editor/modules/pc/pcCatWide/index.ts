import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcCatWideModule } from "./types";

/** 与旧 pc/pcCatWide/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcCatWideDefaults = {
    catIds: []
};

registerModule({
    type: "pcCatWide",
    label: "分类模块-宽",
    url: "pc",
    platform: "pc-home",
    order: 8,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCatWideDefaults)
});
