import type { ModuleType, ModuleActivityType } from "@/types/decorate/decorate.d";

/** 拼团模块数据 */
export type GrouponModule = ModuleType &
    ModuleActivityType & {
        [key: string]: any;
    };
