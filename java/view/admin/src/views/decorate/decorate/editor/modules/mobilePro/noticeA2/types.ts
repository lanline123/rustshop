import type { ModuleType, ModuleNoticeA2Type } from "@/types/decorate/decorate.d";

/** 公告模块数据（沿用旧 View/Edit 的共享类型组合） */
export type NoticeA2Module = ModuleType &
    ModuleNoticeA2Type & {
        [key: string]: any;
    };
