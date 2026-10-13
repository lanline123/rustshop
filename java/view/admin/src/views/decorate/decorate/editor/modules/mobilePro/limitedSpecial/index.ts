import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultLimitedSpecial } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { LimitedSpecialModule } from "./types";

/** 旧 View.vue 直接用集中常量 defaultLimitedSpecial 作为 defaultModule（契约，单一来源） */
export const limitedSpecialDefaults = defaultLimitedSpecial;

registerModule({
    type: "limitedSpecial",
    label: "限时特惠",
    icon: '<i class="iconfont-admin icon-seckill"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 27,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(limitedSpecialDefaults)
});
