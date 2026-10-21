import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultNewcomerCombo } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { NewcomerComboModule } from "./types";

/** 旧 mobilePro/newcomerCombo/View.vue 直接以共享常量 defaultNewcomerCombo 作为 defaultModule（契约，单一来源） */
export const newcomerComboDefaults = defaultNewcomerCombo;

registerModule({
    type: "newcomerCombo",
    label: "新人二重礼",
    icon: '<i class="iconfont-admin icon-coupon"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 26,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(newcomerComboDefaults)
});
