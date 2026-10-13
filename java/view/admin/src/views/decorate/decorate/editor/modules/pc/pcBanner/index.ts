import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcBannerModule } from "./types";

/** 与旧 pc/pcBanner/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcBannerDefaults = {
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
    type: "pcBanner",
    label: "轮播Banner",
    url: "pc",
    platform: "pc-home",
    order: 1,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcBannerDefaults)
});
