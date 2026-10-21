import type { ModuleType, ModuleCategoryType } from "@/types/decorate/decorate";

/** 类目导航模块数据（旧 View/Edit 以 ModuleType & ModuleCategoryType 交叉类型读写） */
export type CategoryNavModule = ModuleType &
    ModuleCategoryType & {
        [key: string]: any;
    };
