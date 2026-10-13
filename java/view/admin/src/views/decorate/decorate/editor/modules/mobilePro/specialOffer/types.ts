import type { ModuleType, ModuleSpecialOfferType } from "@/types/decorate/decorate.d";

/** 天天特价模块数据（沿用旧 View/Edit 的共享类型组合） */
export type SpecialOfferModule = ModuleType &
    ModuleSpecialOfferType & {
        [key: string]: any;
    };
