import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcImageAdModule } from "./types";

/** 与旧 pc/pcImageAd/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcImageAdDefaults = {
    picList: [],
    boxPaddingTop: 10,
    boxPaddingBottom: 10
};

// 与 pcCustomPage/pcImageAd 同 type 不同实现，注册键 type@url 按 url 区分
registerModule({
    type: "pcImageAd",
    label: "通宽广告图",
    url: "pc",
    platform: "pc-home",
    order: 10,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcImageAdDefaults)
});
