import type { ModuleType, ModuleCategoryA1Type } from "@/types/decorate/decorate";

/** 类目导航A1 模块数据（旧 View/Edit 以 ModuleType & ModuleCategoryA1Type 交叉类型读写） */
export type CategoryNavA1Module = ModuleType &
    ModuleCategoryA1Type & {
        [key: string]: any;
    };
