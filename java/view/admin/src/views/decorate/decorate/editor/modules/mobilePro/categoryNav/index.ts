import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultSubTitle, defaultModuleStyle, defaultContentStyle, defaultAnimation } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { CategoryNavModule } from "./types";

const categoryList = [
    {
        picId: 1377,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947YV2lncqLSOSg8OvpbK.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947YV2lncqLSOSg8OvpbK.png",
        picName: "dangrisuda",
        picTitle: "当日速达"
    },
    {
        picId: 1384,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957948LcXRbvCuwlCHyHy7Y0.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957948LcXRbvCuwlCHyHy7Y0.png",
        picName: "xiuxianlingshi",
        picTitle: "休闲零食"
    },
    {
        picId: 1378,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947XV6cPNonZJurGcDfOP.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947XV6cPNonZJurGcDfOP.png",
        picName: "mimianliangyou",
        picTitle: "粮油米面"
    },
    {
        picId: 1385,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/17309579481I5gopEn7PkOneF0NU.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/17309579481I5gopEn7PkOneF0NU.png",
        picName: "ruyinjiushui",
        picTitle: "乳饮酒水"
    },
    {
        picId: 1382,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957948Ouz6wPq8cQkgSjDCkF.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957948Ouz6wPq8cQkgSjDCkF.png",
        picName: "riyongbaihuo",
        picTitle: "日用百货"
    },
    {
        picId: 1380,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947iOLEngIwvzxZDjhtQ6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947iOLEngIwvzxZDjhtQ6.png",
        picName: "muyingyongpin",
        picTitle: "母婴用品"
    },
    {
        picId: 1379,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947lWqY7UySCwQ89qsq7f.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947lWqY7UySCwQ89qsq7f.png",
        picName: "gerenxihu",
        picTitle: "个人洗护"
    },
    {
        picId: 1383,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957948vFCfxjt0TIROPOocWI.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957948vFCfxjt0TIROPOocWI.png",
        picName: "zhipinjiaqing",
        picTitle: "纸品家清"
    },
    {
        picId: 1376,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947BswaPOfoOZnn8tDSX6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947BswaPOfoOZnn8tDSX6.png",
        picName: "meironghufu",
        picTitle: "护肤美容"
    },
    {
        picId: 1381,
        picThumb: "https://oss.tigshop.com/img/gallery/202411/1730957947vIKQibO98m2q9oP8ct.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.tigshop.com/img/gallery/202411/1730957947vIKQibO98m2q9oP8ct.png",
        picName: "fangbiansushi",
        picTitle: "方便速食"
    }
];

/** 与旧 mobilePro/categoryNav/View.vue 的 defaultModule 逐字段一致（契约） */
export const categoryNavDefaults = {
    listStyleType: "list", // 列表样式 list:固定, slide:横向滑动
    categoryList: categoryList, //分类列表
    rowNum: 5, //行数
    animation: defaultAnimation, //动画
    picFillType: "cover", // 图片填充类型 cover:填充, contain:周边留白
    picPercent: 90, // 图片百分比
    radius: 200, // 圆角
    categoryPadding: 10, // 分类间距
    subTitle: defaultSubTitle, //分类标题文字
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "categoryNav",
    label: "类目导航",
    icon: '<i class="iconfont-admin icon-category_nav"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 18,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(categoryNavDefaults)
});
