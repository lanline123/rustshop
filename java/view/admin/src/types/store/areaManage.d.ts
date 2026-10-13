export interface RequestList {
    /**
     * 区域名称
     */
    areaStoreName?: string;
    /**
     * 查询参数
     */
    keyword?: string;
    /**
     * 当前页码
     */
    page?: number;
    /**
     * 分页大小
     */
    size?: number;
    /**
     * 排序字段
     */
    sortField?: string;
    /**
     * 显示顺序
     */
    sortOrder?: string;
    [property: string]: any;
}

/**
 * PageAreaStoreManagerVO
 */
export interface Response {
    pages?: number;
    records?: AreaStoreManagerVO[];
    size?: number;
    total?: number;
    [property: string]: any;
}
/**
 * AreaStoreManagerVO，区域门店管理参数
 */
export interface AreaStoreManagerVO {
    /**
     * 区域门店管理ID
     */
    areaStoreManagerId?: number;
    /**
     * 区域门店名称
     */
    areaStoreName?: string;
    /**
     * 添加时间
     */
    createTime?: string;
    /**
     * 绑定店铺数量
     */
    shopCount?: number;
    /**
     * 门店id集合
     */
    shopIds?: string;
    /**
     * 排序字段
     */
    sortOrder?: number;
    /**
     * 添加时间
     */
    updateTime?: string;
    [property: string]: any;
}

/**
 * AreaStoreManagerCreateParam，区域门店管理创建参数
 */
export interface RequestInfo {
    /**
     * 区域门店名称
     */
    areaStoreName: string;
    /**
     * 排序字段
     */
    sortOrder: number;
    shopIds: number[];
}


export interface RequestShopList {
    /**
     * 区域门店管理ID
     */
    areaStoreManagerId?: number;
    /**
     * 是否选择
     */
    check?: number;
    /**
     * 查询参数
     */
    keyword?: string;
    /**
     * 当前页码
     */
    page?: number;
    /**
     * 店铺名称
     */
    shopTitle?: string;
    /**
     * 分页大小
     */
    size?: number;
    /**
     * 排序字段
     */
    sortField?: string;
    /**
     * 显示顺序
     */
    sortOrder?: string;
    [property: string]: any;
}

/**
 * PageShopVO
 */
export interface ResponseShopList {
    pages?: number;
    records?: ShopVO[];
    size?: number;
    total?: number;
    [property: string]: any;
}

/**
 * ShopVO，店铺表参数
 */
export interface ShopVO {
    /**
     * 店铺创建时间
     */
    addTime?: string;
    adminUserShop?: AdminUserShopVO;
    /**
     * 是否选中
     */
    check?: boolean;
    /**
     * 点击量
     */
    clickCount?: number;
    /**
     * 联系电话
     */
    contactMobile?: string;
    /**
     * 简介
     */
    description?: string;
    /**
     * 冻结资金
     */
    frozenMoney?: number;
    /**
     * 是否联系客服：1 是 0 否
     */
    isContactKefu?: number;
    /**
     * 客服入口信息页面： 1 商品详情页 2 订单页
     */
    kefuInlet?: number[];
    /**
     * 客服链接
     */
    kefuLink?: string;
    /**
     * 客服电话
     */
    kefuPhone?: string;
    /**
     * 客服微信
     */
    kefuWeixin?: string;
    merchant?: MerchantVO;
    /**
     * 商户id
     */
    merchantId?: number;
    /**
     * 联系方式（JSON）
     */
    shopContactConfig?: ShopContactConfig[];
    /**
     * 门店封面(门店时必填)
     */
    shopCoverPicture?: string;
    /**
     * 门店详细地址
     */
    shopDetailedAddress?: string;
    /**
     * 店铺表ID
     */
    shopId?: number;
    /**
     * 店铺纬度
     */
    shopLatitude?: number;
    /**
     * 店铺logo
     */
    shopLogo?: string;
    /**
     * 店铺经度
     */
    shopLongitude?: number;
    /**
     * 店铺资金
     */
    shopMoney?: number;
    shopOpenCloseConfig?: ShopOpenCloseConfig;
    /**
     * 门店regionId
     */
    shopRegionIds?: number[];
    /**
     * 门店regionId
     */
    shopRegionNames?: string[];
    /**
     * 门头照片
     */
    shopShowPicture?: PicInfo[];
    /**
     * 店铺名称
     */
    shopTitle?: string;
    /**
     * 1店铺，2门店，3自提点
     */
    shopType?: number;
    /**
     * 店铺状态1开业 4暂停运营 10关店
     */
    status?: number;
    /**
     * 店铺文案
     */
    statusText?: string;
    /**
     * 自提时传入门店id
     */
    storeParentId?: number;
    /**
     * 上级组织名称
     */
    storeParentName?: string;
    [property: string]: any;
}

/**
 * AdminUserShopVO，店铺员工管理VO
 */
export interface AdminUserShopVO {
    /**
     * 添加时间
     */
    addTime?: string;
    /**
     * 管理员ID
     */
    adminId?: number;
    adminUser?: AdminUserVO;
    /**
     * 权限列表
     */
    authList?: { [key: string]: any }[];
    /**
     * 头像
     */
    avatar?: string;
    /**
     * 邮箱
     */
    email?: string;
    /**
     * ID
     */
    id?: number;
    /**
     * 是否管理员
     */
    isAdmin?: number;
    /**
     * 是否使用
     */
    isUsing?: number;
    role?: RoleVO;
    /**
     * 角色ID
     */
    roleId?: number;
    /**
     * 店铺ID
     */
    shopId?: number;
    user?: UserVO;
    /**
     * 用户ID
     */
    userId?: number;
    /**
     * 用户名
     */
    username?: string;
    [property: string]: any;
}

/**
 * AdminUserVO，管理员用户VO
 */
export interface AdminUserVO {
    /**
     * 管理员ID
     */
    adminId?: number;
    /**
     * 头像
     */
    avatar?: string;
    /**
     * 商户ID
     */
    merchantId?: number;
    /**
     * 手机号码
     */
    mobile?: string;
    /**
     * 用户名
     */
    username?: string;
    [property: string]: any;
}

/**
 * RoleVO，角色VO
 */
export interface RoleVO {
    /**
     * 角色ID
     */
    roleId?: number;
    /**
     * 角色名称
     */
    roleName?: string;
    /**
     * 店铺id
     */
    shopId?: number;
    [property: string]: any;
}

/**
 * UserVO，用户信息
 */
export interface UserVO {
    /**
     * 头像
     */
    avatar?: string;
    /**
     * 昵称
     */
    nickname?: string;
    /**
     * 用户ID
     */
    userId?: number;
    /**
     * 用户名
     */
    username?: string;
    [property: string]: any;
}

/**
 * MerchantVO，商家表参数
 */
export interface MerchantVO {
    /**
     * 商户认证日期
     */
    addTime?: string;
    admin?: MerchantAdminDataDTO;
    /**
     * 基础数据json
     */
    baseData?: BaseData;
    /**
     * 企业名称
     */
    companyName?: string;
    /**
     * 申请主体信息
     */
    corporateName?: string;
    /**
     * 入驻申请ID
     */
    merchantApplyId?: number;
    merchantData?: JSONObject;
    /**
     * 商家ID
     */
    merchantId?: number;
    /**
     * 结算周期单位天
     */
    settlementCycle?: number;
    /**
     * 店铺数量
     */
    shopCount?: number;
    shopData?: JSONObject;
    /**
     * 状态，1：正常，2：取消资格
     */
    status?: number;
    /**
     * 状态文本
     */
    statusText?: string;
    /**
     * 类型，1：个人，2：企业
     */
    type?: number;
    /**
     * 类型文案，1：个人认证，2：企业认证
     */
    typeText?: string;
    user?: User;
    /**
     * 用户ID
     */
    userId?: number;
    [property: string]: any;
}

/**
 * MerchantAdminDataDTO，商户管理用户数据
 */
export interface MerchantAdminDataDTO {
    /**
     * admin_id
     */
    adminId?: number;
    /**
     * admin_user_id
     */
    adminUserId?: number;
    /**
     * is_admin
     */
    isAdmin?: number;
    /**
     * merchant_id
     */
    merchantId?: number;
    /**
     * merchant_user_id
     */
    merchantUserId?: number;
    /**
     * user_id
     */
    userId?: number;
    /**
     * 用户名
     */
    username?: string;
    [property: string]: any;
}

/**
 * 基础数据json
 */
export interface BaseData {
    empty?: boolean;
    innerMap?: { [key: string]: { [key: string]: any } };
    [property: string]: boolean | undefined | { [key: string]: any };
}

/**
 * JSONObject
 */
export interface JSONObject {
    config?: JSONConfig;
    empty?: boolean;
    raw?: { [key: string]: { [key: string]: any } };
    [property: string]: boolean | undefined | { [key: string]: any }; // 允许更多类型
}

/**
 * JSONConfig
 */
export interface JSONConfig {
    checkDuplicate?: boolean;
    dateFormat?: string;
    ignoreCase?: boolean;
    ignoreError?: boolean;
    ignoreNullValue?: boolean;
    keyComparator?: { [key: string]: any };
    order?: boolean;
    stripTrailingZeros?: boolean;
    transientSupport?: boolean;
    [property: string]: any;
}

/**
 * User，用户数据
 */
export interface User {
    /**
     * 当前会员地址
     */
    addressId?: number;
    /**
     * 头像
     */
    avatar?: string;
    /**
     * 用户现有余额
     */
    balance?: number;
    /**
     * 出生日期
     */
    birthday?: string;
    /**
     * 分销员注册时间
     */
    distributionRegisterTime?: number;
    /**
     * 会员邮箱
     */
    email?: string;
    /**
     * 邮箱是否已验证
     */
    emailValidated?: number;
    /**
     * 公众号1,小程序2,H5,PC4,APP5
     */
    fromTag?: number;
    /**
     * 用户冻结余额
     */
    frozenBalance?: number;
    /**
     * 成长值
     */
    growthPoints?: number;
    /**
     * 【JSON】历史浏览记录id：number[]
     */
    historyProductIds?: string;
    /**
     * 是否是公司授权会员
     */
    isCompanyAuth?: number;
    /**
     * 是否是分销员
     */
    isDistribution?: number;
    /**
     * 是否是超级会员 1是 0不是 2过期
     */
    isSvip?: number;
    /**
     * 最后一次登录IP
     */
    lastIp?: string;
    /**
     * 最后一次登录时间
     */
    lastLogin?: number;
    /**
     * 手机号
     */
    mobile?: string;
    /**
     * 手机号是否已验证
     */
    mobileValidated?: number;
    /**
     * 昵称
     */
    nickname?: string;
    /**
     * 累积消费金额
     */
    orderAmount?: number;
    /**
     * 累积消费次数
     */
    orderCount?: number;
    /**
     * 用户密码
     */
    password?: string;
    /**
     * 积分
     */
    points?: number;
    /**
     * 会员等级id
     */
    rankId?: number;
    /**
     * 推荐人会员id
     */
    referrerUserId?: number;
    /**
     * 注册时间
     */
    regTime?: number;
    /**
     * 会员状态；1-正常，0-禁用
     */
    status?: number;
    /**
     * 会员过期时间
     */
    svipExpireTime?: number;
    /**
     * 会员资料自增id
     */
    userId?: number;
    /**
     * 用户名
     */
    username?: string;
    /**
     * 微信二维码图片
     */
    wechatImg?: string;
    [property: string]: any;
}

/**
 * ShopContactConfig，联系方式（JSON）
 */
export interface ShopContactConfig {
    /**
     * 类型 1 = 普通座机号 2 = 企业座机号 3 = 手机号码
     */
    type?: number;
    /**
     * 号码 使用 - 隔开，前面为区号，后面为号码
     */
    values?: string;
    [property: string]: any;
}

/**
 * ShopOpenCloseConfig，运营时间（JSON）
 */
export interface ShopOpenCloseConfig {
    times?: TimeRange[];
    /**
     * 营业模式类型 1 = 全天 2 = 每日固定时间 3 = 按星期时间
     */
    type?: number;
    [property: string]: any;
}

/**
 * TimeRange
 */
export interface TimeRange {
    /**
     * 星期（1=周一，7=周日），type=3 时必填，其他模式可为 null
     */
    dayOfWeek?: string;
    ranges?: TimeRangeStartEnd[];
    [property: string]: any;
}

/**
 * TimeRangeStartEnd
 */
export interface TimeRangeStartEnd {
    /**
     * 结束时间（HH:mm 格式）
     */
    end?: string;
    /**
     * 开始时间（HH:mm 格式）
     */
    start?: string;
    [property: string]: any;
}

/**
 * PicInfo，图片信息
 */
export interface PicInfo {
    /**
     * 图片名称
     */
    picName?: string;
    /**
     * 图片缩略图
     */
    picThumb?: string;
    /**
     * 图片URL
     */
    picUrl?: string;
    /**
     * 状态
     */
    status?: string;
    /**
     * UID
     */
    uid?: number;
    /**
     * URL
     */
    url?: string;
    [property: string]: any;
}