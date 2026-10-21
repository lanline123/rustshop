import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { ImageHotareaModule } from "./types";

/** 与旧 mobile/imageHotarea/View.vue 的 defaultModule 逐字段一致（契约） */
export const imageHotareaDefaults = {
    picList: [],
    imgPadding: 0,
    picRadioStyle: 1,
    frame: defaultFrame,
    title: defaultTitle
};

registerModule({
    type: "imageHotarea",
    label: "热图绘制",
    icon: '<i class="iconfont-admin icon-image_hotarea"></i>',
    url: "mobile",
    platform: "mobile",
    order: 4,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(imageHotareaDefaults)
});
