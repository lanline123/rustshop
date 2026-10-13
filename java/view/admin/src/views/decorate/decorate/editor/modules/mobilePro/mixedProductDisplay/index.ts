import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { getDaysLaterEndTime, formatTime } from "@/utils/time";
import { isPro } from "@/utils/version";

export type { MixedProductDisplayModule } from "./types";

// 演示图与分组数据是 defaultModule 字面量的一部分（旧 View.vue 的 url/groups 常量，契约冻结）
const url = {
    picId: 1454,
    picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293Kfvp2BXElAzXCOY7pq.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picUrl: "https://oss.tigshop.com/img/gallery/202411/1731650293Kfvp2BXElAzXCOY7pq.png",
    picName: "Fi_ZwwrzwDQddtmqTBCAyht4dPI"
};
const groups = [
    {
        groupTitle: "限时福利",
        groupSubTitle: "直触底价 先到先得",
        groupType: "product",
        pruductList: [
            {
                productId: 0,
                productSn: "",
                productName: "德宝纸巾",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293KF9TjvpDKHOATMR3H7.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "薯片",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650294a4EsPq1zkYme6p5OhC.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "饼干",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650294hYUnaJSx7UhsjhhGEi.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "奶粉",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293zLV7bKUMAFWH6hrM7m.jpg"
            }
        ],
        moreLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        }
    },
    {
        groupTitle: "发现好货",
        groupSubTitle: "精选推荐 多买多省",
        groupType: "product",
        pruductList: [
            {
                productId: 0,
                productSn: "",
                productName: "高露洁牙膏",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/17316502936Ow5WaZszFnuYuGzB8.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "黑人牙膏",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293Tx6p8XKkDJPhDOOAT3.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "高露洁牙膏",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650293yDagFw4VE9FG1oJSnW.jpg"
            },
            {
                productId: 0,
                productSn: "",
                productName: "黑人牙膏",
                productDesc: "",
                productPrice: 39.0,
                marketPrice: 1999.0,
                picThumb: "https://oss.tigshop.com/img/gallery/202411/1731650294KeYpvFBkgkloWdbqX7.jpg"
            }
        ],
        moreLink: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        }
    }
];

/** 与旧 mobilePro/mixedProductDisplay/View.vue 的 defaultModule 逐字段一致（契约） */
export const mixedProductDisplayDefaults = {
    productGroups: groups, // 商品分组
    rightPic: url, // 右侧图片
    showCountdown: 1, // 是否显示倒计时 1:显示, 0:不显示
    countdownType: "dailyCycle", // 倒计时类型  dailyCycle: 每日循环 specifyTime: 指定时间
    dailyTime: [formatTime(new Date(), 8), formatTime(new Date(), 20)], // 倒计时开始和结束时间
    specifyTime: getDaysLaterEndTime(3), // 指定时间  默认值3天后
    timeContent: ["时", "分", "秒"], // 倒计时内容  时 分 秒 毫秒

    picScale: "1:1", // 图片比例  3:2, 1:1, 3:4, 16:9
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    productRadius: 6, // 商品圆角
    morePosition: "top", // 更多链接占位 top:顶部标题区, auto:标题+商品区

    showProductPrice: 1, // 是否显示商品价格 1:显示, 0:不显示
    priceWeight: 400, // 商品价格字体粗细
    crossedOutPrice: 1, // 是否显示商品划线价 1:显示, 0:不显示
    productSales: 0, // 商品销量 0:不显示, 1:显示

    moduleColor: {
        // 模块颜色设置
        titleColor: "#171B1E", // 模块大标题颜色
        sellingPointColor: "#999999", // 卖点小标题颜色
        countdownBackgroundColor: "#72B76C", // 倒计时背景颜色
        countdownColor: "#ffffff", // 倒计时数字颜色

        priceBackgroundColor: "#72B76C", // 商品价格背景颜色
        priceColor: "#ffffff", // 商品价格颜色
        crossedOutPriceColor: "#999999", // 商品划线价格颜色
        lineColor: "#eeeeee" // 中间分割线颜色
    },

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "mixedProductDisplay",
    label: "商品混排",
    icon: '<i class="iconfont-admin icon-mixed_product_display"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 9,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(mixedProductDisplayDefaults)
});
