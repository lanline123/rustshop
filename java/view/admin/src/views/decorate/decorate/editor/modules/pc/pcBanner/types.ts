import type { ModulePicListType } from "@/types/decorate/decorate";

/** PC 首页 - 轮播 Banner 模块数据 */
export interface PcBannerModule {
    /** Banner 布局：1=默认风格，2=简约风格 */
    bannerStyle: number;
    /** 简约风格下是否显示分类栏 */
    showCat: number;
    logo: { picUrl: string; picThumb: string };
    /** Banner 轮播大图 */
    picList: ModulePicListType[];
    /** 右侧轮播小图（默认风格） */
    picList2: ModulePicListType[];
    /** 右侧会员栏图标（默认风格） */
    picList3: ModulePicListType[];
    [key: string]: any;
}
