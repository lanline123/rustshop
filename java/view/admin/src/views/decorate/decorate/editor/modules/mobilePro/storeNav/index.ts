import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultSubTitle, defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro, isStore } from "@/utils/version";

import { StoreNavSource, StoreNavStyle, type StoreNavShopItem } from "./types";

export type { StoreNavModule } from "./types";

export const storeNavDefaults = {
    subTitle: defaultSubTitle, //标题文字
    storeTitleStyle: {
        showTitle: 1,
        titleColor: "#1a1a1a",
        titleFontSize: 14,
        titleFontBold: 700
    }, // 门店标题样式
    titlePaddingY: 0, // 标题上下间距（样式一）
    titlePaddingX: 0, // 标题左右间距（样式一）
    storeStyle: StoreNavStyle.One as number, // 门店样式 1:样式一 2:样式二
    storeSource: StoreNavSource.Auto as number, // 展示门店 1:自动获取 2:指定店铺
    storeNum: 10, // 自动获取时展示的门店数量
    storeList: [] as StoreNavShopItem[], // 指定店铺列表，按顺序展示
    showRating: 1, // 是否显示门店评分 0:不显示, 1:显示
    showFollow: 1, // 是否显示关注按钮 0:不显示, 1:显示（样式二）
    showMinOrder: 1, // 是否显示起送价 0:不显示, 1:显示（样式二）
    showDeliveryFee: 1, // 是否显示配送费 0:不显示, 1:显示（样式二）
    showDistance: 1, // 是否显示距离 0:不显示, 1:显示（样式二）
    storeGap: 15, // 门店项间距
    storeBackgroundColor: "#ffffff", // 门店背景颜色
    storePaddingY: 0, // 门店上下间距
    storePaddingX: 0, // 门店左右间距
    storeRadius: 0, // 门店圆角半径
    productGap: 8, // 商品项间距（样式二）
    showProductName: 1, // 是否显示商品名称 0:不显示, 1:显示（样式二）
    productNameRow: 1, // 商品名称行数 1/2（样式二）
    showProductPrice: 1, // 是否显示商品价格 0:不显示, 1:显示（样式二）
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: { ...defaultContentStyle, backgroundColor: "#ffffff", paddingTopBottom: 10 } // 内容样式
};

registerModule({
    type: "storeNav",
    label: "门店导航",
    icon: '<i class="iconfont-admin icon-category_nav"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 23,
    enabled: () => isPro() && isStore(),
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(storeNavDefaults)
});
