/**
 * 拼团模块共享能力统一导出（移植自 Vort-Admin _shared/groupon + _shared/buy-button）
 *
 * 使用示例：
 * ```ts
 * import {
 *   GrouponActivityItem,
 *   ModuleBuyButton,
 *   useGrouponItemList,
 *   useScrollControl,
 *   dateStringToTimestamp
 * } from "../_shared/groupon";
 * ```
 */

// ==================== 类型导出 ====================
export type { GrouponActivityItem, GrouponProductItem } from "./types";

// ==================== 组件导出 ====================
/** 选择拼团活动：供 DialogForm path 加载，勿再以独立弹窗方式引用 */
export { default as SelectGrouponActivityDialog } from "./components/SelectGrouponActivityDialog.vue";
export { default as ModuleBuyButton } from "./components/ModuleBuyButton.vue";

// ==================== Composables 导出 ====================
export { useGrouponItemList, defaultMapGrouponItem } from "./composables/useGrouponItemList";
export type { UseGrouponItemListOptions } from "./composables/useGrouponItemList";

// ==================== 工具函数导出 ====================
// 滚动工具
export { getScrollStep, scrollPrev, scrollNext, useScrollControl } from "./utils/scroll";
// 时间工具
export { dateStringToTimestamp, formatNum, parseCountdown, formatCountdownText } from "./utils/time";
