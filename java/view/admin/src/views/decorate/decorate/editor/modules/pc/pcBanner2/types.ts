import type { ModulePicListType } from "@/types/decorate/decorate";

/** PC 首页 - 轮播 Banner2 模块数据（海外 PC 风格专属） */
export interface PcBanner2Module {
    bannerStyle: number;
    showCat: number;
    logo: { picUrl: string; picThumb: string };
    /** Banner 轮播图 */
    picList: ModulePicListType[];
    picList2: ModulePicListType[];
    picList3: ModulePicListType[];
    [key: string]: any;
}
