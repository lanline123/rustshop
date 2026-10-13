/**
 * PC 首页优惠券模块（新套可视化，对齐 Vort pc-home-coupon）
 * 说明：实现沉淀在 ../_shared/coupon，此处仅做模块注册
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { pcCouponDefaults } from "../_shared/coupon";

export type { PcCouponModule } from "../_shared/coupon";
export { pcCouponDefaults };

registerModule({
    type: "pc-home-coupon",
    label: "优惠券",
    icon: '<i class="iconfont-admin icon-coupon"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 82,
    preview: () => import("../_shared/coupon/PcCouponPreview.vue"),
    settings: () => import("../_shared/coupon/PcCouponSettings.vue"),
    getDefaultValue: () => cloneDeep(pcCouponDefaults)
});
