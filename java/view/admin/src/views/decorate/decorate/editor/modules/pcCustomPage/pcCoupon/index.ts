import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { PcCouponModule } from "./types";

/** 与旧 pcCustomPage/pcCoupon/View.vue 的 defaultModule 逐字段一致（契约，title/desc 为字符串字段而非 defaultTitle） */
export const pcCouponDefaults = {
    showTitle: 0,
    title: "每日领券",
    desc: "天天优惠领不停",
    colorStyle: 0,
    itemBackgroundColor: "#ffffff",
    backgroundColor: "#ffffff",
    itemPadding: 10,
    boxRadius: 8,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    frame: defaultFrame
};

registerModule({
    type: "pcCoupon",
    label: "优惠券",
    icon: '<i class="iconfont-admin icon-coupon"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 10,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcCouponDefaults)
});
