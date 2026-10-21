import type { ModulePicListType } from "@/types/decorate/decorate";

/** PC 首页 - 分类导航3 模块数据（默认值沿用旧 View.vue 的 Banner 字段，契约保留） */
export interface PcCategoryNav3Module {
    bannerStyle: number;
    showCat: number;
    logo: { picUrl: string; picThumb: string };
    /** 导航图组 */
    picList: ModulePicListType[];
    picList2: ModulePicListType[];
    picList3: ModulePicListType[];
    /** 图片背景色 */
    imageBackgroundColor?: string;
    /** 标题内容 */
    titleText?: string;
    /** 是否显示查看更多 */
    showMore?: number;
    /** 查看更多文字 */
    showMoreText?: string;
    /** 更多链接 */
    moreLink?: any;
    [key: string]: any;
}
