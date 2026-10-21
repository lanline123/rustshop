/**
 * PC 文本标题模块（新套可视化，对齐 Vort pc-title-ad）
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcTitleAdModule } from "./types";

export type { PcTitleAdModule } from "./types";

export const pcTitleAdDefaults: PcTitleAdModule = {
    title: {
        showTitle: 1,
        titleStyle: 1,
        titleAlign: 1,
        titleBackground: "",
        titleBackground2: "",
        titleBackgroundPic: { picUrl: "", picThumb: "" },
        titleRadius: 0,
        titleText: "标题内容",
        titleColor: "#2a3145",
        descText: "",
        descColor: "#aaaaaa",
        showMore: 0,
        moreLink: { link: "", title: "" },
        moreColor: "#969799"
    },
    frame: {
        textColor: "",
        itemBackgroundColor: "",
        backgroundColor: "#ffffff",
        innerPadding: 0,
        itemHeight: 35,
        itemRadius: 3,
        boxRadius: 0,
        boxPadding: 10,
        leftAndRightPadding: 0,
        boxPaddingTop: 5,
        boxPaddingBottom: 5
    }
};

registerModule({
    type: "pc-title-ad",
    label: "文本标题",
    icon: '<i class="iconfont-admin icon-title_ad"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 75,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcTitleAdDefaults)
});
