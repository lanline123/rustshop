import request from "@/utils/request";
import type {
    WholesalerPageQuery,
    WholesalerPageResult,
    WholesalerAddDTO,
    WholesalerImportDTO,
    WholesalerImportResultVO,
    WholesalerSelectableUserQuery,
    WholesalerSelectableUserPageResult
} from "@/types/wholesale/wholesaler";

/** 批发商分页 GET /wholesaler/page */
export const getWholesalerPage = (params: WholesalerPageQuery) => request<WholesalerPageResult>({ url: "wholesale/wholesaler/page", method: "get", params });

/** 可选为批发商的会员分页（含 isWholesaler 标识） GET /wholesaler/selectableUserPage */
export const getSelectableUserPage = (params: WholesalerSelectableUserQuery) =>
    request<WholesalerSelectableUserPageResult>({ url: "wholesale/wholesaler/selectableUserPage", method: "get", params });

/** 手动添加批发商 POST /wholesaler/add */
export const addWholesaler = (data: WholesalerAddDTO) => request<{ wholesalerId: number }>({ url: "wholesale/wholesaler/add", method: "post", data });

/** 批量导入批发商（同步，直接返回结果 VO） POST /wholesaler/import */
export const importWholesaler = (data: WholesalerImportDTO) => request<WholesalerImportResultVO>({ url: "wholesale/wholesaler/import", method: "post", data });

/** 启停 POST /wholesaler/setStatus（参数走 URL query） */
export const setWholesalerStatus = (params: { wholesalerId: number; status: number }) =>
    request({ url: "wholesale/wholesaler/setStatus", method: "post", params });

/** 改等级 POST /wholesaler/changeGrade（参数走 URL query） */
export const changeWholesalerGrade = (params: { wholesalerId: number; gradeId: number }) =>
    request({ url: "wholesale/wholesaler/changeGrade", method: "post", params });

/** 移除批发商 POST /wholesaler/remove（软删批发商身份，支持单个/多选）*/
export const removeWholesaler = (data: { wholesalerIds: number[] }) =>
    request<{ removedCount: number }>({ url: "wholesale/wholesaler/remove", method: "post", data });
