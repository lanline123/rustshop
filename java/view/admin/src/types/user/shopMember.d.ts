/**
 * @file 店铺客户 平台端类型（D1116-shopMember，2026-07-08 对齐后端接口文档）
 * @description 平台端仅「客户设置」：店铺客户总开关 + 信息脱敏 / 添加客户 / 导入客户。
 *              后端基础路径 /adminapi/user/shopMember，setting/get|save。
 */

/** 开关：0关闭 1开启 */
export enum ShopMemberSwitch {
    Off = 0,
    On = 1
}

/* ---------------- 平台「客户设置」 ---------------- */

export interface ShopMemberSettingState {
    /** 店铺客户总开关（能力门）：0关 1开 */
    shopMemberEnabled: number;
    /** 店铺客户信息脱敏：0关 1开（店铺端客户电话/姓名脱敏展示） */
    shopMemberDesensitize?: number;
    /** 店铺添加客户：0关 1开（店铺可自行新增店铺客户） */
    shopMemberAllowAdd?: number;
    /** 店铺导入客户：0关 1开（店铺可导入自有客户） */
    shopMemberAllowImport?: number;
}
