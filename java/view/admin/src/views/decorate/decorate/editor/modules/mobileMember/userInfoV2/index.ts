import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { UserInfoV2Module } from "./types";

const defaultInnerPadding = 16;

/** 与旧 mobileMember/userInfoV2/View.vue 的 defaultModule 逐字段一致（契约） */
export const userInfoV2Defaults = {
    frame: {
        ...defaultFrame,
        backgroundColor: "#ffffff",
        boxRadius: 0,
        innerPadding: defaultInnerPadding,
        boxPadding: 0,
        boxPaddingTop: 0,
        boxPaddingBottom: 0
    }
};

registerModule({
    type: "userInfoV2",
    label: "用户信息2",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileMember",
    platform: "mobile",
    order: 2,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(userInfoV2Defaults)
});
