import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { LiveModule } from "./types";

/** 与旧 mobilePromotion/live/View.vue 的 defaultModule 逐字段一致（frame 为 defaultFrame 展开后覆盖，契约） */
export const liveDefaults = {
    titleText: "小程序直播",
    displayMode: "list",
    sourceType: "auto",
    autoLiveNum: 10,
    selectedLiveList: [],
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        innerPadding: 12,
        boxRadius: 8,
        boxPadding: 10,
        boxPaddingTop: 5,
        boxPaddingBottom: 5
    }
};

registerModule({
    type: "live",
    label: "小程序直播",
    icon: '<i class="iconfont-tig icon-xiaochengxuzhibo"></i>',
    url: "mobilePromotion",
    platform: "mobile",
    order: 1,
    toolbarGroup: "live",
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(liveDefaults)
});
