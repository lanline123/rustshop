import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";

export type { UserInfoModule } from "./types";

const defaultSignInIcon = "https://oss.tigshop.com/img/gallery/202605/1779691601SIdmgIcby2Mii610eQ.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200";
const defaultSettingsIcon = "https://oss.tigshop.com/img/gallery/202605/1779679345kBAM5aU9K9EuPRQFfo.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200";
const defaultGradient = { colorA: "#fcf6fc", colorB: "#f3f3fe" };

/** 与旧 mobileMember/userInfo/View.vue 的 defaultModule 逐字段一致（契约） */
export const userInfoDefaults = {
    signIn: { show: true, icon: { picUrl: defaultSignInIcon, picThumb: defaultSignInIcon }, title: "签到有礼" },
    settings: { show: true, icon: { picUrl: defaultSettingsIcon, picThumb: defaultSettingsIcon }, title: "设置" },
    gradient: { ...defaultGradient },
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0, innerPadding: 16 }
};

registerModule({
    type: "userInfo",
    label: "用户信息",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileMember",
    platform: "mobile",
    order: 1,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(userInfoDefaults)
});
