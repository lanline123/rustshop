import type { ModuleComboSuggestionsEditType } from "@/types/decorate/decorate";

/** 组合推荐模块数据 */
export interface ComboSuggestionsModule extends ModuleComboSuggestionsEditType {
    [key: string]: any;
}
