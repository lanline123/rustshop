import type { ModuleType, ModuleIcoPicType, ModuleFrameType } from "@/types/decorate/decorate";

export interface UserInfoShortcut {
    show: boolean;
    icon: ModuleIcoPicType;
    title?: string;
}
export interface UserInfoGradient {
    colorA: string;
    colorB: string;
    gradientType?: string;
}
export interface UserInfoStatsItem {
    key: string;
    label: string;
    show: boolean;
}
export interface UserInfoMemberBar {
    show: boolean;
    backgroundColor: string;
    backgroundColorB?: string;
    borderRadius: number;
    levelName?: string;
    description?: string;
    buttonText?: string;
    buttonLink?: Record<string, any>;
    waveImage?: ModuleIcoPicType;
}
export interface UserInfoBottomArc {
    show: boolean;
    height: number;
}
export interface UserInfoTextColors {
    nameColor: string;
    subColor: string;
    statValueColor: string;
    statLabelColor: string;
}

/** 用户信息3 模块数据（四种布局可切换） */
export type UserInfo3Module = Omit<ModuleType, "frame"> & {
    layoutStyle: number;
    signIn: UserInfoShortcut;
    settings: UserInfoShortcut;
    gradient: UserInfoGradient;
    bgColor: string;
    backgroundImage: ModuleIcoPicType;
    bottomArc: UserInfoBottomArc;
    memberBar: UserInfoMemberBar;
    notification: UserInfoShortcut;
    stats: { show: boolean; items: UserInfoStatsItem[] };
    textColors: UserInfoTextColors;
    frame: ModuleFrameType;
    showEditIcon?: boolean;
    /** 历史字段：旧数据用 showSignIn/showSettings 开关，渲染时迁移为 signIn/settings */
    showSignIn?: boolean;
    showSettings?: boolean;
    [key: string]: any;
};
