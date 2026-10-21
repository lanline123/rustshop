import type { ModuleType, ModuleMixedNoticeDisplayType } from "@/types/decorate/decorate.d";

/** 公告混排模块数据（沿用旧 View/Edit 的共享类型组合） */
export type MixedNoticeModule = ModuleType &
    ModuleMixedNoticeDisplayType & {
        [key: string]: any;
    };
