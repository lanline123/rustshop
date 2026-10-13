/**
 * PC 首页快捷导航模块（新套可视化，对齐 Vort pc-home-quick-nav）
 * 横向图文导航条，对标 nav_container fs_mod 样式
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcHomeQuickNavModule } from "./types";
import type { PicListItem } from "../_shared/types";

export type { PcHomeQuickNavModule, PcHomeQuickNavStyle } from "./types";

/** 默认导航项列表（图文链接格式） */
const defaultItems: PicListItem[] = [
    {
        picId: "1377",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947YV2lncqLSOSg8OvpbK.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947YV2lncqLSOSg8OvpbK.png",
        picName: "dangrisuda",
        picTitle: "当日速达",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1384",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948LcXRbvCuwlCHyHy7Y0.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948LcXRbvCuwlCHyHy7Y0.png",
        picName: "xiuxianlingshi",
        picTitle: "休闲零食",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1378",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947XV6cPNonZJurGcDfOP.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947XV6cPNonZJurGcDfOP.png",
        picName: "mimianliangyou",
        picTitle: "粮油米面",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1385",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/17309579481I5gopEn7PkOneF0NU.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/17309579481I5gopEn7PkOneF0NU.png",
        picName: "ruyinjiushui",
        picTitle: "乳饮酒水",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1382",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948Ouz6wPq8cQkgSjDCkF.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948Ouz6wPq8cQkgSjDCkF.png",
        picName: "riyongbaihuo",
        picTitle: "日用百货",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1380",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947iOLEngIwvzxZDjhtQ6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947iOLEngIwvzxZDjhtQ6.png",
        picName: "muyingyongpin",
        picTitle: "母婴用品",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1379",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947lWqY7UySCwQ89qsq7f.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947lWqY7UySCwQ89qsq7f.png",
        picName: "gerenxihu",
        picTitle: "个人洗护",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1383",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948vFCfxjt0TIROPOocWI.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957948vFCfxjt0TIROPOocWI.png",
        picName: "zhipinjiaqing",
        picTitle: "纸品家清",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1376",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947BswaPOfoOZnn8tDSX6.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947BswaPOfoOZnn8tDSX6.png",
        picName: "meironghufu",
        picTitle: "护肤美容",
        picLink: { link: "", name: "" }
    },
    {
        picId: "1381",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947vIKQibO98m2q9oP8ct.png?x-oss-process=image/resize,m_pad,h_200,h_200",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1730957947vIKQibO98m2q9oP8ct.png",
        picName: "fangbiansushi",
        picTitle: "方便速食",
        picLink: { link: "", name: "" }
    }
];

/** 模块默认值 */
export const pcHomeQuickNavDefaults: PcHomeQuickNavModule = {
    frame: {
        boxPadding: 0,
        boxPaddingTop: 0,
        boxPaddingBottom: 0,
        backgroundColor: ""
    },
    style: {
        fontSize: 14,
        textColor: "#333333",
        hoverColor: "#e1251b",
        containerAlign: 2,
        itemGap: 16,
        singleLine: 1,
        fontWeight: 700,
        lineHeight: 36,
        iconSize: 18,
        iconGap: 6
    },
    items: defaultItems
};

registerModule({
    type: "pc-home-quick-nav",
    label: "快捷导航",
    icon: '<i class="iconfont-admin icon-category_nav"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 6,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcHomeQuickNavDefaults)
});
