import type { ModuleType, ModuleCouponType } from "@/types/decorate/decorate.d";

/** 优惠券模块数据 */
export type CouponModule = ModuleType &
    ModuleCouponType & {
        [key: string]: any;
    };
