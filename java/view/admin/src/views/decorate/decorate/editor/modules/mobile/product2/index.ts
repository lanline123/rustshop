import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultProducts } from "@/views/decorate/decorate/src/modules/index";
import { isOverseas } from "@/utils/version";

export type { Product2Module } from "./types";

/** 与旧 mobile/product2/View.vue 的 defaultModule 逐字段一致（契约） */
export const product2Defaults = {
    imageBackgroundColor: "",
    buyBtnStyle: 0,
    goodsRadio: 0,
    boxRadius: 0,
    innerPadding: 0,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    products: defaultProducts
};

