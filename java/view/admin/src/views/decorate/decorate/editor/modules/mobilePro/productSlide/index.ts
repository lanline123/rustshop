import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultProductSlide } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { ProductSlideModule } from "./types";

/** 旧 mobilePro/productSlide/View.vue 直接以共享常量 defaultProductSlide 作为 defaultModule（契约，单一来源） */
export const productSlideDefaults = defaultProductSlide;

registerModule({
    type: "productSlide",
    label: "商品滑动",
    icon: '<i class="iconfont-admin icon-group_sliding"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 28,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(productSlideDefaults)
});
