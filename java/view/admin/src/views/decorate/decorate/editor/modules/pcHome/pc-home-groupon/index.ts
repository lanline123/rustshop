/**
 * PC 首页拼团模块（新套可视化，对齐 Vort pc-home-groupon）
 * 与旧 pcGroupon（camelCase，占位预览）并存：type 不同，互不影响
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcHomeGrouponModule } from "./types";

export type { PcHomeGrouponModule } from "./types";

/**
 * PC 首页拼团模块默认值
 * - 对齐前台默认体验
 */
export const pcHomeGrouponDefaults: PcHomeGrouponModule = {
    // 基础设置
    style: 4, // 默认一行 6 个
    groupType: 1, // 普通拼团
    cardStyle: 1, // 样式一（priceBox + 图片倒计时 overlay）

    // 显示控制
    showName: 1, // 显示商品名称
    showPrice: 1, // 显示商品价格

    // 对齐/间距
    textAlign: 1, // 左对齐
    goodsPadding: 0, // 无间距

    // 容器样式
    backgroundColor: "#ffffff",
    boxRadius: 0,
    innerPadding: 0,
    boxPadding: 0,
    boxPaddingTop: 5,
    boxPaddingBottom: 5,

    // 标题设置（默认不显示，用户可按需开启）
    title: {
        showTitle: 0,
        titleStyle: 1,
        titleAlign: 1,
        titleBackground: "",
        titleBackground2: "",
        titleBackgroundPic: { picUrl: "", picThumb: "" },
        titleRadius: 0,
        titlePaddingTop: 20,
        titlePaddingBottom: 20,
        titleText: "拼团",
        titleColor: "#2a3145",
        descText: "",
        descColor: "#aaaaaa",
        showMore: 0,
        // 右侧区默认走倒计时，与老数据（showMore=1 即倒计时）语义一致
        moreType: 2,
        countdownLabel: "距结束",
        moreLink: { link: "", title: "" },
        moreColor: "#1a1a1a"
    },

    // 购买按钮
    buyBtnStyle: 0, // 默认不显示购买按钮

    // 活动数据
    grouponActivityItem: null
};

registerModule({
    type: "pc-home-groupon",
    label: "拼团",
    icon: '<i class="iconfont-admin icon-seckill"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 83,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcHomeGrouponDefaults)
});
