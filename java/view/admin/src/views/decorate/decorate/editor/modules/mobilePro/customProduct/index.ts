import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultModuleStyle, defaultContentStyle, defaultAnimation } from "@/views/decorate/decorate/src/modules/index";

export type { CustomProductModule } from "./types";

const pruductList = [
    {
        productId: 0,
        productSn: "",
        productName: "麦片商品的标题信息",
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662JqyhZ6f1BCsWeUJlgj.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "巧克力棒商品的标题信息",
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662cOmZ28i8gKl6JiUBad.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "汤达人商品的标题信息",
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662AztoZ2OADHp1b8vUoQ.jpg"
    },
    {
        productId: 0,
        productSn: "",
        productName: "士力架商品的标题信息",
        productDesc: "",
        productPrice: 29.0,
        marketPrice: 0,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731908662O7VZIzaFtItS4OvAOR.jpg"
    }
];
const url = {
    picId: 1516,
    picThumb: "https://oss.tigshop.com/img/gallery/202411/1732167975uqaGwJU2aHObkmOmpb.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picUrl: "https://oss.tigshop.com/img/gallery/202411/1732167975uqaGwJU2aHObkmOmpb.png",
    picName: "FhZQpeGm_m6BK7kV9qd0Phm5ToF"
};

/** 与旧 mobilePro/customProduct/View.vue 的 defaultModule 逐字段一致（契约） */
export const customProductDefaults = {
    pruductList: pruductList, //商品列表
    picUrl: url, // 图片url
    picLocation: "right", //导航文字对齐方式 left right
    animation: defaultAnimation, //动画
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    productRadius: 5, // 商品圆角半径
    picBottomRadius: 1, // 图片底部圆角  1: 直角 2: 圆角
    crossedOutPrice: 1, // 商品划线价 0:不显示 1:显示

    moduleColor: {
        // 模块颜色设置
        productBackgroundColor: "#ffffff", // 商品背景颜色
        productNameColor: "#333333", // 商品标题颜色
        productPriceColor: "#72b76c", // 商品价格颜色
        crossedOutPriceColor: "#999999" // 商品划线价颜色
    },
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

// 旧 ToolBar 中已被注释、不进工具栏；保留注册仅为老数据可渲染
registerModule({
    type: "customProduct",
    label: "个性商品",
    icon: '<i class="iconfont-admin icon-product"></i>',
    url: "mobilePro",
    platform: "mobile",
    visible: false,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(customProductDefaults)
});
