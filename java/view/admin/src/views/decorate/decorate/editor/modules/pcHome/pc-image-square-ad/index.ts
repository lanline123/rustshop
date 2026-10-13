/**
 * PC 图片魔方模块（新套可视化，对齐 Vort pc-image-square-ad）
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import type { PcImageSquareAdModule } from "./types";
import type { PicListItem } from "../_shared/types";

export type { PcImageSquareAdModule } from "./types";
export { LAYOUT_PIC_COUNT } from "./types";

const demoPicList: PicListItem[] = [
    {
        picId: "1",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picName: "演示图片1",
        picTitle: "魔方图1",
        picLink: { link: "", name: "" }
    },
    {
        picId: "2",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picName: "演示图片2",
        picTitle: "魔方图2",
        picLink: { link: "", name: "" }
    }
];

export const pcImageSquareAdDefaults: PcImageSquareAdModule = {
    picType: 1,
    picList: demoPicList,
    imgPadding: 0,
    frame: {
        textColor: "",
        itemBackgroundColor: "",
        backgroundColor: "#ffffff",
        innerPadding: 0,
        itemHeight: 35,
        itemRadius: 3,
        boxRadius: 0,
        boxPadding: 10,
        leftAndRightPadding: 0,
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
    type: "pc-image-square-ad",
    label: "图片魔方",
    icon: '<i class="iconfont-admin icon-image_square_ad"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 12,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcImageSquareAdDefaults)
});
