import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { FixedFunctionalAreaModule } from "./types";

/** 与旧 mobileMember/fixedFunctionalArea/View.vue 的 defaultModule 逐字段一致（契约） */
export const fixedFunctionalAreaDefaults = {
    frame: {
        ...defaultFrame,
        backgroundColor: "",
        boxRadius: 0,
        innerPadding: 0,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};

// 固定模块：由 ensureFixedModules 注入不进工具栏，但渲染器需要注册才能加载
registerModule({
    type: "fixedFunctionalArea",
    label: "固定功能区",
    url: "mobileMember",
    platform: "mobile",
    visible: false,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(fixedFunctionalAreaDefaults)
});
