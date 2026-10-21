import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcCategoryNavModule } from "./types";

/** 与旧 pc/pcCategoryNav/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcCategoryNavDefaults = {
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
    type: "pcCategoryNav",
    label: "分类导航",
    url: "pc",
    platform: "pc-home",
    order: 14,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCategoryNavDefaults)
});
