import type { ModuleType } from "@/types/decorate/decorate.d";

/** 商品提示模块数据（旧 View/Edit 即为 ModuleType & Record<string, any>，无专属类型） */
export type ShopTipsModule = ModuleType & {
    [key: string]: any;
};
