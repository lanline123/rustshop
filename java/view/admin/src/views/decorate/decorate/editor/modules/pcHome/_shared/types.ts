/**
 * PC 首页可视化模块（新套）公共类型
 *
 * 与 Vort-Admin types/modules/decorate 同构（字段 1:1），保证：
 * - 新模块默认值与 Vort 逐字段一致
 * - 将来 Tigshop-Pc 前台直接复用 vortmall-pc 渲染组件时数据契约无缝对接
 * 不复用旧 types/decorate/decorate.d.ts（旧契约语义，新旧两套隔离）
 */

// 图片类型
export interface PicItem {
    picId?: string;
    picUrl?: string;
    picThumb?: string;
    picName?: string;
}

// 链接类型
export interface LinkItem {
    path?: string;
    name?: string;
    label?: string;
    id?: string;
    sn?: string;
    link?: string;
    appLink?: string;
    data?: Record<string, unknown>;
}

// 图片列表项（带链接）
export interface PicListItem extends PicItem {
    picLink?: LinkItem;
    picTitle?: string;
    picDesc?: string;
    gradientColorA?: string;
    gradientColorB?: string;
}

// 渐变类型
export type GradientType = "upDown" | "leftRight" | "diagonal" | "purity";

// 图片填充类型
export type PicFillType = "cover" | "contain";

// 模块边框/容器设置
export interface ModuleFrame {
    textColor?: string;
    itemBackgroundColor?: string;
    backgroundColor?: string;
    innerPadding?: number;
    itemHeight?: number;
    itemRadius?: number;
    boxRadius?: number;
    boxPadding?: number;
    leftAndRightPadding?: number;
    boxPaddingTop?: number;
    boxPaddingBottom?: number;
}

// 模块标题设置
export interface ModuleTitle {
    showTitle?: number;
    titleStyle?: number;
    titleAlign?: number;
    titleBackground?: string;
    titleBackground2?: string;
    titleBackgroundPic?: PicItem;
    titleRadius?: number;
    titlePaddingTop?: number;
    titlePaddingBottom?: number;
    titleText?: string;
    titleColor?: string;
    descText?: string;
    descColor?: string;
    showMore?: number;
    /** 标题右侧内容类型：1=显示更多链接，2=显示倒计时。未设置时由各模块的老语义推导（见 _shared/titleMore.ts） */
    moreType?: number;
    /** 倒计时前缀文字（与"更多"的 moreLink.title 相互独立） */
    countdownLabel?: string;
    /**
     * 更多链接：在 LinkItem 基础上扩展"链接文字 title"。
     * 必须保留 SelectLink 回传的全部字段（path/id/sn/data 等），
     * 否则前台 urlFormat 缺少跳转所需参数会导致"更多"链接无法跳转。
     */
    moreLink?: LinkItem & {
        /** 更多链接展示文字 */
        title?: string;
        /** 链接分类标签（用于回显，如"商品"、"分类"等） */
        label?: string;
        /** 链接名称（用于回显，如具体商品名） */
        name?: string;
        /** 链接路径（用于回显） */
        path?: string;
    };
    moreColor?: string;
}
