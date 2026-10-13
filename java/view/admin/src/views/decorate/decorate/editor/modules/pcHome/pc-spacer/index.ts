/**
 * PC 空白辅助模块（新套可视化，对齐 Vort pc-spacer）
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcSpacerModule } from "./types";

export type { PcSpacerModule } from "./types";

export const pcSpacerDefaults: PcSpacerModule = {
    frame: {
        boxPadding: 0,
        boxPaddingTop: 0,
        boxPaddingBottom: 0,
        boxRadius: 0
    },
    backgroundColor: "#ffffff",
    blankHeight: 30
};

registerModule({
    type: "pc-spacer",
    label: "空白辅助",
    icon: '<i class="iconfont-admin icon-white_blank"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 70,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcSpacerDefaults)
});
