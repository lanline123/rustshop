import type { ModulePicListType } from "@/types/decorate/decorate";

/** PC 首页 - 自定义模块一数据 */
export interface PcCustomAd1Module {
    /** 左侧大图 */
    picList: ModulePicListType[];
    /** 中间每日推荐 */
    picList2: ModulePicListType[];
    /** 右侧热销精品 */
    picList3: ModulePicListType[];
    [key: string]: any;
}
