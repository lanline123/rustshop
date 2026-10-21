import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcCatProductNormalModule } from "./types";

/** 与旧 pc/pcCatProductNormal/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcCatProductNormalDefaults = {
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
    picList2: []
};

registerModule({
    type: "pcCatProductNormal",
    label: "分类商品-窄",
    url: "pc",
    platform: "pc-home",
    order: 6,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCatProductNormalDefaults)
});
