import type { ModuleType, ModuleIcoPicType, ModuleFrameType } from "@/types/decorate/decorate";

export interface UserInfoShortcut {
    show: boolean;
    icon: ModuleIcoPicType;
    title?: string;
}
export interface UserInfoGradient {
    colorA: string;
    colorB: string;
}

/** 用户信息模块数据 */
export type UserInfoModule = Omit<ModuleType, "frame"> & {
    signIn: UserInfoShortcut;
    settings: UserInfoShortcut;
    gradient: UserInfoGradient;
    frame: ModuleFrameType;
    /** 历史字段：旧数据用 showSignIn/showSettings 开关，渲染时迁移为 signIn/settings */
    showSignIn?: boolean;
    showSettings?: boolean;
    [key: string]: any;
};
