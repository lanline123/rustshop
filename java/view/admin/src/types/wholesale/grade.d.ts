// 批发商等级

/** 等级列表项（GET /grade/list → List<WholesalerGradeVO>） */
export interface WholesalerGradeVO {
    gradeId: number;
    gradeName: string;
    /** 0=禁用 / 1=启用 */
    status: number;
    sortOrder: number;
    /** 已绑定客户数（withUserCount=true 时返回） */
    userCount?: number;
    /** 是否默认等级（= wholesale.default_grade_id == gradeId），只读 */
    isDefault?: boolean;
    remark?: string;
    addTime?: string;
}

/** 等级列表查询入参 */
export interface WholesalerGradeListQuery {
    /** 状态筛选；下拉只取启用中需传 1 */
    status?: number;
    /** 是否返回已绑定客户数，默认 true */
    withUserCount?: boolean;
}

/** 等级新增/编辑入参（POST /grade/save，WholesalerGradeSaveDTO） */
export interface WholesalerGradeSaveDTO {
    /** 不传=新增；>0=编辑 */
    gradeId?: number;
    gradeName: string;
    sortOrder?: number;
    remark?: string;
}
