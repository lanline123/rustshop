import request from "@/utils/request";
import type { WholesalerGradeVO, WholesalerGradeListQuery, WholesalerGradeSaveDTO } from "@/types/wholesale/grade";

/** 等级列表 GET /grade/list */
export const getGradeList = (params?: WholesalerGradeListQuery) => request<WholesalerGradeVO[]>({ url: "wholesale/grade/list", method: "get", params });

/** 等级新增/编辑 POST /grade/save */
export const saveGrade = (data: WholesalerGradeSaveDTO) => request<{ gradeId: number }>({ url: "wholesale/grade/save", method: "post", data });

/** 等级启停 POST /grade/setStatus */
export const setGradeStatus = (params: { gradeId: number; status: number }) =>
    request<{ warning?: string } | void>({ url: "wholesale/grade/setStatus", method: "post", params });

/** 等级删除 POST /grade/delete */
export const deleteGrade = (params: { gradeId: number }) => request({ url: "wholesale/grade/delete", method: "post", params });
