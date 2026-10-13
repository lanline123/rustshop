import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { PcGrouponModule } from "./types";

/** 与旧 pcCustomPage/pcGroupon/View.vue 的 defaultModule 逐字段一致（契约，backgroundColor 同源取自 defaultFrame.backgroundColor） */
export const pcGrouponDefaults = {
    style: 2,
    groupType: 1,
    goodsStyle: 1,
    goodsRadioStyle: 1,
    textAlign: 1,
    textWeight: 1,
    goodsNameRow: 1,
    goodsNamePadding: 0,
    showName: 1,
    showPrice: 1,
    cardStyle: 0,
    goodsPadding: 0,
    innerPadding: 0,
    boxPadding: 0,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    backgroundColor: defaultFrame.backgroundColor,
    boxRadius: 0,
    frame: defaultFrame,
    // 右侧区默认走更多链接，与老数据（showMore=1 即"查看更多"）语义一致
    title: { ...defaultTitle, moreType: 1, countdownLabel: "距结束" },
    grouponActivityItem: undefined
};

registerModule({
    type: "pcGroupon",
    label: "拼团",
    icon: '<i class="iconfont-admin icon-seckill"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 12,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcGrouponDefaults)
});
