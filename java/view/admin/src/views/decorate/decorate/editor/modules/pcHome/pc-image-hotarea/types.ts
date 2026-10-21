/**
 * PC 热图绘制模块 - 类型定义
 */

import type { ModuleFrame, ModuleTitle } from "../_shared/types";

/** 热区链接信息 */
export interface HotareaLink {
    link?: string;
    name?: string;
}

/** 热区信息 */
export interface HotareaItem {
    // 热区宽度（相对于500px绘制区域）
    width?: number;
    // 热区高度
    height?: number;
    // 热区顶部位置
    top?: number;
    // 热区左侧位置
    left?: number;
    // 热区链接
    link?: HotareaLink;
    // 计算后的位置样式（运行时使用）
    position?: string;
}

/** 带热区的图片项 */
export interface HotareaPicItem {
    picId?: number;
    picUrl?: string;
    picThumb?: string;
    picName?: string;
    picTitle?: string;
    // 热区列表
    hotarea?: HotareaItem[];
}

/** PC 热图绘制模块数据 */
export interface PcImageHotareaModule {
    // 图片列表（带热区）
    picList?: HotareaPicItem[];
    // 图片边距
    imgPadding?: number;
    // 边框/容器设置
    frame?: ModuleFrame;
    // 标题设置
    title?: ModuleTitle;
}
