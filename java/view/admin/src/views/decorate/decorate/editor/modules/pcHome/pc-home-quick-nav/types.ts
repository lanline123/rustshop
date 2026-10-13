/**
 * PC 首页快捷导航模块 - 类型定义
 * 横向图文导航条，对标 nav_container fs_mod 样式
 */

import type { ModuleFrame, PicListItem } from "../_shared/types";

// 导出 PicListItem 供外部使用
export type { PicListItem };

/**
 * 样式配置
 */
export interface PcHomeQuickNavStyle {
    /** 字号（px） */
    fontSize?: number;
    /** 文字颜色 */
    textColor?: string;
    /** hover 文字颜色 */
    hoverColor?: string;
    /** 整体排布：1=居左，2=居中，3=居右（控制 .nav_container.fs_mod 的 justify-content） */
    containerAlign?: 1 | 2 | 3;
    /** 导航项间距（px） */
    itemGap?: number;
    /** 是否单行模式（不换行 + overflow hidden） */
    singleLine?: 0 | 1;
    /** 文字粗细（400 常规 / 700 加粗） */
    fontWeight?: 400 | 700;
    /** 行高（px） */
    lineHeight?: number;
    /** 图标尺寸（px） */
    iconSize?: number;
    /** 图标与文字间距（px） */
    iconGap?: number;
}

/**
 * PC 首页快捷导航模块完整类型
 */
export interface PcHomeQuickNavModule {
    /** 框架配置 */
    frame?: ModuleFrame;
    /** 样式配置 */
    style?: PcHomeQuickNavStyle;
    /** 导航项列表（图文链接） */
    items?: PicListItem[];
}
