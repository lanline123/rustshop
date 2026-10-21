import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { ImageSquareAdModule } from "./types";

/** 与旧 mobile/imageSquareAd/View.vue 的 defaultModule 逐字段一致（契约） */
export const imageSquareAdDefaults = {
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
    type: "imageSquareAd",
    label: "图片魔方",
    icon: '<i class="iconfont-admin icon-image_square_ad"></i>',
    url: "mobile",
    platform: "mobile",
    order: 3,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(imageSquareAdDefaults)
});
