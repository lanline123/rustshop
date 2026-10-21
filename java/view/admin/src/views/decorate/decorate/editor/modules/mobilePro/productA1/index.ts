import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultProductA1 } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { ProductA1Module } from "./types";

/** 旧 mobilePro/productA1/View.vue 直接以共享常量 defaultProductA1 作为 defaultModule（契约，单一来源） */
export const productA1Defaults = defaultProductA1;

registerModule({
    type: "productA1",
    label: "商品A1",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 8,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(productA1Defaults)
});
