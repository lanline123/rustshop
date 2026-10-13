/**
 * PC 公告模块（新套可视化，对齐 Vort pc-notice）
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcNoticeModule } from "./types";

export type { PcNoticeModule } from "./types";

const DEFAULT_ICON_URL = "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202508/1754895669bP3iJSUiXSBoRB1VQq.png";

export const pcNoticeDefaults: PcNoticeModule = {
    text: "公告内容",
    icoPic: {
        picUrl: DEFAULT_ICON_URL,
        picThumb: DEFAULT_ICON_URL
    },
    frame: {
        textColor: "#555555",
        itemBackgroundColor: "#fff8e6",
        backgroundColor: "#ffffff",
        innerPadding: 0,
        itemHeight: 35,
        itemRadius: 4,
        boxRadius: 0,
        boxPadding: 10,
        boxPaddingTop: 5,
        boxPaddingBottom: 5
    }
};

registerModule({
    type: "pc-notice",
    label: "公告",
    icon: '<i class="iconfont-admin icon-notice"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 72,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcNoticeDefaults)
});
