import type { ModuleType, ModuleIcoPicType, ModuleFrameType } from "@/types/decorate/decorate";

/** 我的钱包模块数据 */
export type MyWalletModule = Omit<ModuleType, "frame"> & {
    /** 布局样式：1=标题+三列数据，2=图标+竖分割线 */
    layoutStyle?: number;
    walletIcon?: ModuleIcoPicType;
    frame: ModuleFrameType;
    [key: string]: any;
};
