import type { ModuleType, ModuleFrameType, ModulePicListType } from "@/types/decorate/decorate";

/** 账号模块数据 */
export type AccountModule = Omit<ModuleType, "frame"> & {
    moduleTitle?: string;
    accountList: ModulePicListType[];
    frame: ModuleFrameType;
    [key: string]: any;
};
