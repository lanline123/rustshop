import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import type { UserInfoStatsItem } from "./types";

export type { UserInfo3Module } from "./types";

const defaultSignInIcon = "https://oss.tigshop.com/img/gallery/202605/1779691601SIdmgIcby2Mii610eQ.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200";
const defaultSettingsIcon = "https://oss.tigshop.com/official/img/pc/202605/1779872068yYRoxuCDxWgGcOti6b.png";
const defaultNotificationIcon = "https://oss.tigshop.com/official/img/pc/202605/1779958812O0gl221tWQVIEt7OXD.png";
const DEFAULT_GROWTH_CARD_BG = "https://oss.tigshop.com/official/img/pc/202606/17808830195Hk80LmOeUpDCCCEKW.png";
const defaultStyle1BgImage = "https://oss.tigshop.com/official/img/pc/202605/1779860190FsB8OM4MrGwgOCvKsA.png";
const style2GradientDefault = { colorA: "#E0FCED", colorB: "#FDF1F3" };

const defaultStatsItems: UserInfoStatsItem[] = [
    { key: "collect", label: "商品收藏", show: true },
    { key: "follow", label: "门店关注", show: true },
    { key: "history", label: "浏览记录", show: true }
];

/** 与旧 mobileMember/userInfo3/View.vue 的 mergeDefaultModule 字面量逐字段一致（契约） */
export const userInfo3Defaults = {
    layoutStyle: 1,
    showEditIcon: true,
    signIn: { show: true, icon: { picUrl: defaultSignInIcon, picThumb: defaultSignInIcon }, title: "签到有礼" },
    settings: { show: true, icon: { picUrl: defaultSettingsIcon, picThumb: defaultSettingsIcon } },
    gradient: { ...style2GradientDefault },
    bgColor: "#57D45C",
    backgroundImage: { picUrl: defaultStyle1BgImage, picThumb: defaultStyle1BgImage },
    bottomArc: { show: true, height: 24 },
    memberBar: {
        show: true,
        backgroundColor: "",
        borderRadius: 10,
        levelName: "付费会员",
        description: "购买会员，畅享更多专属权益",
        buttonText: "查看",
        buttonLink: { path: "base", label: "权益卡中心", name: "权益卡中心", link: "member/premiumMember/index" },
        waveImage: { picUrl: DEFAULT_GROWTH_CARD_BG, picThumb: DEFAULT_GROWTH_CARD_BG }
    },
    notification: { show: true, icon: { picUrl: defaultNotificationIcon, picThumb: defaultNotificationIcon } },
    stats: { show: true, items: defaultStatsItems.map((i) => ({ ...i })) },
    frame: { ...defaultFrame, backgroundColor: "", boxPaddingTop: 0, boxPaddingBottom: 0, boxPadding: 0, innerPadding: 16 }
};

registerModule({
    type: "userInfo3",
    label: "用户信息3",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobileMember",
    platform: "mobile",
    order: 3,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(userInfo3Defaults)
});
