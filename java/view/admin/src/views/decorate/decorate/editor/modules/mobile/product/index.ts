import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle, defaultProducts } from "@/views/decorate/decorate/src/modules/index";

export type { ProductModule } from "./types";

/** 与旧 mobile/product/View.vue 的 defaultModule 逐字段一致（契约） */
export const productDefaults = {
    style: 1,
    goodsStyle: 1,
    goodsRadioStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 2,
    goodsNamePadding: 1,
    showName: 1,
    showBrief: 1,
    showPrice: 1,
    goodsPadding: 5,
    buyBtnStyle: 1,
    backgroundColor: "",
    boxRadius: 0,
    innerPadding: 0,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    imgPadding: 1,
    waterfall: 0,
    swiperPageColor: "",
    frame: defaultFrame,
    title: defaultTitle,
    products: defaultProducts
};

registerModule({
    type: "product",
    label: "商品",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobile",
    platform: "mobile",
    order: 5,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(productDefaults)
});
