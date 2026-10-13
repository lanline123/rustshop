import type { ModuleFrameType } from "@/types/decorate/decorate";

/** 分割线模块数据 */
export interface WhiteLineModule {
    /** 分割类型：1=空白，2=分割线 */
    lineType: number;
    /** 线条样式：1=实线，2=虚线，3=点状线 */
    lineStyle: number;
    frame: ModuleFrameType;
    [key: string]: any;
}
