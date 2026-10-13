import type { ModuleFrameType } from "@/types/decorate/decorate";

/** 分类中心V1 模块数据（旧 View 为 defineModel<any>，字段来自 defaults + Edit 实际读写） */
export interface CateCenterV1Module {
    searchPlaceholder?: string;
    showCategoryName?: boolean;
    showRecommend?: boolean;
    recommendText?: string;
    /** 左侧一级分类栏背景色 */
    sideBgColor?: string;
    /** 左侧一级分类文字色 */
    sideTextColor?: string;
    /** 左侧一级分类选中文字色 */
    sideActiveTextColor?: string;
    /** 左侧一级分类选中背景色 */
    sideActiveBgColor?: string;
    /** 右侧分组标题色 */
    groupTitleColor?: string;
    /** 右侧分类名称色 */
    categoryNameColor?: string;
    frame?: ModuleFrameType;
    [key: string]: any;
}
