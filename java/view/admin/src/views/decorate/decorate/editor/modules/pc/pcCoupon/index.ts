import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcCouponModule } from "./types";

/** 与旧 pc/pcCoupon/View.vue 的 defaultModule 逐字段一致（契约：空对象） */
export const pcCouponDefaults = {};

registerModule({
    type: "pcCoupon",
    label: "优惠券模块",
    url: "pc",
    platform: "pc-home",
    order: 5,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCouponDefaults)
});
