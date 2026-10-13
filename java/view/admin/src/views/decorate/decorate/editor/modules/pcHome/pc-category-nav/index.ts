/**
 * PC 分类导航模块（新套可视化，对齐 Vort pc-category-nav）
 * 热门分类取数通过分类接口筛选 isHot
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcCategoryNavModule } from "./types";

export type { PcCategoryNavModule } from "./types";

export const pcCategoryNavDefaults: PcCategoryNavModule = {
    frame: {
        boxPadding: 0,
        boxPaddingTop: 0,
        boxPaddingBottom: 0,
        boxRadius: 8,
        backgroundColor: "#f7f7f7",
        boxHeight: 328
    },
    titleName: "热门分类",
    showTitle: 1,
    displayNum: 6,
    backgroundColor: "#f7f7f7"
};

registerModule({
    type: "pc-category-nav",
    label: "分类导航",
    icon: '<i class="iconfont-admin icon-category_nav"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 4,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCategoryNavDefaults)
});
