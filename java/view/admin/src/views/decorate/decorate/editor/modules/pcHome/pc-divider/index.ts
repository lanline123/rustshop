/**
 * PC 分割线模块（新套可视化，对齐 Vort pc-divider）
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcDividerModule } from "./types";

export type { PcDividerModule } from "./types";

export const pcDividerDefaults: PcDividerModule = {
    lineType: 2,
    lineStyle: 1,
    frame: {
        textColor: "",
        itemBackgroundColor: "",
        backgroundColor: "#ffffff",
        innerPadding: 0,
        itemHeight: 10,
        itemRadius: 0,
        boxRadius: 0,
        boxPadding: 10,
        boxPaddingTop: 5,
        boxPaddingBottom: 5
    }
};

registerModule({
    type: "pc-divider",
    label: "分割线",
    icon: '<i class="iconfont-admin icon-white_line"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 60,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcDividerDefaults)
});
