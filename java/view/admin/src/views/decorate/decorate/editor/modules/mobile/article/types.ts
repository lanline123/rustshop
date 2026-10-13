import type { ModuleType, ModuleArticleType } from "@/types/decorate/decorate.d";

/** 文章列表模块数据 */
export type ArticleModule = ModuleType &
    ModuleArticleType & {
        [key: string]: any;
    };
