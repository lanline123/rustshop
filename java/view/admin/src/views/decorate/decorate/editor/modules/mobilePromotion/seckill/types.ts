import type { ModuleType, ModuleActivityType } from "@/types/decorate/decorate.d";

/** 秒杀模块数据 */
export type SeckillModule = ModuleType &
    ModuleActivityType & {
        [key: string]: any;
    };
