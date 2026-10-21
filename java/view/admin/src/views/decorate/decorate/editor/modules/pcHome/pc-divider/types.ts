/**
 * PC 分割线模块 - 类型定义
 */

import type { ModuleFrame } from "../_shared/types";

export interface PcDividerModule {
    // 分割类型：1=空白, 2=分割线
    lineType?: number;
    // 线条样式：1=实线, 2=虚线, 3=点状线
    lineStyle?: number;
    // 容器设置（包含高度、边距等）
    frame?: ModuleFrame;
}
