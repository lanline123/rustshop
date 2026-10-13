import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcCatProductWideModule } from "./types";

/** 与旧 pc/pcCatProductWide/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcCatProductWideDefaults = {
    categoryId: 0,
    shortTitle: "",
    enTitle: "",
    color: "",
    color2: "",
    logo: {
        picUrl: "",
        picThumb: ""
    },
    picList: [],
    picList2: [],
    productIds: [],
    linkList: []
};

registerModule({
    type: "pcCatProductWide",
    label: "分类商品-宽",
    url: "pc",
    platform: "pc-home",
    order: 7,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCatProductWideDefaults)
});
