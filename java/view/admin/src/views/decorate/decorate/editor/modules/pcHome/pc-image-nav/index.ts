/**
 * PC 图文导航模块（新套可视化，对齐 Vort pc-image-nav）
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcImageNavModule } from "./types";

export type { PcImageNavModule } from "./types";

export const pcImageNavDefaults: PcImageNavModule = {
    navType: 1,
    navStyle: 1,
    rowNum: 4,
    colNum: 2,
    picList: [],
    backgroundColor: "#ffffff",
    imgPadding: 8,
    radioStyle: 1,
    picPageType: 1,
    swiperPageColor: "#333333",
    frame: {
        textColor: "",
        itemBackgroundColor: "",
        backgroundColor: "#ffffff",
        innerPadding: 0,
        itemHeight: 35,
        itemRadius: 3,
        boxRadius: 0,
        boxPadding: 10,
        boxPaddingTop: 5,
        boxPaddingBottom: 5
    }
};

registerModule({
    type: "pc-image-nav",
    label: "图文导航",
    icon: '<i class="iconfont-admin icon-image_nav"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 11,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcImageNavDefaults)
});
