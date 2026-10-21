import type { ModuleType, ModuleActivityType } from "@/types/decorate/decorate.d";

/** 商品2模块数据 */
export type Product2Module = ModuleType &
    ModuleActivityType & {
        [key: string]: any;
    };
