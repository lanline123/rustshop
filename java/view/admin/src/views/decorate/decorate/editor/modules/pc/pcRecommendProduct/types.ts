import type { ModuleProductsType } from "@/types/decorate/decorate";

/** PC 首页 - 推荐商品（瀑布流）模块数据 */
export interface PcRecommendProductModule {
    /** 标题名称 */
    titleName?: string;
    /** 商品选取配置 */
    products: ModuleProductsType;
    [key: string]: any;
}
