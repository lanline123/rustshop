import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { PcGrouponModule } from "./types";

/** 与旧 pc/pcGroupon/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcGrouponDefaults = {
    style: 2,
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
    frame: defaultFrame,
    title: defaultTitle,
    groupType: 1,
    grouponActivityItem: undefined
};

// 与 pcCustomPage/pcGroupon 同 type 不同实现，注册键 type@url 按 url 区分
registerModule({
    type: "pcGroupon",
    label: "拼团",
    url: "pc",
    platform: "pc-home",
    order: 16,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcGrouponDefaults)
});
