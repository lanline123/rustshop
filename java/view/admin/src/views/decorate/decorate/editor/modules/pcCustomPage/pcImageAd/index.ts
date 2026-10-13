import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { PcImageAdModule } from "./types";

/** 与旧 pcCustomPage/pcImageAd/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcImageAdDefaults = {
    picType: 1,
    picList: [],
    swiperPreView: 1,
    swiperPageColor: "",
    imgPadding: 0,
    picPageType: 1,
    picRadioStyle: 1,
    isFluxWidth: 0,
    frame: defaultFrame,
    title: defaultTitle
};

registerModule({
    type: "pcImageAd",
    label: "图片广告1",
    icon: '<i class="iconfont-admin icon-image_nav"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 1,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcImageAdDefaults)
});
