/**
 * PC 热图绘制模块（新套可视化，对齐 Vort pc-image-hotarea）
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcImageHotareaModule } from "./types";

export type { PcImageHotareaModule, HotareaPicItem, HotareaItem, HotareaLink } from "./types";

export const pcImageHotareaDefaults: PcImageHotareaModule = {
    picList: [],
    imgPadding: 0,
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
    },
    title: {
        showTitle: 0,
        titleStyle: 1,
        titleAlign: 1,
        titleBackground: "",
        titleBackground2: "",
        titleBackgroundPic: { picUrl: "", picThumb: "" },
        titleRadius: 0,
        titleText: "标题内容",
        titleColor: "",
        descText: "",
        descColor: "#aaaaaa",
        showMore: 0,
        moreLink: { link: "", title: "" },
        moreColor: "#aaaaaa"
    }
};

registerModule({
    type: "pc-image-hotarea",
    label: "热图绘制",
    icon: '<i class="iconfont-admin icon-image_hotarea"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 13,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcImageHotareaDefaults)
});
