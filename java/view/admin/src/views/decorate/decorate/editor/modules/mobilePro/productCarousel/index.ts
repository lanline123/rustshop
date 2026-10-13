import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultProductCarousel } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { ProductCarouselModule } from "./types";

/** 旧 mobilePro/productCarousel/View.vue 直接以共享常量 defaultProductCarousel 作为 defaultModule（契约，单一来源） */
export const productCarouselDefaults = defaultProductCarousel;

registerModule({
    type: "productCarousel",
    label: "商品轮播",
    icon: '<i class="iconfont-admin icon-group_sliding"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 29,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(productCarouselDefaults)
});
