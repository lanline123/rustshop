import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultSubTitle, defaultModuleStyle } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { SlidingImageModule } from "./types";

// 演示图片是 defaultModule 字面量的一部分（旧 View.vue 的 picList 常量，契约冻结）
const picList = [
    {
        picId: 1417,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731293094u3csMxzo4Wzksk1uTw.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731293094u3csMxzo4Wzksk1uTw.jpg",
        picName: "FvsG9vzOZj0aUGNdgE0VrqzDqpu",
        gradientColorA: "rgba(235, 152, 115, 1)",
        gradientColorB: "rgba(234, 92, 88, 1)",
        picTitle: "每日特价",
        picDesc: "2件5折"
    },
    {
        picId: 1418,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17312930944dZgpDLjByAzYyejYt.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17312930944dZgpDLjByAzYyejYt.jpg",
        picName: "Fru1GfeNP4JIvRLqI61o8NT36ZIe",
        gradientColorA: "rgba(243, 115, 130, 1)",
        gradientColorB: "rgba(229, 65, 116, 1)",
        picTitle: "精选好货",
        picDesc: "9.9秒杀"
    },
    {
        picId: 1420,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731293094BFUdJ8FAzQ6sHI9cs8.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731293094BFUdJ8FAzQ6sHI9cs8.jpg",
        picName: "Fmkw5_XrRUmaNYs1jk2858je58Qi",
        gradientColorA: "rgba(219, 123, 248, 1)",
        gradientColorB: "rgba(154, 39, 241, 1)",
        picTitle: "加量不加价",
        picDesc: "最高加一倍"
    },
    {
        picId: 1419,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1731293094bHw28wIxfKlXorX1Rs.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1731293094bHw28wIxfKlXorX1Rs.jpg",
        picName: "FglfJu8K11jusC9gUKjkZU3mZ3eE",
        gradientColorA: "rgba(116, 214, 172, 1)",
        gradientColorB: "rgba(98, 193, 96, 1)",
        picTitle: "下单即发货",
        picDesc: "满49包邮"
    }
];

/** 与旧 mobilePro/slidingImage/View.vue 的 defaultModule 逐字段一致（契约） */
export const slidingImageDefaults = {
    picList: picList, // 图片列表
    backgroundGradientType: "diagonal", // 背景渐变类型 "upDown": "上下","leftRight": "左右","diagonal": "斜向","purity": "单色"
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    picWidth: 25, // 图片宽度
    picMargin: 0, // 图片间距
    picPadding: 4, // 图片内边距
    picRadius: 15, // 图片圆角
    picReducePercent: 0.85, // 图片缩小比例
    subTitle: defaultSubTitle, //标题文字
    showDesc: 1, // 是否显示描述 0:不显示, 1:显示
    descColor: "#ffffff", // 描述文字颜色
    moduleStyle: defaultModuleStyle, // 模块样式
    autoPlay: 1, //自动轮播
    interval: 5 // 轮播间隔 单位秒
};

registerModule({
    type: "slidingImage",
    label: "小图滑动",
    icon: '<i class="iconfont-admin icon-sliding_image"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 15,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(slidingImageDefaults)
});
