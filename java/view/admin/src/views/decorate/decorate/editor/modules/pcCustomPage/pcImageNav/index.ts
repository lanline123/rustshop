import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { PcImageNavModule } from "./types";

/** 与旧 pcCustomPage/pcImageNav/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcImageNavDefaults = {
    navType: 1,
    navStyle: 1,
    rowNum: 4,
    colNum: 2,
    picList: [],
    imgPadding: 8,
    radioStyle: 1,
    picPageType: 1,
    swiperPageColor: "",
    frame: defaultFrame
};

registerModule({
    type: "pcImageNav",
    label: "图文导航",
    icon: '<i class="iconfont-admin icon-image_nav"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 7,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcImageNavDefaults)
});
