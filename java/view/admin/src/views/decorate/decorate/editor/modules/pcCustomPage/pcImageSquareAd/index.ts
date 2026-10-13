import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { PcImageSquareAdModule } from "./types";

/** 与旧 pcCustomPage/pcImageSquareAd/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcImageSquareAdDefaults = {
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
    type: "pcImageSquareAd",
    label: "图片魔方",
    icon: '<i class="iconfont-admin icon-image_square_ad"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 2,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcImageSquareAdDefaults)
});
