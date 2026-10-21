import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { ShopInfoModule } from "./types";

/** 与旧 mobilePromotion/shopInfo/View.vue 的 defaultModule 逐字段一致（契约） */
export const shopInfoDefaults = {
    customPic: {
        picUrl: "",
        picThumb: ""
    },
    backgroundDefault: 1,
    style: 1,
    frame: defaultFrame
};

registerModule({
    type: "shopInfo",
    label: "店铺信息",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobilePromotion",
    platform: "mobile",
    order: 5,
    shopOnly: true,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(shopInfoDefaults)
});
