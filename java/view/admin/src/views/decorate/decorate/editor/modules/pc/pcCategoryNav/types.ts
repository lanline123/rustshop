import type { ModulePicListType, ModuleStyle, ModuleContentStyle } from "@/types/decorate/decorate";

/** PC 首页 - 分类导航模块数据（默认值沿用旧 View.vue 的 Banner 字段，契约保留） */
export interface PcCategoryNavModule {
    bannerStyle: number;
    showCat: number;
    logo: { picUrl: string; picThumb: string };
    picList: ModulePicListType[];
    picList2: ModulePicListType[];
    picList3: ModulePicListType[];
    /** 每屏显示数量 */
    rowNum?: number;
    moduleStyle?: ModuleStyle;
    contentStyle?: ModuleContentStyle;
    [key: string]: any;
}
