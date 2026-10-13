/**
 * @file 店铺客户 店铺/门店端接口（D1116-shopMember，2026-07-08 对齐后端接口文档）
 * @description 客户（会员）列表 / 详情 / 新增 / 导入 / 导入失败数据下载。
 *              基础路径 /adminapi/user/shopMember（url 前缀 user/shopMember/），shopId 由登录态自动解析，前端不传。
 *              客户标签复用 api/user/userTags。新增/导入受平台开关双守卫（后端拦截 + 前端置灰）。
 */
import request from "@/utils/request";
import { getAdminType } from "@/utils/storage";
import type {
    ShopCustomerAddParams,
    ShopCustomerFilterParams,
    ShopCustomerFilterResult,
    ShopCustomerImportItem,
    ShopCustomerItem
} from "@/types/merchant/shopMember.d";

/* -------- 客户（会员）列表 -------- */

export const getShopCustomerList = (params: ShopCustomerFilterParams) =>
    request<ShopCustomerFilterResult>({ url: "user/shopMember/list", method: "get", params });

/* -------- 客户详情（店铺端仅传 userId） -------- */

export const getShopCustomerDetail = (params: { userId: number }) =>
    request<ShopCustomerItem>({ url: "user/shopMember/detail", method: "get", params });

/* -------- 新增单个客户 -------- */

export const addShopCustomer = (data: ShopCustomerAddParams) =>
    request<number>({ url: "user/shopMember/add", method: "post", data });

/* -------- 批量导入客户（Excel，multipart/form-data，字段名 file） -------- */

export const importShopCustomer = (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return request<ShopCustomerImportItem[]>({
        url: "user/shopMember/import",
        method: "post",
        data: formData,
        headers: { "Content-Type": "multipart/form-data" }
    });
};

/* -------- 下载导入失败行（回传原文件，返回带错误信息的 Excel 流） -------- */

export const downloadShopCustomerImportError = (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return request({
        url: "user/shopMember/importErrorData",
        method: "post",
        data: formData,
        responseType: "arraybuffer",
        headers: { "Content-Type": "multipart/form-data" }
    });
};

/* -------- 下载导入模板 --------
 * 店铺端（adminType !== 'admin'）：走店铺作用域 user/shopMember/downloadImportTemplate；
 * 平台端（adminType === 'admin'）：沿用旧的平台会员模板 user/user/downloadUserImportTemplate。
 */
export const downloadShopCustomerImportTemplate = () => {
    const url =
        getAdminType() === "admin"
            ? "user/user/downloadUserImportTemplate"
            : "user/shopMember/downloadImportTemplate";
    return request({ url, method: "get", responseType: "arraybuffer" });
};
