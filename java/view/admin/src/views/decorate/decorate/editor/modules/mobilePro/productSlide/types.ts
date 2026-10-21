import type { ModuleType, ProductSlideType } from "@/types/decorate/decorate.d";

/** 商品滑动模块数据（沿用旧 View/Edit 的共享类型组合） */
export type ProductSlideModule = ModuleType &
    ProductSlideType & {
        [key: string]: any;
    };
