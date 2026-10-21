import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcCustomAd1Module } from "./types";

/** 与旧 pc/pcCustomAd1/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcCustomAd1Defaults = {
    picList: [],
    picList2: [],
    picList3: []
};

registerModule({
    type: "pcCustomAd1",
    label: "自定义模块一",
    url: "pc",
    platform: "pc-home",
    order: 12,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCustomAd1Defaults)
});
