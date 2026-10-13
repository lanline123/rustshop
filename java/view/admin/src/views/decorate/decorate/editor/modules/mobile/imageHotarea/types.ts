import type { ModuleFrameType, ModuleTitleType, ModuleImageHotareaPicListType } from "@/types/decorate/decorate";

/** 热图绘制模块数据 */
export interface ImageHotareaModule {
    picList: ModuleImageHotareaPicListType[];
    /** 图片边距 */
    imgPadding: number;
    /** 图片边角：1=直角，2=圆角 */
    picRadioStyle: number;
    frame: ModuleFrameType;
    title: ModuleTitleType;
    [key: string]: any;
}
