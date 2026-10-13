/**
 * PC 图片广告模块 - 类型定义
 */

import type { ModuleFrame, ModuleTitle, PicListItem } from "../_shared/types";

export interface PcImageAdModule {
    // 布局类型：1=一行一个, 2=轮播, 5=一行两个, 6=一行三个, 7=一行四个（PC精简版）
    picType?: number;
    // 图片列表
    picList?: PicListItem[];
    // 轮播一行显示数量
    swiperPreView?: number;
    // 指示器颜色
    swiperPageColor?: string;
    // 图片边距
    imgPadding?: number;
    // 指示器样式：1=样式一, 2=样式二, 3=样式三
    picPageType?: number;
    // 图片边角：1=直角, 2=圆角
    picRadioStyle?: number;
    // 是否通宽（PC端）：0=否, 1=是
    isFluxWidth?: number;
    // 边框/容器设置
    frame?: ModuleFrame;
    // 标题设置
    title?: ModuleTitle;
}
