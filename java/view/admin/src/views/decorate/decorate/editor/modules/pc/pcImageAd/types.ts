import type { ModulePicListType } from "@/types/decorate/decorate";

/** PC 首页 - 通宽广告图模块数据 */
export interface PcImageAdModule {
    picList: ModulePicListType[];
    /** 上边距 */
    boxPaddingTop: number;
    /** 下边距 */
    boxPaddingBottom: number;
    /** 模板类型（Settings 提示文案按其切换） */
    picType?: number;
    [key: string]: any;
}
