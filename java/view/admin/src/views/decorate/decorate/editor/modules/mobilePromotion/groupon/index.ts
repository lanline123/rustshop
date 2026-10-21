import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { GrouponModule } from "./types";

/** 与旧 mobilePromotion/groupon/View.vue 的 defaultModule 逐字段一致（契约） */
export const grouponDefaults = {
    style: 1,
    goodsStyle: 1,
    goodsRadioStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 2,
    goodsNamePadding: 1,
    showName: 1,
    showBrief: 1,
    showPrice: 1,
    goodsPadding: 5,
    buyBtnStyle: 1,
    backgroundColor: "",
    boxRadius: 0,
    innerPadding: 0,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    // 边距由顶层 boxPadding* 控制，frame 内边距置 0，避免与外层叠成双倍空隙
    frame: {
        ...defaultFrame,
        boxPadding: 0,
        boxPaddingTop: 0,
        boxPaddingBottom: 0
    },
    title: defaultTitle,
    groupType: 1,
    grouponActivityItem: undefined
};

registerModule({
    type: "groupon",
    label: "拼团",
    icon: '<i class="iconfont-tig icon-pintuan"></i>',
    url: "mobilePromotion",
    platform: "mobile",
    order: 4,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(grouponDefaults)
});
