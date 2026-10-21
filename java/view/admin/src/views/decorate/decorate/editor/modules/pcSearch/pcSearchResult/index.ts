/**
 * PC 搜索结果模块
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcSearchResultModule } from "./types";
import { pcSearchResultDefaults } from "./types";

export type { PcSearchResultModule } from "./types";

/** 对齐 vortmall pc-search-result：icon = decorate/search_result */
const searchResultIcon = `
<svg xmlns="http://www.w3.org/2000/svg" width="26" height="26" viewBox="0 0 1024 1024" fill="currentColor">
    <path d="M416 192c123.5 0 224 100.5 224 224 0 50.4-16.9 96.8-45.1 134.2l175.5 175.5c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L549.6 595.5C512.2 623.7 465.8 640.6 416 640.6c-123.5 0-224-100.5-224-224S292.5 192 416 192z m0 64c-88.4 0-160 71.6-160 160s71.6 160 160 160 160-71.6 160-160-71.6-160-160-160z m320 384h128c17.7 0 32 14.3 32 32s-14.3 32-32 32H736c-17.7 0-32-14.3-32-32s14.3-32 32-32z m-64 96h192c17.7 0 32 14.3 32 32s-14.3 32-32 32H672c-17.7 0-32-14.3-32-32s14.3-32 32-32z m-64 96h256c17.7 0 32 14.3 32 32s-14.3 32-32 32H608c-17.7 0-32-14.3-32-32s14.3-32 32-32z"/>
</svg>
`;

registerModule({
    type: "pcSearchResult",
    label: "搜索结果",
    icon: searchResultIcon,
    url: "pcSearch",
    platform: "pc-search",
    toolbarGroup: "searchResult",
    order: 80,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcSearchResultDefaults) as PcSearchResultModule
});
