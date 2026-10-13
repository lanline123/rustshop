import type { ModuleType, ModuleFrameType } from "@/types/decorate/decorate";

/** 固定功能区模块数据（账期支付/配送订单等端内必备入口） */
export type FixedFunctionalAreaModule = Omit<ModuleType, "frame"> & {
    frame: ModuleFrameType;
    [key: string]: any;
};
