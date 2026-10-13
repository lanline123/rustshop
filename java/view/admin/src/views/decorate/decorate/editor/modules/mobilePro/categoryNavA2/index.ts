import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultSubTitle, defaultModuleStyle, defaultContentStyle, defaultAnimation } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { CategoryNavA2Module } from "./types";

const categoryList = [
    {
        picId: 1390,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340xhqljwDakcNeJXCPna.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340xhqljwDakcNeJXCPna.png",
        picName: "FoAbizq7oeHiwJzoJVaBmgV50oRN",
        picTitle: "折扣专区"
    },
    {
        picId: 1396,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958341AwZmIgqi2MgyFeZuUt.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958341AwZmIgqi2MgyFeZuUt.png",
        picName: "Ft0N3qmFphVAkmv8Pn6QDp61Qw7",
        picTitle: "运动旅行"
    },
    {
        picId: 1398,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958342pNWuK2IUhmpjICKmzC.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958342pNWuK2IUhmpjICKmzC.png",
        picName: "FuiW1ysk28blbkbFpw6FHFJDKiOA",
        picTitle: "个护护理"
    },
    {
        picId: 1393,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17309583411C1elxz6qd8TAbdnTB.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17309583411C1elxz6qd8TAbdnTB.png",
        picName: "Fra7mrH3EH2NejP7y3K9LrcoGEaH",
        picTitle: "彩妆护肤"
    },
    {
        picId: 1392,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958341pqcAwTAnyf1I8qQtRN.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958341pqcAwTAnyf1I8qQtRN.png",
        picName: "FrPMN2wDsjJoc6ydlCHge82oon6R",
        picTitle: "母婴用品"
    },
    {
        picId: 1387,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340219IHpDSXO5TlSVDNb.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340219IHpDSXO5TlSVDNb.png",
        picName: "FjF9irxfLxeOpMunPtgK2Gjoocb",
        picTitle: "数码家电"
    },
    {
        picId: 1395,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958341ltUrGddGs3uvLccUTu.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958341ltUrGddGs3uvLccUTu.png",
        picName: "Fr9nt9qxQxefziAVXZIGkQZx1goQ",
        picTitle: "洗护用品"
    },
    {
        picId: 1399,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958342xoZNYWFWnp2ltRzpzO.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958342xoZNYWFWnp2ltRzpzO.png",
        picName: "FulNvSiPYQdW_EOcqb851h3fuIrp",
        picTitle: "整理收纳"
    },
    {
        picId: 1386,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340uaWpBXrJkO4hghTti6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340uaWpBXrJkO4hghTti6.png",
        picName: "FnbMGPun7yw92rulVAnOChQpfukU",
        picTitle: "家居生活"
    },
    {
        picId: 1397,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958341CjpCfA4M71ErjOZuBA.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958341CjpCfA4M71ErjOZuBA.png",
        picName: "Ftn_vGbQk_MQfqgB0ttsh0dWhj",
        picTitle: "服装服饰"
    },
    {
        picId: 1394,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958341DTU3wlQ5Iv2Phj2RUd.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958341DTU3wlQ5Iv2Phj2RUd.png",
        picName: "Fs4k1iKK6V1vSz1BeXlrHmMPCNwZ",
        picTitle: "家居生活"
    },
    {
        picId: 1389,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340zy5c1MTCtT41yA6Y7X.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340zy5c1MTCtT41yA6Y7X.png",
        picName: "FkmgdX63YoSx3nXZLroarmC5b",
        picTitle: "美味零食"
    },
    {
        picId: 1391,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340LIruk4KbAEdw3LgOA6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340LIruk4KbAEdw3LgOA6.png",
        picName: "FpttgR5n4ZIHVXn4jf0_ZM9PpBl",
        picTitle: "酒水饮料"
    },
    {
        picId: 1388,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730958340Jh4hoAMEWqjuMWVSzf.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730958340Jh4hoAMEWqjuMWVSzf.png",
        picName: "Fmv5JqMd6qokW4bBjoSaBTWEaMt",
        picTitle: "鲜花礼品"
    }
];

/** 与旧 mobilePro/categoryNavA2/View.vue 的 defaultModule 逐字段一致（契约） */
export const categoryNavA2Defaults = {
    listStyleType: "list", // 列表样式 list:固定, slide:单列滑动 slide2:双排滑动
    categoryList: categoryList, //分类列表
    rowNum: "custom", //行数 number | string
    rowPicPercent: 18, //图片占比宽度
    animation: defaultAnimation, //动画
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    picPercent: 90, // 图片百分比
    radius: 200, // 圆角
    categoryPadding: 10, // 分类间距
    subTitle: defaultSubTitle, //分类标题文字

    indicatorBar: 1, // 底部指示条
    barWidth: 30, // 底部指示条宽度
    barHeight: 2, // 底部指示条高度
    barWidthOn: 30, // 底部指示条选中高度
    barColor: "#eeeeee", // 底部指示条默认颜色
    barColorOn: "#ff6600", // 底部指示条选中颜色

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "categoryNavA2",
    label: "类目导航A2",
    icon: '<i class="iconfont-admin icon-category_nav"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 20,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(categoryNavA2Defaults)
});
