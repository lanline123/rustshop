import type { ModulePicListType, ModuleProductsType } from "@/types/decorate/decorate";

/** PC 首页 - 分类商品（简约）模块数据 */
export interface PcCatProductSimpleModule {
    /** 风格：1=风格一，2=风格二 */
    baseStyle: number;
    /** 标题名称 */
    title: string;
    /** 商品选取配置 */
    products: ModuleProductsType;
    /** 更多链接 */
    moreLink: any;
    /** 左侧大图（风格二） */
    picList: ModulePicListType[];
    [key: string]: any;
}
