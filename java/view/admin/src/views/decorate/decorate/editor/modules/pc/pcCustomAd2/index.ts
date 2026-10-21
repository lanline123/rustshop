import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcCustomAd2Module } from "./types";

/** 与旧 pc/pcCustomAd2/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcCustomAd2Defaults = {
    linkList: [],
    picList: [],
    picList2: [],
    picList3: []
};

registerModule({
    type: "pcCustomAd2",
    label: "自定义模块二",
    url: "pc",
    platform: "pc-home",
    order: 13,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCustomAd2Defaults)
});
