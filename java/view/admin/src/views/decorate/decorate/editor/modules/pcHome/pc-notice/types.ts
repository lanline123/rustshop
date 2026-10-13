/**
 * PC 公告模块 - 类型定义
 */

import type { ModuleFrame, PicItem } from "../_shared/types";

export interface PcNoticeModule {
    // 公告内容
    text?: string;
    // 公告图标
    icoPic?: PicItem;
    // 容器/样式配置
    frame?: ModuleFrame;
}
