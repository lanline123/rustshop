/**
 * PC 图片魔方模块 - 类型定义
 */

import type { ModuleFrame, ModuleTitle, PicListItem } from "../_shared/types";

export interface PcImageSquareAdModule {
    // 布局类型：1=一行两个, 2=一行三个, 3=一行四个, 4=左二右二, 5=左一右二, 6=左一右三, 7=上一下二
    picType?: number;
    // 图片列表
    picList?: PicListItem[];
    // 图片边距
    imgPadding?: number;
    // 边框/容器设置
    frame?: ModuleFrame;
    // 标题设置
    title?: ModuleTitle;
}

/**
 * 布局类型对应的图片数量映射
 */
export const LAYOUT_PIC_COUNT: Record<number, number> = {
    1: 2,
    2: 3,
    3: 4,
    4: 4,
    5: 3,
    6: 4,
    7: 3
};
