import type { ModuleType, ModuleCatNavType } from "@/types/decorate/decorate.d";

/** 分类导航模块数据 */
export type CatNavModule = ModuleType &
    ModuleCatNavType & {
        [key: string]: any;
    };
