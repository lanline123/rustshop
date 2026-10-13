import type { ModuleType, NewcomerComboType } from "@/types/decorate/decorate.d";

/** 新人二重礼模块数据（沿用旧 View/Edit 的共享类型组合） */
export type NewcomerComboModule = ModuleType &
    NewcomerComboType & {
        [key: string]: any;
    };
