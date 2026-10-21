/**
 * 优惠券模块共享能力统一导出
 *
 * 使用示例：
 * ```ts
 * import { PcCouponModule, pcCouponDefaults, PcCouponPreview, PcCouponSettings } from "../_shared/coupon";
 * ```
 */

// ==================== 类型导出 ====================
export type { PcCouponModule } from "./types";

// ==================== 默认值导出 ====================
export { pcCouponDefaults } from "./defaults";

// ==================== 组件导出 ====================
export { default as PcCouponPreview } from "./PcCouponPreview.vue";
export { default as PcCouponSettings } from "./PcCouponSettings.vue";
