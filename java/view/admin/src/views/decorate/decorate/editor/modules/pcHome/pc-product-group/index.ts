/**
 * PC 商品分组模块（新套可视化，对齐 Vort pc-product-group，仅 PC 首页）
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { ProductGroupModule, ProductGroupItem, ProductColorConfig, GroupTitleConfig, BuyBtnStyle } from "./types";

export type { ProductGroupModule, ProductGroupItem, ProductColorConfig, GroupTitleConfig, BuyBtnStyle } from "./types";

// ==================== 默认值定义 ====================

// 默认购买按钮样式
export const defaultBuyBtnStyle: BuyBtnStyle = {
    showBtn: 1,
    btnType: 1,
    text: "",
    textColor: "#ffffff",
    iconColor: "#ffffff",
    paddingLeftRight: 3
};

// 默认商品颜色配置
const defaultProductColor: ProductColorConfig = {
    infoLeftRightPadding: 10,
    backgroundColor: "#ffffff",
    titleColor: "#19191b",
    priceColor: "",
    crossedOutPriceColor: "#999999"
};

// 默认分组标题配置
const defaultGroupTitle: GroupTitleConfig = {
    marginLeftRight: 5,
    showTitle: 1,
    bigTitleFontSize: 14,
    titleColor: "#444444",
    activeTitleColor: "#FF0F23",
    activeTitleBgColor: "#ffebf1",
    titleMinWidth: 80,
    titlePaddingTopBottom: 8,
    titlePaddingLeftRight: 12,
    titleRadius: 4,
    splitLineColor: "transparent"
};

// 演示商品列表
const demoProductList = [
    {
        productId: 0,
        productSn: "",
        productName: "商品的大标题雨价格在后台他会自动调用读取",
        productDesc: "",
        productPrice: 88.8,
        marketPrice: 98.8,
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "商品的大标题雨价格在后台他会自动调用读取",
        productDesc: "",
        productPrice: 56.8,
        marketPrice: 68.88,
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "商品的大标题雨价格在后台他会自动调用读取",
        productDesc: "",
        productPrice: 48.8,
        marketPrice: 69.9,
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "商品的大标题雨价格在后台他会自动调用读取",
        productDesc: "",
        productPrice: 48.8,
        marketPrice: 69.9,
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "商品的大标题雨价格在后台他会自动调用读取",
        productDesc: "",
        productPrice: 32.0,
        marketPrice: 39.0,
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "商品的大标题雨价格在后台他会自动调用读取",
        productDesc: "",
        productPrice: 32.0,
        marketPrice: 39.0,
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg"
    }
];

// 演示商品分组列表
const demoProductGroups: ProductGroupItem[] = [
    {
        sourceType: "products",
        title: "为你推荐",
        titlePic: {
            picUrl: "https://oss.vortmall.com/demo/upload/c4ca4238/2026/02/07/147580fe25834bb09199267ee601b834.png",
            picThumb:
                "https://oss.vortmall.com/demo/upload/c4ca4238/2026/02/07/147580fe25834bb09199267ee601b834.png?x-oss-process=image/resize,m_pad,h_200,h_200"
        },
        titleLink: { link: "", name: "" },
        bannerPic: { picUrl: "", picThumb: "" },
        bannerLink: { link: "", name: "" },
        productList: [...demoProductList]
    },
    {
        sourceType: "products",
        title: "热卖",
        titleLink: { link: "", name: "" },
        bannerPic: { picUrl: "", picThumb: "" },
        bannerLink: { link: "", name: "" },
        productList: [...demoProductList]
    },
    {
        sourceType: "products",
        title: "折扣",
        titleLink: { link: "", name: "" },
        bannerPic: { picUrl: "", picThumb: "" },
        bannerLink: { link: "", name: "" },
        productList: [...demoProductList]
    },
    {
        sourceType: "products",
        title: "预售",
        titleLink: { link: "", name: "" },
        bannerPic: { picUrl: "", picThumb: "" },
        bannerLink: { link: "", name: "" },
        productList: [...demoProductList]
    }
];

// 商品分组模块默认值
export const productGroupDefaults: ProductGroupModule = {
    productSource: "products",
    productGroups: demoProductGroups,
    groupList: [],

    productCardStyle: 1,
    // PC 默认更适合多列展示
    listStyle: 4,

    cardPadding: 0,
    cardRadius: 4,

    showProductName: 1,
    productNameRow: 1,
    productNameWeight: 400,
    productNameSize: 16,

    showProductPrice: 1,
    productPriceMarginTop: 10,
    productPriceWeight: 400,
    productPriceSize: 28,

    crossedOutPrice: 1,

    showSales: 1,

    showShopTag: 1,

    buyBtnStyle: defaultBuyBtnStyle,

    productColor: defaultProductColor,
    groupTitle: defaultGroupTitle,

    moduleStyle: {
        boxPaddingTop: 5,
        boxPaddingBottom: 5,
        boxPadding: 10,
        backgroundColor: ""
    },
    contentStyle: {
        paddingTop: 0,
        paddingBottom: 0,
        paddingLeftRight: 0,
        boxRadius: 0,
        backgroundColor: "",
        backgroundPic: { picUrl: "", picThumb: "" },
        backgroundPicFillType: "cover",
        boxShadow: 0
    }
};

// ==================== 模块自注册 ====================

registerModule({
    type: "pc-product-group",
    label: "商品分组",
    icon: '<i class="iconfont-admin icon-product_grouping"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 15,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(productGroupDefaults)
});
