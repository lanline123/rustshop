import type { ModuleType, ModuleProductGroupingType } from "@/types/decorate/decorate.d";

/** 商品分组模块数据（沿用旧 View/Edit 的共享类型组合） */
export type ProductGroupingModule = ModuleType &
    ModuleProductGroupingType & {
        [key: string]: any;
    };
