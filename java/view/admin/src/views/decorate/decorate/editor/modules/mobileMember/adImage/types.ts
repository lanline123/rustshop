import type { ModuleType, ModuleIcoPicType, ModuleFrameType } from "@/types/decorate/decorate";

export interface AdImageLink {
    link?: string;
    name?: string;
    label?: string;
    data?: Record<string, any>;
}

/** 广告图模块数据 */
export type AdImageModule = Omit<ModuleType, "frame"> & {
    pic: ModuleIcoPicType;
    picLink: AdImageLink;
    frame: ModuleFrameType;
    [key: string]: any;
};
