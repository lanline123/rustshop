/**
 * PC 分类导航模块 - 类型定义
 */

/**
 * 模块框架配置（含 boxHeight，为本模块特有，不复用 _shared ModuleFrame）
 */
export interface PcCategoryNavFrame {
    /** 左右内边距 */
    boxPadding?: number;
    /** 上内边距 */
    boxPaddingTop?: number;
    /** 下内边距 */
    boxPaddingBottom?: number;
    /** 容器圆角 */
    boxRadius?: number;
    /** 背景颜色 */
    backgroundColor?: string;
    /** 容器高度 */
    boxHeight?: number;
}

export interface PcCategoryNavModule {
    /** 框架配置 */
    frame?: PcCategoryNavFrame;
    /** 标题文本 */
    titleName?: string;
    /** 是否显示标题：1-显示，0-不显示 */
    showTitle?: 0 | 1;
    /** 一屏显示数量（slidesPerView） */
    displayNum?: number;
    backgroundColor?: string;
}
