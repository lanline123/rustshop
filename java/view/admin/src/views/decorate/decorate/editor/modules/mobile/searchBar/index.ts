import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultIcoPic } from "@/views/decorate/decorate/src/modules/index";

export type { SearchBarModule } from "./types";

/** 与旧 mobile/searchBar/View.vue 的 defaultModule 逐字段一致（契约） */
export const searchBarDefaults = {
    text: "搜索商品",
    icoPic: defaultIcoPic,
    frame: defaultFrame
};

registerModule({
    type: "searchBar",
    label: "商品搜索",
    icon: '<i class="iconfont-admin icon-search_bar"></i>',
    url: "mobile",
    platform: "mobile",
    order: 12,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(searchBarDefaults)
});
