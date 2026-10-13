import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { PcTitleAdModule } from "./types";

/** 与旧 pcCustomPage/pcTitleAd/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcTitleAdDefaults = {
    frame: defaultFrame,
    title: defaultTitle
};

registerModule({
    type: "pcTitleAd",
    label: "文本标题",
    icon: '<i class="iconfont-admin icon-title_ad"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 8,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcTitleAdDefaults)
});
