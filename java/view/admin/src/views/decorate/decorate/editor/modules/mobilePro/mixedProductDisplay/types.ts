import type { ModuleType, ModuleMixedProductDisplayType } from "@/types/decorate/decorate.d";

/** 商品混排模块数据（沿用旧 View/Edit 的共享类型组合） */
export type MixedProductDisplayModule = ModuleType &
    ModuleMixedProductDisplayType & {
        [key: string]: any;
    };
