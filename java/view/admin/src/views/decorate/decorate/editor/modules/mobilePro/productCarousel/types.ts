import type { ModuleType, ProductCarouselType } from "@/types/decorate/decorate.d";

/** 商品轮播模块数据（沿用旧 View/Edit 的共享类型组合） */
export type ProductCarouselModule = ModuleType &
    ProductCarouselType & {
        [key: string]: any;
    };
