import type { ModuleFrameType, ModuleTitleType } from "@/types/decorate/decorate";

/** PC 自定义页 - 文本标题模块数据 */
export interface PcTitleAdModule {
    frame: ModuleFrameType;
    title: ModuleTitleType;
    [key: string]: any;
}
