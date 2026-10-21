import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { PcSeckillModule } from "./types";

/** 与旧 pcCustomPage/pcSeckill/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcSeckillDefaults = {
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
    innerPadding: 10,
    boxPadding: 10,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,
    frame: defaultFrame,
    // 右侧区默认走倒计时，与老数据（顶部恒显示倒计时）语义一致
    title: { ...defaultTitle, showMore: 1, moreType: 2, countdownLabel: "距结束" }
};

registerModule({
    type: "pcSeckill",
    label: "秒杀",
    icon: '<i class="iconfont-admin icon-seckill"></i>',
    url: "pcCustomPage",
    platform: "pc-custom",
    order: 11,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcSeckillDefaults)
});
