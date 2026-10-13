import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { isOverseas } from "@/utils/version";

export type { PcBanner2Module } from "./types";

/** 与旧 pc/pcBanner2/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcBanner2Defaults = {
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

