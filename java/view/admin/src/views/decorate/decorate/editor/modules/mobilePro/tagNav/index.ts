import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultTagNav } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { TagNavModule } from "./types";

/** 旧 mobilePro/tagNav/View.vue 直接以共享常量 defaultTagNav 作为 defaultModule（契约，单一来源） */
export const tagNavDefaults = defaultTagNav;

registerModule({
    type: "tagNav",
    label: "标签导航",
    icon: '<i class="iconfont-admin icon-category_nav"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 22,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(tagNavDefaults)
});
