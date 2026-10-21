import type { ModuleType, ModuleImageType } from "@/types/decorate/decorate.d";

/** 文本标题模块数据 */
export type TitleAdModule = ModuleType &
    ModuleImageType & {
        [key: string]: any;
    };
