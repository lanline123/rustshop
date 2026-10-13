import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcWhiteBlankModule } from "./types";

/**
 * 与旧 pcCustomPage/pcWhiteBlank/View.vue 行为一致（契约）：
 * 旧 View 是纯 props 组件、无 defaultModule 也不烧录默认值，
 * 拖入后 module 保持 {}，展示侧用 backgroundColor/blankHeight 的兜底渲染
 */
export const pcWhiteBlankDefaults = {};

registerModule({
    type: "pcWhiteBlank",
    label: "空白辅助",
    icon: '<i class="iconfont-admin icon-white_blank"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 6,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcWhiteBlankDefaults)
});
