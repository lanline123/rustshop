import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { PcNoticeModule } from "./types";

/** 与旧 pcCustomPage/pcNotice/View.vue 的 defaultModule 逐字段一致（契约，icoPic 为旧文件内联字面量而非 defaultIcoPic） */
export const pcNoticeDefaults = {
    text: "公告内容",
    icoPic: {
        picUrl: "https://oss.tigshop.com/img/gallery/202508/1754895669bP3iJSUiXSBoRB1VQq.png"
    },
    frame: defaultFrame
};

registerModule({
    type: "pcNotice",
    label: "公告",
    icon: '<i class="iconfont-admin icon-notice"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 9,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcNoticeDefaults)
});
