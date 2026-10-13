import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { WhiteBlankModule } from "./types";

/** 与旧 mobile/whiteBlank/View.vue 的 defaultModule 逐字段一致（契约） */
export const whiteBlankDefaults = {
    backgroundColor: "",
    blankHeight: 30
};

registerModule({
    type: "whiteBlank",
    label: "空白辅助",
    icon: '<i class="iconfont-admin icon-white_blank"></i>',
    url: "mobile",
    platform: "mobile",
    order: 8,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(whiteBlankDefaults)
});
