/**
 * PC 图片广告模块（新套可视化，对齐 Vort pc-image-ad）
 *
 * 注意与旧 pcImageAd（camelCase，占位预览）/ pcCustomPage 的 pcImageAd 并存：
 * type 不同（kebab-case），互不影响
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcImageAdModule } from "./types";
import type { PicListItem } from "../_shared/types";

export type { PcImageAdModule } from "./types";

const demoPicList: PicListItem[] = [
    {
        picId: "1",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picName: "演示图片1",
        picTitle: "广告位1",
        picLink: { link: "", name: "" }
    },
    {
        picId: "2",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picName: "演示图片2",
        picTitle: "广告位2",
        picLink: { link: "", name: "" }
    }
];

export const pcImageAdDefaults: PcImageAdModule = {
    picType: 1,
    picList: demoPicList,
    swiperPreView: 1,
    swiperPageColor: "#333333",
    imgPadding: 0,
    picPageType: 1,
    picRadioStyle: 1,
    isFluxWidth: 0,
    frame: {
        textColor: "",
        itemBackgroundColor: "",
        backgroundColor: "#ffffff",
        innerPadding: 0,
        itemHeight: 35,
        itemRadius: 3,
        boxRadius: 0,
        boxPadding: 10,
        boxPaddingTop: 5,
        boxPaddingBottom: 5
    },
    title: {
        showTitle: 0,
        titleStyle: 1,
        titleAlign: 1,
        titleBackground: "",
        titleBackground2: "",
        titleBackgroundPic: { picUrl: "", picThumb: "" },
        titleRadius: 0,
        titleText: "标题内容",
        titleColor: "",
        descText: "",
        descColor: "#aaaaaa",
        showMore: 0,
        moreLink: { link: "", title: "" },
        moreColor: "#aaaaaa"
    }
};

registerModule({
    type: "pc-image-ad",
    label: "图片广告",
    icon: '<i class="iconfont-admin icon-image_nav"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 10,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcImageAdDefaults)
});
