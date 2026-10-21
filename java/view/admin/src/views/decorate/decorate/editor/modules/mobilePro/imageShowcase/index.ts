import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultImageShowcase } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { ImageShowcaseModule } from "./types";

/** 旧 View.vue 直接用集中常量 defaultImageShowcase 作为 defaultModule（契约，单一来源） */
export const imageShowcaseDefaults = defaultImageShowcase;

registerModule({
    type: "imageShowcase",
    label: "图片橱窗",
    icon: '<i class="iconfont-admin icon-image_nav"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 2,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(imageShowcaseDefaults)
});
