import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultTitle } from "@/views/decorate/decorate/src/modules/index";
import { isOverseas } from "@/utils/version";

export type { Seckill2Module } from "./types";

/** 与旧 mobilePromotion/seckill2/View.vue 的 defaultModule 逐字段一致（契约，无 frame 字段） */
export const seckill2Defaults = {
    style: 1,
    goodsNamePadding: 1,
    showName: 0,
    showPrice: 1,
    goodsPadding: 5,
    imageBackgroundColor: "",
    backgroundColor: "",
    boxRadius: 0,
    innerPadding: 0,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    title: defaultTitle
};

