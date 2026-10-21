import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame } from "@/views/decorate/decorate/src/modules/index";
import { isPro, isStore } from "@/utils/version";

export type { StoreOrderCardModule, StoreOrderCardItem } from "./types";

/** 本模块 frame 默认覆盖：圆角 8px、左右/上下内边距 10px、图片边距 0 */
export const moduleFrameDefault = {
    boxRadius: 8,
    innerPadding: 10,
    innerPaddingLeftRight: 10,
    innerPaddingTopBottom: 10,
    imagePaddingTop: 0,
    imagePaddingBottom: 0,
    imagePaddingLeftRight: 0
};

/** 生成默认卡片项 */
export const buildDefaultCard = () => ({
    picId: 0,
    picThumb: "",
    picUrl: "",
    picName: "",
    title: "",
    desc: "",
    link: {}
});

/** 各模板对应卡片张数 */
export const LENGTH_MAP: Record<number, number> = {
    1: 1,
    2: 2,
    3: 3,
    4: 3,
    5: 4,
    6: 5
};

/** 与旧 mobilePro/storeOrderCard/View.vue 的 defaultModule 逐字段一致（契约） */
export const storeOrderCardDefaults = {
    picType: 2,
    cardStyle: 1,
    boxStyle: 1,
    backgroundColor: "",
    cardBackground: "#ffffff",
    cardGap: 8,
    frame: { ...defaultFrame, ...moduleFrameDefault },
    cardList: [buildDefaultCard(), buildDefaultCard()]
};

registerModule({
    type: "storeOrderCard",
    label: "点单卡片",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 30,
    enabled: () => isPro() && isStore(),
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(storeOrderCardDefaults)
});
