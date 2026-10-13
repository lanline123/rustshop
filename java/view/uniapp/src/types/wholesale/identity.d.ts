/**
 * @file 批发商身份相关类型定义
 */

/**
 * 批发商身份
 * - 由 GET /api/wholesale/me 返回
 * - 未登录态后端也回 isWholesaler=false，不报错
 */
export interface WholesaleIdentity {
    /** 当前用户是否批发商身份 */
    isWholesaler: boolean;
    /** 批发等级 ID；非批发商为 0 */
    gradeId: number;
    /** 等级名；非批发商为空串 */
    gradeName: string;
    /** 后台批发总开关；false=关停，前端所有批发 UI 隐藏 */
    globalEnabled: boolean;
}
