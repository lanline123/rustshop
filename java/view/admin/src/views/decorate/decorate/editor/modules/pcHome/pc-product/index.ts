/**
 * PC 商品模块（新套可视化，对齐 Vort pc-product）
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcProductModule } from "./types";

export type { PcProductModule, ProductSelectConfig, ProductItem } from "./types";

const defaultProducts = {
    productSelectType: 1,
    productIds: [],
    productCategoryId: undefined,
    productTag: "",
    productNumber: 10
};

export const pcProductDefaults: PcProductModule = {
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
    imgPadding: 1,
    waterfall: 0,
    swiperPageColor: "",
    frame: {
        textColor: "",
        itemBackgroundColor: "",
        backgroundColor: "#ffffff",
        innerPadding: 0,
        itemHeight: 35,
        itemRadius: 3,
        boxRadius: 0,
        boxPadding: 10,
        leftAndRightPadding: 0,
        boxPaddingTop: 5,
        boxPaddingBottom: 5
    },
    title: {
        showTitle: 0,
        titleStyle: 1,
        titleAlign: 1,
        titleBackground: "",
        titleBackground2: "",
        titleBackgroundPic: { picUrl: "", picThumb: "" },
        titleRadius: 0,
        titleText: "标题内容",
        titleColor: "",
        descText: "",
        descColor: "#aaaaaa",
        showMore: 0,
        moreLink: { link: "", title: "" },
        moreColor: "#aaaaaa"
    },
    products: defaultProducts
};

registerModule({
    type: "pc-product",
    label: "商品",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 14,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcProductDefaults)
});
