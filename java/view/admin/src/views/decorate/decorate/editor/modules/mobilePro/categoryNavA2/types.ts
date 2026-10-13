import type { ModuleType, ModuleCategoryA2Type } from "@/types/decorate/decorate";

/** 类目导航A2 模块数据（旧 View/Edit 以 ModuleType & ModuleCategoryA2Type 交叉类型读写） */
export type CategoryNavA2Module = ModuleType &
    ModuleCategoryA2Type & {
        [key: string]: any;
    };
