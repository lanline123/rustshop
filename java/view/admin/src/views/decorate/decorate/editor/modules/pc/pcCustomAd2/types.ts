import type { ModulePicListType, ModuleLinkListType } from "@/types/decorate/decorate";

/** PC 首页 - 自定义模块二数据 */
export interface PcCustomAd2Module {
    /** 左侧链接（超过 6 个不显示） */
    linkList: ModuleLinkListType[];
    /** 左侧大图（单图模式） */
    picList: ModulePicListType[];
    /** 中间广告图 */
    picList2: ModulePicListType[];
    /** 右侧广告图 */
    picList3: ModulePicListType[];
    [key: string]: any;
}
