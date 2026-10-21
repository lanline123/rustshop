import type { ModulePicListType, ModuleLinkListType } from "@/types/decorate/decorate";

/** PC 首页 - 分类商品（宽）模块数据 */
export interface PcCatProductWideModule {
    /** 选择分类（多选模式下为数组） */
    categoryId: number | number[];
    /** 标题名称 */
    title?: string;
    /** 标题短名称 */
    shortTitle: string;
    /** 标题英文简称 */
    enTitle: string;
    /** 模块主颜色 */
    color: string;
    /** 模块副颜色（渐变） */
    color2: string;
    logo: { picUrl: string; picThumb: string };
    /** 左侧轮播图 */
    picList: ModulePicListType[];
    /** 中间 6 广告图 */
    picList2: ModulePicListType[];
    /** 右侧热门商品 */
    productIds: number[];
    /** 左侧轮播下方链接 */
    linkList: ModuleLinkListType[];
    [key: string]: any;
}
