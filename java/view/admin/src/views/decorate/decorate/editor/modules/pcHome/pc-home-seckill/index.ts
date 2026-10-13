/**
 * PC 首页秒杀模块（新套可视化，对齐 Vort pc-home-seckill）
 * 与旧 pcSeckill（camelCase，占位预览）并存：type 不同，互不影响
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcHomeSeckillModule } from "./types";

export type { PcHomeSeckillModule } from "./types";

export const pcHomeSeckillDefaults: PcHomeSeckillModule = {
    frame: {
        boxPadding: 0,
        boxPaddingTop: 0,
        boxPaddingBottom: 0,
        boxRadius: 0
    },
    seckillTitle: "全场优惠限时9折起",
    title: {
        showTitle: 1,
        titleStyle: 1,
        titleAlign: 1,
        titleBackground: "",
        titleBackground2: "",
        titleBackgroundPic: { picUrl: "", picThumb: "" },
        titleRadius: 0,
        titlePaddingTop: 20,
        titlePaddingBottom: 20,
        titleText: "全场优惠限时9折起",
        titleColor: "#1a1a1a",
        descText: "",
        descColor: "#aaaaaa",
        showMore: 0,
        // 右侧区默认走倒计时，与老数据（showMore=1 即倒计时）语义一致
        moreType: 2,
        countdownLabel: "倒计时",
        moreLink: { link: "", title: "" },
        moreColor: "#1a1a1a"
    },
    goodsGap: 10
};

registerModule({
    type: "pc-home-seckill",
    label: "秒杀",
    icon: '<i class="iconfont-admin icon-seckill"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 84,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcHomeSeckillDefaults)
});
