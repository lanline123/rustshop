import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { getDaysLaterEndTime, formatTime } from "@/utils/time";
import { isPro } from "@/utils/version";

export type { CountdownModule } from "./types";

const pruductList = [
    {
        productId: 0,
        productSn: "",
        productName: "麦片",
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662JqyhZ6f1BCsWeUJlgj.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "巧克力棒",
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662cOmZ28i8gKl6JiUBad.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "汤达人",
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662AztoZ2OADHp1b8vUoQ.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "士力架",
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662O7VZIzaFtItS4OvAOR.jpg"
    }
];

/** 与旧 mobilePro/countdown/View.vue 的 defaultModule 逐字段一致（契约） */
export const countdownDefaults = {
    pruductList: pruductList, //商品列表
    moduleColorStyle: "orange", // 模块颜色风格
    moduleTitle: {
        // 模块标题
        title: "限时抢购", // 大标题
        subTitle: "超低价格，史无前例", // 小标题
        moreText: "向左滑动查看更多", // 查看更多文案
        titleLink: {
            // 标题跳转链接
            link: "",
            title: ""
        }
    },
    countdownData: {
        //倒计时数据
        showCountdown: 1, // 是否显示倒计时
        countdownBackgroundPic: "", // 倒计时背景图
        countdownType: "dailyCycle", // 倒计时类型  dailyCycle: 每日循环 specifyTime: 指定时间
        dailyTime: formatTime(new Date(), 20), // 倒计时开始和结束时间
        specifyTime: getDaysLaterEndTime(1), // 指定时间  默认值3天后
        timeContent: ["天", "时", "分", "秒"], // 倒计时内容 天 时 分 秒
        marginTop: 50, // 倒计时距离顶部距离
        countdownPicBottom: 30 // 倒计时图片上浮距离
    },

    rowNum: 3, //每屏显示数量  2 / 2.5 / 3 / 3.5
    picScale: "1:1", // 图片比例  3:2, 1:1, 3:4, 16:9
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    textAlignment: "center", //导航文字对齐方式 left center
    productPadding: 5, // 商品内部间距
    productRadius: 5, // 商品圆角半径
    picBottomRadius: 1, // 图片底部圆角  1: 直角 2: 圆角

    showProductName: 0, // 显示商品名称 0:不显示 1:显示
    showProductPrice: 1, // 显示商品价格 0:不显示 1:显示
    priceWeight: 700, // 商品价格字体粗细
    crossedOutPrice: 1, // 商品划线价 0:不显示 1:显示
    // productSales: 1,  // 显示商品销量 0:不显示 1:显示
    moduleColor: {
        // 模块颜色设置
        titleColor: "#ffffff", // 模块大标题颜色
        subTitleColor: "#ffffff", // 模块小标题颜色
        tipsColor: "#fafafa", // 提示信息颜色
        productBackgroundColor: "#ffffff", // 商品背景颜色
        productNameColor: "#333333", // 商品标题颜色
        productPriceColor: "#72b76c", // 商品价格颜色
        crossedOutPriceColor: "#999999", // 商品划线价颜色
        countdownColor: "#f7f7f7" // 倒计时文本颜色
    },
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "countdown",
    label: "倒计时",
    icon: '<i class="iconfont-admin icon-countdown"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 5,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(countdownDefaults)
});
