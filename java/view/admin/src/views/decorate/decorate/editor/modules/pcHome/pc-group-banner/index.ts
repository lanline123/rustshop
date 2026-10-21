/**
 * PC 分组轮播 Banner 模块（新套可视化，对齐 Vort pc-group-banner，跨境版独有）
 * 跨境裁剪：enabled 谓词 isOverseas() 控制工具栏显示（不切割时同样生效）；
 * 整目录登记于 .vort/feature-manifest.json 的 overseas；
 * 注册导入处见 modules/index.ts 的 @vort-if overseas。
 */

import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { isOverseas } from "@/utils/version";
import type { PcGroupBannerModule } from "./types";
import type { PicListItem } from "../_shared/types";

export type { PcGroupBannerModule } from "./types";

const demoPicList: PicListItem[] = [
    {
        picId: "1",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301937REbUnsSmPehWiBMaFd.webp",
        picName: "演示图片1",
        picTitle: "Banner1",
        picLink: { link: "", name: "" }
    },
    {
        picId: "2",
        picUrl: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picThumb: "https://oss.vortmall.com/upload/c4ca4238/img/gallery/202411/1731301941uaSjIShOBNL2VBSr0y.webp",
        picName: "演示图片2",
        picTitle: "Banner2",
        picLink: { link: "", name: "" }
    }
];

export const pcGroupBannerDefaults: PcGroupBannerModule = {
    frame: {
        backgroundColor: "#ffffff",
        boxPadding: 10,
        boxPaddingTop: 0,
        boxPaddingBottom: 0,
        boxRadius: 8,
        slideHeight: 195
    },
    bannerStyle: 1,
    showCat: 1,
    logo: {
        picUrl: "",
        picThumb: ""
    },
    picList: demoPicList,
    picList2: [],
    picList3: [],
    number: 3
};

registerModule({
    type: "pc-group-banner",
    label: "分组轮播Banner",
    icon: '<i class="iconfont-admin icon-banner"></i>',
    url: "pc",
    platform: "pc-home",
    toolbarGroup: "pcNew",
    order: 2,
    enabled: () => isOverseas(),
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(pcGroupBannerDefaults)
});
