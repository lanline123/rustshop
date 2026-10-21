import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { isOverseas } from "@/utils/version";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { ImageAd2Module } from "./types";

/** 与旧 mobile/imageAd2/View.vue 的 defaultModule 逐字段一致（契约） */
export const imageAd2Defaults = {
    picType: 1,
    picList: [],
    swiperPreView: 1,
    swiperPageColor: "",
    imgPadding: 0,
    picPageType: 1,
    picRadioStyle: 1,
    showIndicator: 1,
    isFluxWidth: 0,
    frame: defaultFrame,
    title: defaultTitle
};

