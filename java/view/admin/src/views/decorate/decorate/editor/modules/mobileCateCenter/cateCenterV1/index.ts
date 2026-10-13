import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { CateCenterV1Module } from "./types";

/** 与旧 mobileCateCenter/cateCenterV1/View.vue 的 defaultModule 逐字段一致（契约） */
export const cateCenterV1Defaults = {
    searchPlaceholder: "点击搜索商品信息",
    showCategoryName: true,
    showRecommend: true,
    recommendText: "推荐",
    // 颜色一律留空：空串表示跟随主题色 / 原默认色，保证老数据视觉零变化
    sideBgColor: "",
    sideTextColor: "",
    sideActiveTextColor: "",
    sideActiveBgColor: "",
    groupTitleColor: "",
    categoryNameColor: "",
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0 }
};

registerModule({
    type: "cateCenterV1",
    label: "分类中心V1",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileCateCenter",
    platform: "mobile",
    order: 1,
    exclusive: true,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(cateCenterV1Defaults)
});
