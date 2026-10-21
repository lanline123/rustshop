import type { ModuleType, ModulestoreNavType } from "@/types/decorate/decorate.d";

/** 门店样式 */
export const StoreNavStyle = {
    /** 样式一 */
    One: 1,
    /** 样式二 */
    Two: 2
} as const;

/** 展示门店来源 */
export const StoreNavSource = {
    /** 自动获取 */
    Auto: 1,
    /** 指定店铺 */
    Assign: 2
} as const;

/** 指定店铺最多可选数量 */
export const STORE_NAV_MAX_STORE = 20;

/** 自动获取时可配置的门店数量范围 */
export const STORE_NAV_MIN_AUTO_NUM = 1;
export const STORE_NAV_MAX_AUTO_NUM = 20;

/** 指定店铺时保存的门店条目 */
export interface StoreNavShopItem {
    shopId: number;
    shopTitle: string;
    /** 门店 LOGO */
    shopLogo?: string;
}

/** 门店导航模块数据（沿用旧 View/Edit 的共享类型组合） */
export type StoreNavModule = ModuleType &
    ModulestoreNavType & {
        /** 门店样式 1:样式一 2:样式二 */
        storeStyle?: number;
        /** 展示门店 1:自动获取 2:指定店铺 */
        storeSource?: number;
        /** 自动获取时展示的门店数量 */
        storeNum?: number;
        /** 指定店铺列表，按顺序展示 */
        storeList?: StoreNavShopItem[];
        [key: string]: any;
    };
