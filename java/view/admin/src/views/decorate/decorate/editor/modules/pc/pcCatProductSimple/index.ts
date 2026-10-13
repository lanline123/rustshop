import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcCatProductSimpleModule } from "./types";

/** 与旧 pc/pcCatProductSimple/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcCatProductSimpleDefaults = {
    baseStyle: 1,
    title: "",
    products: {
        productSelectType: 1,
        productCategoryId: 1,
        productNumber: 8,
        productIds: [],
        productTag: ""
    },
    moreLink: "",
    picList: []
};

registerModule({
    type: "pcCatProductSimple",
    label: "分类商品-简约",
    url: "pc",
    platform: "pc-home",
    order: 9,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCatProductSimpleDefaults)
});
