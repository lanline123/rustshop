/**
 * @file 店铺客户 店铺/门店端类型（D1116-shopMember，2026-07-08 对齐后端接口文档）
 * @description 店铺端客户（会员）列表 / 详情 / 新增 / 导入。基础路径 /adminapi/user/shopMember，shopId 由登录态自动解析。
 *              脱敏由后端服务端完成（列表直接返回打码串）；冻结态由 config.isOpenShopMember 判定（顶部横幅 + 置灰写操作）。
 *              客户标签复用平台标签体系（types/user/userTags）。时间字段均为 Unix 秒。
 */

/* ---------------- 客户（会员）列表 ---------------- */

export interface ShopCustomerItem {
    userId: number;
    /** 昵称（脱敏态由后端打码，如 张**） */
    nickname: string;
    /** 手机号（脱敏态由后端打码，如 138****8888） */
    mobile: string;
    /** 头像 */
    avatar?: string;
    /** 该店累计已支付订单数 */
    orderCount?: number | null;
    /** 该店累计消费金额（已支付） */
    orderAmount?: number | string | null;
    /** 入会时间（Unix 秒） */
    joinTime?: number | null;
    /** 最近消费时间（Unix 秒；手动添加未消费为 null） */
    lastConsumeTime?: number | null;
}

/** 列表请求参数（对齐后端 GET user/shopMember/list） */
export interface ShopCustomerFilterParams {
    /** 用户类型：0 正式会员，1 游客 */
    userType?: 0 | 1;
    /** 关键字（昵称/手机号/会员ID，后端统一匹配） */
    keyword?: string;
    /** 按客户ID精确筛选 */
    userId?: number | string;
    /** 昵称（模糊） */
    nickname?: string;
    /** 手机号（模糊） */
    mobile?: string;
    /** 累计消费下限 */
    orderAmountMin?: number | string;
    /** 累计消费上限 */
    orderAmountMax?: number | string;
    /** 入会时间起（Unix 秒） */
    joinTimeStart?: number | string;
    /** 入会时间止（Unix 秒） */
    joinTimeEnd?: number | string;
    /** 最近消费时间起（Unix 秒） */
    lastConsumeTimeStart?: number | string;
    /** 最近消费时间止（Unix 秒） */
    lastConsumeTimeEnd?: number | string;
    page?: number;
    size?: number;
}

export interface ShopCustomerFilterResult {
    records: ShopCustomerItem[];
    total: number;
}

/* ---------------- 新增单个会员（2026-07-10 CR：对齐平台添加会员表单，去会员等级） ---------------- */

export interface ShopCustomerAddParams {
    /** 会员名称（对齐平台，新增必填） */
    username?: string;
    /** 手机号码 */
    mobile?: string;
    /** 邮箱地址（后端依赖：user/shopMember/add 需支持该字段） */
    email?: string;
}

/* ---------------- 批量导入结果行 ---------------- */

export interface ShopCustomerImportItem {
    userId: number;
    username: string;
    mobile: string;
    email?: string;
    nickname?: string;
    avatar?: string;
}
