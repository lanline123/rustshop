import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultSubTitle, defaultModuleStyle, defaultContentStyle, defaultAnimation } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { CategoryNavA1Module } from "./types";

const categoryList = [
    {
        picId: 1519,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1732608267mJFZA5uFuT5974JXl8.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1732608267mJFZA5uFuT5974JXl8.png",
        picName: "FgisG78Lwl4UG3y5fBiEUnLrxIV",
        picTitle: "诚信经营"
    },
    {
        picId: 1522,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1732608267sJb7DHOuVX8896buhT.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1732608267sJb7DHOuVX8896buhT.png",
        picName: "FpnzuzjBafleMMwgzc5MvNITxVHj",
        picTitle: "品质保障"
    },
    {
        picId: 1521,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1732608267yXWHECJxn75eqyk60Q.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1732608267yXWHECJxn75eqyk60Q.png",
        picName: "Fh56a7Prcs9wghXkxYtAHGlF3_uT1",
        picTitle: "贴心服务"
    },
    {
        picId: 1520,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17326082679hhyPXOj9i02unfUgw.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17326082679hhyPXOj9i02unfUgw.png",
        picName: "Fn5u9A8vmu2DAG404sp8z_016zdK",
        picTitle: "购物无忧"
    }
];

/** 与旧 mobilePro/categoryNavA1/View.vue 的 defaultModule 逐字段一致（契约） */
export const categoryNavA1Defaults = {
    categoryList: categoryList, //分类列表
    listStyleType: "list", // 列表样式 list:固定, slide:横向滑动
    rowNum: 4, //每行显示数量
    animation: defaultAnimation, //动画
    imageBackgroundColor: "", // 图片背景色
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    picPercent: 90, // 图片百分比
    radius: 200, // 圆角
    categoryPadding: 10, // 分类间距
    subTitle: defaultSubTitle, //分类标题文字
    showSplitLine: 1, // 显示分割线
    radiusStyle: 1, //图片形状 1:圆形, 2:方形

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "categoryNavA1",
    label: "类目导航A1",
    icon: '<i class="iconfont-admin icon-category_nav"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 19,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(categoryNavA1Defaults)
});
