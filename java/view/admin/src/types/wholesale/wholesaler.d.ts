// 批发商管理

/** 批发商分页查询入参（GET /wholesaler/page，WholesalerPageQuery） */
export interface WholesalerPageQuery {
    /** 页码，默认 1 */
    page?: number;
    /** 每页条数，默认 20，1-100 */
    size?: number;
    /** 联合模糊：username + mobile + email + nickname */
    keyword?: string;
    /** 批发等级筛选 */
    gradeId?: number;
    /** 批发商启停：0=禁用 1=启用 */
    status?: number;
    /** 注册渠道筛选（user.fromTag：1=公众号 2=小程序 3=H5 4=PC 5=APP） */
    fromTag?: number;
    /** 批发商来源：1=注册自动 2=后台导入 3=手动添加 */
    source?: number;
    /** 添加时间起 yyyy-MM-dd HH:mm:ss */
    addTimeStart?: string;
    /** 添加时间止 yyyy-MM-dd HH:mm:ss */
    addTimeEnd?: string;
    sortField?: string;
    sortOrder?: string;
}

/** 批发商列表项（WholesalerVO） */
export interface WholesalerVO {
    /** 批发商 ID */
    wholesalerId: number;
    /** 关联 user_id */
    userId: number;
    /** 用户名 */
    username?: string;
    /** 手机号 */
    mobile?: string;
    /** 邮箱 */
    email?: string;
    /** 昵称 */
    nickname?: string;
    /** 头像 */
    avatar?: string;
    /** 会员等级 ID */
    rankId?: number;
    /** 会员等级名称 */
    rankName?: string;
    /** 是否企业认证：0=否 1=是 */
    isCompanyAuth?: number;
    /** 认证状态名（派生：1→已认证 0→未认证） */
    authStatusName?: string;
    /** 注册渠道：1=公众号 2=小程序 3=H5 4=PC 5=APP */
    fromTag?: number;
    /** 注册渠道名（派生） */
    fromTagName?: string;
    /** 会员状态：1=正常 0=禁用 */
    userStatus?: number;
    /** 批发等级 ID */
    gradeId?: number;
    /** 批发等级名称 */
    gradeName?: string;
    /** 批发商启停：0=禁用 1=启用 */
    status: number;
    /** 来源：1=注册自动 2=后台导入 3=手动添加 */
    source?: number;
    /** 来源名（派生） */
    sourceName?: string;
    /** 备注 */
    remark?: string;
    /** 添加时间 yyyy-MM-dd HH:mm:ss */
    addTime?: string;
}

/** 批发商分页结果（IPage<WholesalerVO>） */
export interface WholesalerPageResult {
    records: WholesalerVO[];
    total: number;
    size?: number;
    current?: number;
}

/** 手动添加批发商入参（WholesalerAddDTO） */
export interface WholesalerAddDTO {
    userId: number;
    gradeId: number;
    remark?: string;
}

/** 批量导入批发商入参（WholesalerImportDTO，同步） */
export interface WholesalerImportDTO {
    /** true=导入全部 user；false=按 userIds/rankIds */
    importAll?: boolean;
    /** 指定会员维度：勾选的会员 ID（1-5000） */
    userIds?: number[];
    /** 会员等级维度：勾选的会员等级 ID */
    rankIds?: number[];
    /** 统一等级（缺省由后端按默认批发等级处理） */
    gradeId?: number;
    /** 默认 OVERWRITE_GRADE；SKIP=已是批发商跳过 */
    existsStrategy?: "OVERWRITE_GRADE" | "SKIP";
}

/** 可选为批发商的会员分页查询入参（GET /wholesaler/selectableUserPage） */
export interface WholesalerSelectableUserQuery {
    page?: number;
    size?: number;
    keyword?: string;
    fromTag?: number | string;
    sortField?: string;
    sortOrder?: string;
    rankId?: number;
}

/** 可选为批发商的会员列表项（WholesalerSelectableUserVO） */
export interface WholesalerSelectableUserVO {
    userId: number;
    username?: string;
    nickname?: string;
    avatar?: string;
    mobile?: string;
    email?: string;
    isCompanyAuth?: number;
    status?: number;
    statusName?: string;
    fromTag?: number;
    fromTagName?: string;
    /** 是否已是批发商：true=已是 false=未是 */
    isWholesaler?: boolean;
    /** 已是批发商时的批发等级 ID */
    gradeId?: number;
    /** 已是批发商时的批发等级名称 */
    gradeName?: string;
}

/** 可选为批发商的会员分页结果（IPage<WholesalerSelectableUserVO>） */
export interface WholesalerSelectableUserPageResult {
    records: WholesalerSelectableUserVO[];
    total: number;
    size?: number;
    current?: number;
}

/** 批量导入结果（WholesalerImportResultVO） */
export interface WholesalerImportResultVO {
    totalRequested: number;
    successCount: number;
    /** 已是批发商被覆盖等级 或 跳过 */
    skippedCount: number;
    failedCount: number;
    failedDetails: WholesalerImportFailedDetail[];
}

export interface WholesalerImportFailedDetail {
    userId?: number;
    errorCode?: number;
    errorMessage?: string;
    reason?: string;
}
