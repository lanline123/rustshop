import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultContentStyle, defaultModuleStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { SlideLargeImageModule } from "./types";

// 演示图片是 defaultModule 字面量的一部分（旧 View.vue 的 picList 常量，契约冻结）
const picList = [
    {
        picId: 1424,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731388896OTbuADBoe1LEFefwJo.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731388896OTbuADBoe1LEFefwJo.png",
        picName: "FtohhFYy2fnPEqrcsqpSq5H53DzH"
    },
    {
        picId: 1425,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17313888965IIgWQGqsTYhoe7H7T.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17313888965IIgWQGqsTYhoe7H7T.png",
        picName: "FlVOfGJJyjhQmsiDGJViz_S2clyl"
    },
    {
        picId: 1424,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731388896OTbuADBoe1LEFefwJo.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731388896OTbuADBoe1LEFefwJo.png",
        picName: "FtohhFYy2fnPEqrcsqpSq5H53DzH"
    },
    {
        picId: 1425,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17313888965IIgWQGqsTYhoe7H7T.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17313888965IIgWQGqsTYhoe7H7T.png",
        picName: "FlVOfGJJyjhQmsiDGJViz_S2clyl"
    }
];

/** 与旧 mobilePro/slideLargeImage/View.vue 的 defaultModule 逐字段一致（契约） */
export const slideLargeImageDefaults = {
    picList: picList, // 图片列表
    rowNum: 1.5, //行数
    slidingEffect: "auto", // 滚动效果 auto(自动)/manual(手动)
    slidingSpeed: 25, // 滚动速度
    picMarginTop: 0, // 图片上间距
    picMargin: 10, // 图片间距
    picRadius: 6, // 图片圆角
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    picTitleDesc: {
        //标题描述文字
        show: 1, //是否显示 1:显示 0:不显示
        titleColor: "#ffffff", //标题颜色
        titleFontSize: 14, //标题字体大小
        titleFontWeight: 400, //标题字体粗细
        descColor: "#ffffff", //描述颜色
        descFontSize: 12, //描述字体大小
        descFontWeight: 0, //描述字体粗细
        fontPosition: "", //字体位置 internal:内部 external:外部
        textAlignment: "right", //文字对齐方式 left:左对齐 center:居中 right:右对齐
        marginTop: 10, //文字上间距
        marginBottom: 10, //文字下间距
        marginLeft: 0, //文字侧边间距
        backgroundColor: "" //内容背景颜色
    },
    moduleTitle: {
        //标题设置
        titleText: "", //大标题文字
        subTitleText: "", //小标题文字
        titlePosition: "top", //标题位置 top:居上 bottom:居下
        textAlignment: "right", //文字对齐方式 left:左对齐 center:居中 right:右对齐
        titleMarginTop: 0, //标题上间距
        titleMarginBottom: 0, //标题下间距
        titleMarginLeft: 0, //标题侧边距离
        titleColor: "#ffffff", //大标题颜色
        titleFontSize: 16, //大标题字体大小
        titleFontWeight: 700, //大标题字体粗细
        subFontTitleColor: "#ffffff", //小标题颜色
        subFontTitleSize: 12, //小标题字体大小
        subFontTitleWeight: 700 //小标题字体粗细
    },
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "slideLargeImage",
    label: "大图滑动",
    icon: '<i class="iconfont-admin icon-slide_large_image"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 14,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(slideLargeImageDefaults)
});
