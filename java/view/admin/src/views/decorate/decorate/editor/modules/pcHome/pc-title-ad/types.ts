/**
 * PC 文本标题模块 - 类型定义
 */

import type { ModuleFrame, ModuleTitle } from "../_shared/types";

export interface PcTitleAdModule {
    // 标题配置
    title?: ModuleTitle;
    // 容器配置
    frame?: ModuleFrame;
}
