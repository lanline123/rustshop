/**
 * PC 优惠券模块 - 共享类型定义
 */

export interface PcCouponModule {
    // 是否显示标题栏：0=不显示, 1=显示
    showTitle?: number;
    // 标题内容
    title?: string;
    // 描述内容
    desc?: string;
    // 颜色风格
    colorStyle?: number;
    // 背景颜色
    backgroundColor?: string;
    // 组件背景
    itemBackgroundColor?: string;
    // 组件边距
    itemPadding?: number;
    // 圆角
    boxRadius?: number;
    // 左右边距
    boxPadding?: number;
    // 上边距
    boxPaddingTop?: number;
    // 下边距
    boxPaddingBottom?: number;
}
