import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { TitleAdModule } from "./types";

/** 与旧 mobile/titleAd/View.vue 的 defaultModule 逐字段一致（契约） */
export const titleAdDefaults = {
    frame: defaultFrame,
    title: defaultTitle
};

registerModule({
    type: "titleAd",
    label: "文本标题",
    icon: '<i class="iconfont-admin icon-title_ad"></i>',
    url: "mobile",
    platform: "mobile",
    order: 10,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(titleAdDefaults)
});
