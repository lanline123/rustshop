import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcRecommendProductModule } from "./types";

/** 与旧 pc/pcRecommendProduct/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcRecommendProductDefaults = {
    products: {
        productNumber: 50,
        productIds: []
    }
};

registerModule({
    type: "pcRecommendProduct",
    label: "推荐商品-瀑布流",
    url: "pc",
    platform: "pc-home",
    order: 11,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcRecommendProductDefaults)
});
