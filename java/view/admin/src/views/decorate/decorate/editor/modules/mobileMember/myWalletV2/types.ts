import type { ModuleType, ModuleIcoPicType, ModuleFrameType } from "@/types/decorate/decorate";

export interface WalletV2Item {
    key: string;
    label: string;
    icon: ModuleIcoPicType;
}

/** 我的钱包2 模块数据（双列宫格） */
export type MyWalletV2Module = Omit<ModuleType, "frame"> & {
    items: WalletV2Item[];
    frame: ModuleFrameType;
    [key: string]: any;
};
