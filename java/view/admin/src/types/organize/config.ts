/**
 * 服务配置相关类型定义
 */

/** 发单模式 */
export type DispatchMode = 1 | 2;

/** 发单模式枚举值 */
export const DispatchModeEnum = {
    /** 抢单模式 */
    GRAB: 1 as DispatchMode,
    /** 手动派单 */
    MANUAL: 2 as DispatchMode
} as const;

/** 发单模式选项 */
export const DispatchModeOptions = [
    { label: '抢单模式', value: 1 },
    { label: '手动派单', value: 2 }
] as const;

/**
 * 服务配置（前端统一视图）
 * 注意：后端实际分为同城配送配置和上门服务配置两个独立接口
 * 此类型用于前端兼容旧的服务设置页面
 */
export interface ServiceConfig {
    /** 服务功能开关（对应上门服务的 enabled） */
    serviceEnabled: boolean;
    /** 发单模式：1-抢单，2-派单（对应 orderDispatchMode） */
    dispatchMode: DispatchMode;
    /** 服务前照片是否必填（对应 beforeServicePhotoEnabled） */
    beforePhotoRequired: boolean;
    /** 服务后照片是否必填（对应 afterServicePhotoEnabled） */
    afterPhotoRequired: boolean;
}

/**
 * 同城配送配置扩展
 * @deprecated 使用 ShopLocalDeliveryConfigDetailVO
 */
export interface DeliveryConfigExtend {
    /** 发单模式 */
    dispatchMode: DispatchMode;
    /** 配送后照片是否必填 */
    afterPhotoRequired: boolean;
}

/** 配置表单数据 */
export interface ServiceConfigFormData extends ServiceConfig {}

// ===== 后端实际配置类型（重新导出） =====
export type { ShopLocalDeliveryConfigDetailVO, ShopLocalDeliveryConfigSaveParam } from '@/types/setting/intraCityDelivery';
export type { ShopHomeServiceConfigDetailVO, ShopHomeServiceConfigSaveParam } from '@/types/setting/homeService';
