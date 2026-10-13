import type { ModuleType, ModuleActivityType } from "@/types/decorate/decorate.d";

/** 秒杀2模块数据 */
export type Seckill2Module = ModuleType &
    ModuleActivityType & {
        [key: string]: any;
    };
