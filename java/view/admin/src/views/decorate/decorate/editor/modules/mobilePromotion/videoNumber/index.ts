import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { VideoNumberModule } from "./types";

/**
 * 与旧 mobilePromotion/videoNumber/View.vue 的 defaultModule 逐字段一致（契约）
 * liveTitle/titleColor 取旧 View 内局部常量 defaultLiveTitle("")/defaultTitleColor("#2A2A2A") 的字面量
 */
export const videoNumberDefaults = {
    titleText: "视频号",
    contentType: "video",
    videoNumberId: "",
    videoId: "",
    liveTitle: "",
    titleColorType: "default",
    titleColor: "#2A2A2A",
    productImages: [],
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
    type: "videoNumber",
    label: "视频号",
    icon: '<i class="iconfont-tig icon-shipinhaozhibo"></i>',
    url: "mobilePromotion",
    platform: "mobile",
    order: 2,
    toolbarGroup: "live",
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(videoNumberDefaults)
});
