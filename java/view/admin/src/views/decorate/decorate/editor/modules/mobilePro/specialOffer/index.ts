import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle, defaultContentStyle } from "@/views/decorate/decorate/src/modules/index";
import { getDaysLaterEndTime, formatTime } from "@/utils/time";
import { isPro } from "@/utils/version";

export type { SpecialOfferModule } from "./types";

// 演示商品数据是 defaultModule 字面量的一部分（旧 View.vue 的 pruductList 常量，契约冻结）
const pruductList = [
    {
        productId: 0,
        productSn: "",
        productName: "零食",
        productDesc: "",
        productPrice: 32.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17316338156QyBKObq7Wuw7KCqKa.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "饼干",
        productDesc: "",
        productPrice: 32.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731633815i7FmGX6OEWOBsQ3DUh.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "茶叶",
        productDesc: "",
        productPrice: 32.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731633815zoFfIRIsE4yJ0a5LJn.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "干货",
        productDesc: "",
        productPrice: 32.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731633815UdOFkntVJP4GXu9KsW.jpg"
    }
];

// 模块颜色风格 orange:番茄橙,red:中国红,blue:天空蓝,green:森林绿,purple:紫罗兰,grey:深灰色,chocolate:巧克力,brown:马鞍棕
/** 与旧 mobilePro/specialOffer/View.vue 的 defaultModule 逐字段一致（契约） */
export const specialOfferDefaults = {
    productSource: "products", // 商品来源 手动选择: products, 商品分组: productGroup
    productGroups: [], // 商品分组
    productList: pruductList || [], // 商品列表
    moduleColorStyle: "orange", // 模块颜色风格
    prodcutTitles: [], // 自定义商品标题/卖点
    showProdcutTitles: 1, // 是否显示自定义商品标题/卖点 1:显示, 0:不显示
    moduleTitle: {
        // 模块标题
        title: "天天特价", // 大标题
        subTitle: "每日精选", // 小标题
        countdownTitle: "距结束", // 倒计时文案
        countdownType: "dailyCycle", // 倒计时类型  dailyCycle: 每日循环 specifyTime: 指定时间
        dailyTime: [formatTime(new Date(), 8), formatTime(new Date(), 20)], // 倒计时开始和结束时间
        specifyTime: getDaysLaterEndTime(3), // 指定时间  默认值3天后
        topRightType: "countdown", // 顶部右侧展示：countdown=倒计时（默认） / button=按钮
        buttonText: "立即抢购", // 按钮文案（topRightType=button 时生效）
        buttonLink: {} // 按钮跳转链接（SelectLink LinkType 数据结构）
    },
    picScale: "1:1", // 图片比例  3:2, 1:1, 3:4, 16:9
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    productWidth: 25, // 商品宽度
    productRadius: 6, // 商品圆角
    showProductName: 1, // 是否显示商品名称 1:显示, 0:不显示
    productNameSize: 14, // 商品名称字体大小
    productNameWeight: 400, // 商品名称字体粗细
    showProductPrice: 1, // 是否显示商品价格 1:显示, 0:不显示
    // priceDecimal: 0, //商品价格小数位数 0:不显示  1:显示1位 2:显示2位
    priceWeight: 400, // 商品价格字体粗细
    showSellingPoints: 1, // 是否显示商品卖点 1:显示, 0:不显示
    // soldOutIcon: 0, //缺货图标 1:显示 0:不显示

    moduleColor: {
        // 模块颜色设置
        titleColor: "#ffffff", // 模块大标题颜色
        subTitleColor: "#ffffff", // 模块小标题颜色
        countdownColor: "#ffffff", // 倒计时颜色
        countdownTitleColor: "#72B76C", // 倒计时文案颜色
        buttonTextColor: "#ffffff", // 按钮文字颜色
        buttonGradientColorA: "", // 按钮渐变起始色（留空 → 跟随主题色）
        buttonGradientColorB: "", // 按钮渐变结束色（留空 → 跟随主题色）

        productNameColor: "#555555", // 商品名称颜色
        sellingPointsColor: "#FB6D08" // 商品卖点颜色
    },

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "specialOffer",
    label: "天天特价",
    icon: '<i class="iconfont-admin icon-special_offer"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 10,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(specialOfferDefaults)
});
