/**
 * PC 首页拼团模块类型定义
 * 说明：PC首页独有的拼团模块，对齐前台 pcGroupon 页面
 */

import type { ModuleTitle } from "../_shared/types";
import type { GrouponActivityItem } from "../_shared/groupon";

export interface PcHomeGrouponModule {
    // ==================== 基础设置 ====================
    /**
     * 模板样式：2=一行四个, 3=一行五个, 4=一行六个, 6=横向滑动（每屏6个）
     * 前台 pcGroupon 使用 style 字段控制列数/滚动行为
     */
    style?: number;
    /**
     * 拼团类型：1=普通拼团 2=阶梯拼团
     */
    groupType: number;
    /**
     * 商品卡片样式：1=样式一（前台同款 priceBox + 图片倒计时 overlay） 2=样式二（简版价格布局）
     */
    cardStyle?: number;

    // ==================== 显示控制 ====================
    /**
     * 显示商品名称：0=不显示, 1=显示
     */
    showName?: number;
    /**
     * 显示商品价格：0=不显示, 1=显示
     */
    showPrice?: number;

    // ==================== 对齐/间距 ====================
    /**
     * 文本对齐：1=左对齐, 2=居中对齐
     * 当 cardStyle !== 1 时，控制价格区域的 flex-direction（1=row, 2=column）
     */
    textAlign?: number;
    /**
     * 商品间距：影响列表 gap 和 padding
     */
    goodsPadding?: number;

    // ==================== 容器样式 ====================
    /**
     * 背景颜色
     */
    backgroundColor?: string;
    /**
     * 圆角
     */
    boxRadius?: number;
    /**
     * 内边距（模块内容区）
     */
    innerPadding?: number;
    /**
     * 左右边距
     */
    boxPadding?: number;
    /**
     * 上边距
     */
    boxPaddingTop?: number;
    /**
     * 下边距
     */
    boxPaddingBottom?: number;

    // ==================== 标题设置 ====================
    /**
     * 标题配置（复用通用 ModuleTitle 类型）
     * - showTitle: 是否显示标题区
     * - showMore: 是否显示顶部倒计时区域/查看更多
     * - titleText/titleColor: 标题文字与颜色
     * - descText/descColor: 描述文字与颜色
     * - moreColor: 倒计时文字颜色
     */
    title?: ModuleTitle;

    // ==================== 购买按钮 ====================
    /**
     * 购买按钮样式：0=不显示, >0 时显示对应样式的购买按钮（对接 ModuleBuyButton）
     */
    buyBtnStyle?: number;

    // ==================== 活动数据 ====================
    /**
     * 选中的拼团活动（复用共享 groupon 类型）
     */
    grouponActivityItem?: GrouponActivityItem | null;
}
