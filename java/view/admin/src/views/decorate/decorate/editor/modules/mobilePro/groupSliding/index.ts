import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle, defaultBuyButtonStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { GroupSlidingModule } from "./types";

const groupList = [
    {
        title: "日用清洁热卖榜〉",
        subTitle: "快来跟大家一起买",
        titleLink: {
            link: "",
            title: ""
        },
        backgroundColor: "#72b76c",
        label: `今天直降十元 
                加量不加价 
                抢超值2大桶
                加量不加价 
                抢超值2大桶`,
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190474y0XWFxO8GJZF0dg64.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190474uihMnJYOkEzvGBeWv.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919047ZXFKxeDYmDn9zNQSA3.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190476keLc7Twr4BYAVlUBQ.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/173191904794zD2Sy4XugNwMUBOW.jpg"
            }
        ]
    },
    {
        title: "家居日用热卖榜〉",
        subTitle: "正能家居 生活好帮手",
        titleLink: {
            link: "",
            title: ""
        },
        backgroundColor: "#818dfc",
        label: `今天直降十元 
                加量不加价 
                抢超值2大桶
                加量不加价 
                抢超值2大桶`,
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17319190597yMADwwbOKqaA9pN5O.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919059AeiNkel9O1UtD2eZeD.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919059ts7YHQo7rbPam9y8ZQ.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919059VFsxMuKvPxKoFNfq5y.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919059GV4LN9Xq2j5ACzoJqx.jpg"
            }
        ]
    },
    {
        title: "家居日用热卖榜〉",
        subTitle: "正能家居 生活好帮手",
        titleLink: {
            link: "",
            title: ""
        },
        backgroundColor: "#ff6600",
        label: `今天直降十元 
                加量不加价 
                抢超值2大桶
                加量不加价 
                抢超值2大桶`,
        productList: [
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071axvkfUDkQNFbIzF1iY.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071lOa0ZBx2mYlvq1lgjr.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071xUnObq2LokKPFecwXq.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071rwvbdR1BOrBne9YHqd.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "品牌旗舰电商版商超版洗洁精",
                productDesc: "",
                productPrice: 80.0,
                marketPrice: 0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919071r7emytS94COnCxRtcw.jpg"
            }
        ]
    }
];

/** 与旧 mobilePro/groupSliding/View.vue 的 defaultModule 逐字段一致（契约） */
export const groupSlidingDefaults = {
    groupList: groupList, //分组数据
    groupWidth: 70, //分组宽度
    groupRadius: 8, //分组上下圆角
    productPicRatio: 36, //商品图片占比
    productPicRadius: 6, //商品图片圆角
    productPicRightRadius: 1, //商品图片右侧圆角 0为无圆角 1为圆角

    showRanking: 1, //是否显示排名 0为不显示 1为显示
    showProductLabel: 1, //是否显示商品标签 0为不显示 1为显示

    showProductName: 1, //是否显示商品标题 0为不显示 1为显示
    productNameRow: 1, //商品标题行数 1为单行 2为双行
    productNameWeight: 400, //商品标题字体粗细
    productNameSize: 14, //商品标题字体大小

    showProductPrice: 1, //是否显示商品价格 0为不显示 1为显示
    productPrice_weight: 700, //商品价格字体粗细
    productPrice_size: 14, //商品价格字体大小
    // showDiscountLabel: 0, //是否显示商优惠价标签 0为不显示 1为显示
    productPriceMarginTop: 10, //商品价格上距

    crossedOutPrice: 1, // 商品划线价 0:不显示 1:显示

    buyBtnStyle: defaultBuyButtonStyle, // 购买按钮样式
    moduleColor: {
        titleSize: 20, // 模块大标题字体大小
        subTitleSize: 14, // 模块小标题字体大小
        titleColor: "#ffffff", // 模块大标题颜色
        subTitleColor: "#ffffff", // 模块小标题颜色
        titlePic: {
            // 模块大标题图标
            picId: 1489,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731919039RxeYzkD0P8ByOeKiNa.png?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731919039RxeYzkD0P8ByOeKiNa.png",
            picName: "FljdiuyonglOZKlYhkv9ZcrSw44_"
        },

        productBackgroundColor: "#ffffff", // 商品背景颜色
        productNameColor: "#333333", // 商品背景颜色
        labelBackgroundColor: "#FFF1F6", // 商品标签背景颜色
        labelTextColor: "#FB264D", // 商品标签文字颜色
        productPriceColor: "#72b76c", // 商品价格颜色
        crossedOutPriceColor: "#999999" // 商品划线价颜色
    },
    moduleStyle: defaultModuleStyle // 模块样式
};

registerModule({
    type: "groupSliding",
    label: "分组滑动",
    icon: '<i class="iconfont-admin icon-group_sliding"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 4,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(groupSlidingDefaults)
});
