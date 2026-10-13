import type { ModuleType, ModuleSlidingLargeImageType } from "@/types/decorate/decorate.d";

/** 大图滑动模块数据（沿用旧 View/Edit 的共享类型组合） */
export type SlideLargeImageModule = ModuleType &
    ModuleSlidingLargeImageType & {
        [key: string]: any;
    };
