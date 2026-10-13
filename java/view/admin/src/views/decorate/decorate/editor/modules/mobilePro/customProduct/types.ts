import type { ModuleCustomProductType } from "@/types/decorate/decorate";

/** 个性商品模块数据 */
export interface CustomProductModule extends ModuleCustomProductType {
    [key: string]: any;
}
