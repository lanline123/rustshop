/**
 * @file 店铺客户 平台端接口（D1116-shopMember，2026-07-08 对齐后端接口文档）
 * @description 平台「客户设置」读写。基础路径 /adminapi/user/shopMember（url 前缀 user/shopMember/）。
 *              平台端店铺列表/会员只读查看接口本轮未接入（见 spec §11）。
 */
import request from "@/utils/request";
import type { ShopMemberSettingState } from "@/types/user/shopMember.d";

/* -------- 客户设置（能力门 + 店铺客户设置项；保存后刷新 config 供店铺端读取） -------- */

export const getShopMemberSetting = () =>
    request<ShopMemberSettingState>({ url: "user/shopMember/setting/get", method: "get" });

export const updateShopMemberSetting = (data: ShopMemberSettingState) =>
    request({ url: "user/shopMember/setting/save", method: "post", data });
