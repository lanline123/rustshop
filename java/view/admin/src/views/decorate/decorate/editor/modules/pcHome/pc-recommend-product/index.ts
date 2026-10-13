/**
 * PC 推荐商品模块（新套可视化）
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcRecommendProductModule } from "./types";

export type { PcRecommendProductModule, ProductSelectConfig } from "./types";

const defaultProducts = {
    productSelectType: 1,
    productIds: [],
    productCategoryId: undefined,
    productTag: "",
    productNumber: 20
};

export const pcRecommendProductDefaults: PcRecommendProductModule = {
    frame: {
        boxPadding: 10,
        boxPaddingTop: 10,
        boxPaddingBottom: 50,
        boxRadius: 0,
        backgroundColor: "transparent",
        itemGap: 16,
        columns: 5
    },
    titleName: "猜你喜欢",
    products: defaultProducts
};

// 不进 PC 首页工具栏（visible: false），注册保留以保证已有数据可渲染与设置
registerModule({
    type: "pc-recommend-product",
    label: "推荐商品",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 3,
    visible: false,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcRecommendProductDefaults)
});
