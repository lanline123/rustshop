import type { ModuleCountdownType } from "@/types/decorate/decorate";

/** 倒计时模块数据 */
export interface CountdownModule extends ModuleCountdownType {
    [key: string]: any;
}
