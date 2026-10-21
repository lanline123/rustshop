import type { ModuleType } from "@/types/decorate/decorate.d";

/** 点单卡片单项 */
export interface StoreOrderCardItem {
    picId?: number | string;
    picUrl?: string;
    picThumb?: string;
    picName?: string;
    title?: string;
    desc?: string;
    link?: Record<string, any>;
}

/** 点单卡片模块数据（与旧 View/Edit 字段契约一致） */
export type StoreOrderCardModule = ModuleType & {
    picType?: number;
    cardStyle?: number;
    boxStyle?: number;
    backgroundColor?: string;
    cardBackground?: string;
    cardGap?: number;
    frame?: Record<string, any>;
    cardList?: StoreOrderCardItem[];
    [key: string]: any;
};
