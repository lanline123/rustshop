import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { MyServiceModule } from "./types";

const defaultInnerPadding = 15;
const defaultIconRadius = 50;

/** 与旧 mobileMember/myService/View.vue 的 defaultModule 逐字段一致（契约） */
export const myServiceDefaults = {
    moduleTitle: "服务管理",
    layoutStyle: 1,
    iconRadius: defaultIconRadius,
    serviceList: [],
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 10,
        innerPadding: defaultInnerPadding,
        boxPadding: 10,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};

registerModule({
    type: "myService",
    label: "我的服务",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileMember",
    platform: "mobile",
    order: 9,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(myServiceDefaults)
});
