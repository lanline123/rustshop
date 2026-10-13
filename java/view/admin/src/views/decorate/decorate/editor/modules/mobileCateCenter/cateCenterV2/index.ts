import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { CateCenterV2Module } from "./types";

/** 与旧 mobileCateCenter/cateCenterV2/View.vue 的 defaultModule 逐字段一致（契约） */
export const cateCenterV2Defaults = {
    styleType: 2,
    subTabStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 2,
    showName: 1,
    buyBtnStyle: 1,
    // 颜色一律留空：空串表示跟随主题色 / 原默认色，保证老数据视觉零变化
    themeColor: "",
    sideBgColor: "",
    sideTextColor: "",
    sideActiveTextColor: "",
    sideActiveBgColor: "",
    sideIndicatorColor: "",
    subTabTextColor: "",
    subTabBgColor: "",
    subTabActiveTextColor: "",
    subTabActiveBgColor: "",
    goodsListBgColor: "",
    productTitleColor: "",
    priceColor: "",
    buyBtnColor: "",
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0 }
};

registerModule({
    type: "cateCenterV2",
    label: "分类中心V2",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileCateCenter",
    platform: "mobile",
    order: 2,
    exclusive: true,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(cateCenterV2Defaults)
});
