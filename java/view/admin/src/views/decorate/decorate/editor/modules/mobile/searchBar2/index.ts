import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultIcoPic } from "@/views/decorate/decorate/src/modules/index";
import { isOverseas } from "@/utils/version";

export type { SearchBar2Module } from "./types";

/** 与旧 mobile/searchBar2/View.vue 的 defaultModule 逐字段一致（契约） */
export const searchBar2Defaults = {
    text: "搜索商品",
    icoPic: defaultIcoPic,
    frame: defaultFrame
};

