import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultIcoPic, defaultBackgroundPic } from "@/views/decorate/decorate/src/modules/index";

export type { CatNavModule } from "./types";

/** 与旧 mobile/catNav/View.vue 的 defaultModule 逐字段一致（契约） */
export const catNavDefaults = {
    navBackgroundPic: defaultBackgroundPic,
    logoPic: defaultIcoPic,
    itemWidth: 50,
    textColor: "",
    backgroundColor: "",
    itemBackgroundColor: "",
    searchTextColor: "",
    isGanged: 0,
    logoHeight: 0,
    searchText: "搜索商品",
    boxPadding: 10,
    itemRadius: 3,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    frame: defaultFrame
};

registerModule({
    type: "catNav",
    label: "分类导航",
    icon: '<i class="iconfont-admin icon-cat_nav"></i>',
    url: "mobile",
    platform: "mobile",
    order: 14,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(catNavDefaults)
});
