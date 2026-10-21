import request from "@/utils/request";
import { FilterResult } from "@/types/decorate/mobileDecorate.d";
import { formatDomain } from "@/utils/format";
// 获取装修页面列表
export const getDecorateList = (params: object) => {
    return request<FilterResult>({
        url: "decorate/decorate/list",
        method: "get",
        params
    });
};
// 装修页面列表页面批量操作
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "decorate/decorate/batch",
        method: "post",
        data: { type, ...data }
    });
};
// 装修页面列表页面删除项
export const delDecorate = (data: object) => {
    return request({
        url: "decorate/decorate/del",
        method: "post",
        data
    });
};
// 装修页面列表页面更新项
export const updateDecorateFiled = (data: object) => {
    return request({
        url: "decorate/decorate/updateField",
        method: "post",
        data
    });
};
// 获取装修页面详情
export const getDecorate = (action: string, params: object) => {
    return request<any>({
        url: "decorate/decorate/" + action,
        method: "get",
        params
    });
};
// 更新装修页面
export const updateDecorate = (operation: string, data: object) => {
    return request({
        url: "decorate/decorate/" + operation,
        method: "post",
        data
    });
};

// 装修分享接口
export const decorateShare = (params: object) => {
    return request<any>({
        url: "decorate/decorateShare/share",
        method: "get",
        params
    });
};

// 装修链接导入接口
export const importDecorateLink = (params: object) => {
    return request({
        url: "decorate/decorateShare/import",
        method: "get",
        params
    });
};

/** 装修推广短链接接口返回结构 */
export interface DecoratePromoteShortLink {
    /** H5 短链完整 URL */
    shortLink: string;
    /** PC 短链完整 URL */
    pcShortLink?: string;
    /** 微信小程序短链；生成失败时为 null */
    wechatShortLink: string | null;
    /** 微信短链生成失败原因，成功时为 null */
    wechatShortLinkError?: string | null;
    /** 小程序二维码（Base64 data URL），生成失败时为空字符串 */
    miniCode?: string;
}

// 生成装修推广短链接
export const generateShortLink = (longUrl: string) => {
    return request<DecoratePromoteShortLink>({
        url: "decorate/promote/generateShortLink",
        method: "post",
        params: { longUrl }
    });
};

/** 装修分享信息结构 */
export interface DecorateShareInfo {
    /** 装修页面 ID */
    decorateId: number | string;
    /** 分享标题 */
    shareTitle?: string | null;
    /** 分享描述 */
    shareDesc?: string | null;
    /** 分享图片 URL */
    shareImage?: string | null;
}

// 查询装修分享信息
export const getShareInfo = (decorateId: number | string) => {
    return request<DecorateShareInfo>({
        url: "decorate/promote/shareInfo",
        method: "get",
        params: { decorateId }
    });
};

// 保存装修分享信息
export const saveShareInfo = (data: DecorateShareInfo) => {
    return request<boolean>({
        url: "decorate/promote/saveShareInfo",
        method: "post",
        data
    });
};


// 设为默认页（首页/个人中心/分类页等）
export const setDefaultPage = (data: { id: number; pageType: number }) => {
    return request({
        url: "decorate/decorate/setHome",
        method: "post",
        data,
    });
};

// ===================== 装修模板库（转发官网开放接口） =====================
// 客户访问的根域名，与官网授权绑定同源（统一走 formatDomain，避免 www/端口格式不一致导致校验失败）
const getClientDomain = () => formatDomain(window.location.origin);

// 模板分类树（页面/行业/节日/颜色四个维度）
export const getTemplateCategory = () => {
    return request<any>({
        url: "decorate/template/category",
        method: "get",
        params: { domain: getClientDomain() }
    });
};

// 模板页面列表（页面维度，支持 pageType/categoryIds/keyword 筛选 + 后端分页）
export const getTemplateList = (params: object) => {
    return request<any>({
        url: "decorate/template/list",
        method: "get",
        params: { ...params, domain: getClientDomain() }
    });
};

// 模板页面详情（按 page_sn 获取该页装修 JSON）
export const getTemplateDetail = (sn: string) => {
    return request<any>({
        url: "decorate/template/detail",
        method: "get",
        params: { sn, domain: getClientDomain() }
    });
};
