import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";

export type { PcSeckillModule } from "./types";

/** 与旧 pc/pcSeckill/View.vue 的 defaultModule 逐字段一致（契约） */
export const pcSeckillDefaults = {
    seckillTitle: "全场优惠限时9折起"
};

registerModule({
    type: "pcSeckill",
    label: "秒杀模块",
    url: "pc",
    platform: "pc-home",
    order: 4,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcSeckillDefaults)
});
