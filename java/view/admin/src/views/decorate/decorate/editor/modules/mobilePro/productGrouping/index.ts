import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultBuyButtonStyle, defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { ProductGroupingModule } from "./types";

// 演示分组数据是 defaultModule 字面量的一部分（旧 View.vue 的 productGroups 常量，契约冻结）
const productGroups = [
    {
        title: "新品",
        subTitle: "每月上新",
        titleLink: {},
        bannerPic: {},
        bannerLink: {},
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 88.8,
                marketPrice: 98.8,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 56.8,
                marketPrice: 68.88,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg"
            }
        ]
    },
    {
        title: "热卖",
        subTitle: "特别精选",
        titleLink: {},
        bannerPic: {},
        bannerLink: {},
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 88.8,
                marketPrice: 98.8,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 56.8,
                marketPrice: 68.88,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg"
            }
        ]
    },
    {
        title: "折扣",
        subTitle: "便宜好货",
        titleLink: {},
        bannerPic: {},
        bannerLink: {},
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 88.8,
                marketPrice: 98.8,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 56.8,
                marketPrice: 68.88,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg"
            }
        ]
    },
    {
        title: "预售",
        subTitle: "提前锁定",
        titleLink: {},
        bannerPic: {},
        bannerLink: {},
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 88.8,
                marketPrice: 98.8,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872pw39D1oGqdAaZMpvyo.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 56.8,
                marketPrice: 68.88,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872f6EcZnbAqdW5kIGzxL.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872o3s8aqPGdICr1DOCdB.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 48.8,
                marketPrice: 69.9,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872Mry8L9BWOCiawPhp72.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872yPYr4zSj7AXrfw9oON.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "商品的大标题雨价格在后台他会自动调用读取",
                productDesc: "",
                productPrice: 32.0,
                marketPrice: 39.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1732004872NlX49KeGeYSN4Tw0wa.jpg"
            }
        ]
    }
];

/** 与旧 mobilePro/productGrouping/View.vue 的 defaultModule 逐字段一致（契约） */
export const productGroupingDefaults = {
    productSource: "products", //商品数据来源  products:手动选择  productGroups:商品分组
    productGroups: productGroups, // 手动选择商品列表
    groupList: [], // 商品分组列表

    productCardStyle: 1, // 商品卡片样式 1:无边白底, 2:卡片投影 3:描边白底 4:无边透明
    listStyle: 2, // 列表样式  1: "大图模式",2: "一行两个",3: "一行三个",4: "一行四个",5: "一大两小"
    picScale: "1:1", // 图片比例  3:2, 1:1, 3:4, 16:9
    picFillType: "cover", // 图片填充方式  contain:包含  cover:覆盖
    // textAlignment: "center", //导航文字对齐方式 left center

    cardMargin: 6, // 商品横竖间距
    cardPadding: 0, // 商品内边距
    cardRadius: 5, // 商品圆角半径
    picBottomRadius: 1, // 图片底部圆角 1为圆角 0为无圆角

    showProductName: 1, //是否显示商品标题 0为不显示 1为显示
    productNameRow: 1, //商品标题行数 1为单行 2为双行
    productNameWeight: 400, //商品标题字体粗细
    productNameSize: 14, //商品标题字体大小

    // showProductDesc: 1, //是否显示商品描述 0为不显示 1为显示

    showProductPrice: 1, //是否显示商品价格 0为不显示 1为显示
    productPricePic: "", // 价格标签图
    productPriceMarginTop: 10, //商品价格上距
    productPriceWeight: 400, //商品价格字体粗细
    productPrice_size: 14, //商品价格字体大小

    crossedOutPrice: 1, // 商品划线价 0:不显示 1:显示

    buyBtnStyle: defaultBuyButtonStyle, // 购买按钮样式

    bottomMoreBtn: "text", //底部查看更多按钮 text: 文字按钮 image: 自定义图片 close: 不显示
    bottomMoreBtnText: "查看更多", // 底部查看更多按钮文字
    bottomMoreBtnImage: "", // 底部查看更多按钮自定义图片

    productColor: {
        infoTopPadding: 5, // 商品信息顶部内距
        infoBottomPadding: 5, // 商品信息底部内距
        infoLeftRightPadding: 3, // 商品信息左右内距
        backgroundColor: "#ffffff", // 商品背景色

        titleColor: "#555555", // 商品标题颜色
        priceColor: "#72B76C", // 商品价格颜色
        crossedOutPriceColor: "#999999", // 商品划线价颜色

        moreBtnBgColor: "#ffffff", // 底部查看更多按钮背景颜色
        moreBtnBorderColor: "#eeeeee", // 底部查看更多按钮边框颜色
        moreBtnTextColor: "#333333" // 底部查看更多按钮文字颜色
    },
    groupTitle: {
        // effectType: "ceiling", //搜索框/导航效果 ceiling 吸顶  default 不吸顶
        marginLeftRight: 0, // 分组标题间距
        showTitle: 1, //是否显示分组大标题 0为不显示 1为显示
        bigTitleFontSize: 16, //分组大标题字体大小 14:小 16:中 18:大
        titleColor: "#444444", //分组大标题颜色
        activeTitleColor: "#72B76C", //分组大标题选中颜色

        showSubTitle: 1, //是否显示分组副标题 0为不显示 1为显示
        titleFontSize: 14, //分组副标题字体大小 12:小 14:中 16:大
        subTitleColor: "#999999", //分组副标题颜色
        activeSubTitleColor: "#ffffff", //分组副标题选中颜色
        subTitleGradientType: "diagonal", //分组副标题渐变类型 diagonal:斜向 upDown:上下 leftRight:左右
        gradientColorA: "#72b76c", //分组副标题渐变颜色A
        gradientColorB: "#72b76c", //分组副标题渐变颜色B
        subTitleRadius: 15, //分组副标题圆角半径
        splitLineColor: "#DADADA" //标题分割线颜色
        // ceilingBgColor: "#ffffff" //吸顶标题背景色
    },
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "productGrouping",
    label: "商品分组",
    icon: '<i class="iconfont-admin icon-product_grouping"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 3,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(productGroupingDefaults)
});
