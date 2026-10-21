import type { ModuleType, ShopInfo } from "@/types/decorate/decorate.d";

/** 店铺信息模块数据 */
export type ShopInfoModule = ModuleType &
    ShopInfo & {
        [key: string]: any;
    };
