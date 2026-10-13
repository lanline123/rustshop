import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/index";

export type { SeckillModule } from "./types";

/** 与旧 mobilePromotion/seckill/View.vue 的 defaultModule 逐字段一致（契约） */
export const seckillDefaults = {
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
    frame: defaultFrame,
    title: defaultTitle
};

registerModule({
    type: "seckill",
    label: "秒杀",
    icon: '<i class="iconfont-admin icon-seckill"></i>',
    url: "mobilePromotion",
    platform: "mobile",
    order: 2,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(seckillDefaults)
});
