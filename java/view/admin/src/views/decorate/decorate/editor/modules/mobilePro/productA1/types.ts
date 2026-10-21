import type { ModuleType, ModuleActivityType, ProductA1Type } from "@/types/decorate/decorate.d";

// 排除 ModuleActivityType.buyBtnStyle（number），改由 ProductA1Type.buyBtnStyle（BuyButtonStyleType 对象）承载，避免类型交叉成 never
/** 商品A1模块数据（沿用旧 View/Edit 的共享类型组合） */
export type ProductA1Module = ModuleType &
    Omit<ModuleActivityType, "buyBtnStyle"> &
    ProductA1Type & {
        [key: string]: any;
    };
