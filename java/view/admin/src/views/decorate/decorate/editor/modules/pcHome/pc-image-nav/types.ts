/**
 * PC 图文导航模块 - 类型定义
 */

import type { ModuleFrame, ModuleTitle, PicListItem } from "../_shared/types";

export interface PcImageNavModule {
    // 显示模式：1=固定显示, 2=滑动分页
    navType?: number;
    // 样式：1=图片文字, 2=仅图片, 3=仅文字
    navStyle?: number;
    // 一行显示数量：3-6
    rowNum?: number;
    // 显示行数：1-3
    colNum?: number;
    // 图片列表
    picList?: PicListItem[];
    backgroundColor?: string;
    // 元素边距
    imgPadding?: number;
    // 图片边角：1=直角, 2=圆角
    radioStyle?: number;
    // 指示器样式：1=样式一, 2=样式二
    picPageType?: number;
    // 指示器颜色
    swiperPageColor?: string;
    // 容器设置
    frame?: ModuleFrame;
    // 标题
    title?: ModuleTitle;
}
