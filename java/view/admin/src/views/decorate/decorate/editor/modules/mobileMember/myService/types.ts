import type { ModuleType, ModuleFrameType, ModulePicListType } from "@/types/decorate/decorate";

/** 我的服务模块数据 */
export type MyServiceModule = Omit<ModuleType, "frame"> & {
    moduleTitle?: string;
    /** 布局样式：1=列表版，2=九宫格 */
    layoutStyle?: number;
    iconRadius?: number;
    serviceList: ModulePicListType[];
    frame: ModuleFrameType;
    [key: string]: any;
};
