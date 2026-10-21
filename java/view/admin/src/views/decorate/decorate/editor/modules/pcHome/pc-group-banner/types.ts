/**
 * PC 分组轮播 Banner 模块 - 类型定义
 */

import type { PicListItem } from "../_shared/types";

/**
 * 模块框架配置
 */
export interface PcGroupBannerFrame {
    /** 背景颜色 */
    backgroundColor?: string;
    /** 左右内边距 */
    boxPadding?: number;
    /** 上内边距 */
    boxPaddingTop?: number;
    /** 下内边距 */
    boxPaddingBottom?: number;
    /** 容器圆角 */
    boxRadius?: number;
    /** 轮播高度 */
    slideHeight?: number;
}

export interface PcGroupBannerModule {
    /** 框架配置 */
    frame?: PcGroupBannerFrame;
    /** Banner 样式 */
    bannerStyle?: number;
    /** 是否展示分类 */
    showCat?: number;
    /** Logo */
    logo?: {
        picUrl?: string;
        picThumb?: string;
    };
    /** Banner 图片列表（大图） */
    picList?: PicListItem[];
    /** 保留扩展图片列表 */
    picList2?: PicListItem[];
    picList3?: PicListItem[];
    /** 一组显示数量（1~3） */
    number?: number;
}
