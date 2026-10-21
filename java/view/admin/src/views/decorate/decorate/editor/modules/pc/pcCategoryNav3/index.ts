import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcCategoryNav3Module } from "./types";

/** 与旧 pc/pcCategoryNav3/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcCategoryNav3Defaults = {
    bannerStyle: 1,
    showCat: 1,
    logo: {
        picUrl: "",
        picThumb: ""
    },
    picList: [],
    picList2: [],
    picList3: []
};

registerModule({
    type: "pcCategoryNav3",
    label: "分类导航3",
    url: "pc",
    platform: "pc-home",
    order: 15,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCategoryNav3Defaults)
});
