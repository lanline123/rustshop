import type { ModuleFrameType, ModuleIcoPicType } from "@/types/decorate/decorate";

/** PC 自定义页 - 公告模块数据 */
export interface PcNoticeModule {
    /** 公告内容 */
    text: string;
    /** 公告图标 */
    icoPic: ModuleIcoPicType;
    frame: ModuleFrameType;
    [key: string]: any;
}
