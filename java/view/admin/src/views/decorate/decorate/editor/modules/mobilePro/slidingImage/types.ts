import type { ModuleType, ModuleSlidingImageType } from "@/types/decorate/decorate.d";

/** 小图滑动模块数据（沿用旧 View/Edit 的共享类型组合） */
export type SlidingImageModule = ModuleType &
    ModuleSlidingImageType & {
        [key: string]: any;
    };
