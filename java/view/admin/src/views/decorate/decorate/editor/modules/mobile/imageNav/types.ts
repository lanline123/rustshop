import type { ModuleType, ModuleImageType } from "@/types/decorate/decorate.d";

/** 图文导航模块数据 */
export type ImageNavModule = ModuleType &
    ModuleImageType & {
        [key: string]: any;
    };
