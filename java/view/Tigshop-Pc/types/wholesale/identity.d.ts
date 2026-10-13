/**
 * 当前用户的批发商身份
 * @description 接口 GET wholesale/me 返回值；登出态或后台总开关关时整体不渲染批发能力
 */
export interface WholesaleIdentity {
    /** 是否批发商 */
    isWholesaler: boolean;
    /** 绑定的批发商等级 ID；未绑定为 null */
    gradeId: number | null;
    /** 绑定的批发商等级名称；未绑定为空串 */
    gradeName: string;
    /** 后台总开关；false=关停，前端不渲染批发字段、按零售表现 */
    globalEnabled: boolean;
}
