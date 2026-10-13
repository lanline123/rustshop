import type { ModuleType, ModuleFrameType } from "@/types/decorate/decorate";

/** 用户信息2 模块数据 */
export type UserInfoV2Module = Omit<ModuleType, "frame"> & {
    frame: ModuleFrameType;
    [key: string]: any;
};
