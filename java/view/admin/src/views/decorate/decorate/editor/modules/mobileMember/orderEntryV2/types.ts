import type { ModuleType, ModuleFrameType, ModulePicListType } from "@/types/decorate/decorate";

/** 我的订单2 模块数据（卡片+列表样式） */
export type OrderEntryV2Module = Omit<ModuleType, "frame"> & {
    title?: string;
    cardList: ModulePicListType[];
    listItems: ModulePicListType[];
    cardBackgroundColor?: string;
    /** 角标背景色 */
    badgeBgColor?: string;
    /** 角标文字色 */
    badgeTextColor?: string;
    frame: ModuleFrameType;
    [key: string]: any;
};
