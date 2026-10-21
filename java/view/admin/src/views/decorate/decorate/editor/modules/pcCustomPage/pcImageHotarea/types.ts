import type { ModuleFrameType, ModuleTitleType, ModuleImageHotareaPicListType } from "@/types/decorate/decorate";

/** PC 自定义页 - 热图绘制模块数据 */
export interface PcImageHotareaModule {
    /** 图片列表（每张图可携带 hotarea 热区） */
    picList: ModuleImageHotareaPicListType[];
    /** 图片边距 */
    imgPadding: number;
    frame: ModuleFrameType;
    title: ModuleTitleType;
    [key: string]: any;
}
