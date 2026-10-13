import type { ModulePicListType } from "@/types/decorate/decorate";

/** PC 首页 - 分组轮播 Banner 模块数据（海外 PC 风格专属） */
export interface PcGroupBannerModule {
    bannerStyle: number;
    showCat: number;
    logo: { picUrl: string; picThumb: string };
    /** 分组 Banner 轮播大图 */
    picList: ModulePicListType[];
    picList2: ModulePicListType[];
    picList3: ModulePicListType[];
    /** 一组显示的数量（最多 3） */
    number?: number;
    [key: string]: any;
}
