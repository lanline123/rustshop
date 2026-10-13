import type { ModuleType, ModuleActivityType } from "@/types/decorate/decorate.d";

/** 商品模块数据 */
export type ProductModule = ModuleType &
    ModuleActivityType & {
        [key: string]: any;
    };
