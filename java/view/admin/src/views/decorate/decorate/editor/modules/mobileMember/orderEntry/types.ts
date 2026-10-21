import type { ModuleType, ModuleFrameType, ModulePicListType } from "@/types/decorate/decorate";

/** 我的订单模块数据 */
export type OrderEntryModule = Omit<ModuleType, "frame"> & {
    title?: string;
    /** 布局样式：1=带标题头，2=卡片分割 */
    layoutStyle?: number;
    entryList: ModulePicListType[];
    /** 角标背景色 */
    badgeBgColor?: string;
    /** 角标文字色 */
    badgeTextColor?: string;
    frame: ModuleFrameType;
    [key: string]: any;
};
