import type { ModuleType } from "@/types/decorate/decorate.d";

/** 商城公告模块数据（旧 View/Edit 即为 ModuleType & Record<string, any>，无专属类型） */
export type NoticeA3Module = ModuleType & {
    [key: string]: any;
};
