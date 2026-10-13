import type { ModulePicListType } from "@/types/decorate/decorate";

/** PC 首页 - 分类商品（窄）模块数据 */
export interface PcCatProductNormalModule {
    categoryId: number;
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
    /** 右侧四小图 */
    picList2: ModulePicListType[];
    [key: string]: any;
}
