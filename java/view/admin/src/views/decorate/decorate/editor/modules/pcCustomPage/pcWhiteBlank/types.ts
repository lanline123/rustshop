import type { ModuleFrameType } from "@/types/decorate/decorate";

/** PC 自定义页 - 空白辅助模块数据（默认值为空对象，字段均为 Edit 写入后才存在） */
export interface PcWhiteBlankModule {
    /** 背景颜色 */
    backgroundColor?: string;
    /** 空白高度（px），未设置时展示侧兜底 30px */
    blankHeight?: number;
    frame?: ModuleFrameType;
    [key: string]: any;
}
