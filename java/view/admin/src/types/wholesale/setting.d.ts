// 批发商设置

/** 设置查询出参（GET /setting/get） */
export interface WholesaleSettingVO {
    /** 批发功能总开关 0=关闭 / 1=启用 */
    wholesaleEnabled: number;
    /** 新会员注册自动转批发商 0=否 / 1=是 */
    wholesaleAutoRegister: number;
    /** 自动转批发的默认等级 id，0=未配置 */
    wholesaleDefaultGradeId: number;
    /** 批发订单是否赠送积分 0=否 / 1=是 */
    wholesaleGivePoints: number;
    /** 默认批发等级名称（联表回填，展示用） */
    defaultGradeName?: string;
    /** 当前是否多店铺环境（只读兼容字段，不参与前端禁用判断） */
    isMultiShop?: boolean;
}

/** 设置保存入参（POST /setting/save） */
export interface WholesaleSettingSaveDTO {
    /** 批发功能总开关 0=关闭 / 1=启用 */
    wholesaleEnabled: number;
    /** 新会员注册自动转批发商 0=否 / 1=是 */
    wholesaleAutoRegister: number;
    /** 自动转批发的默认等级 id：0=未配置；>0 须为启用中的等级 */
    wholesaleDefaultGradeId: number;
    /** 批发订单是否赠送积分 0=否 / 1=是 */
    wholesaleGivePoints: number;
}
