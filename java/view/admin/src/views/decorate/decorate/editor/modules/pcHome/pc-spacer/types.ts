/**
 * PC 空白辅助模块 - 类型定义
 */

import type { ModuleFrame } from "../_shared/types";

export interface PcSpacerModule {
    /** 框架配置（外边距等） */
    frame?: ModuleFrame;
    // 背景颜色
    backgroundColor?: string;
    // 空白高度（单位：px）
    blankHeight?: number;
}
