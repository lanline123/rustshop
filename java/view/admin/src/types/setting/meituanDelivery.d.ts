/**
 * 美团配送（第三方配送）相关类型
 * 依据《美团配送前端接口文档 v1.1》
 * 端：平台端（adminapi/o2o/meituanDelivery/*）
 */

// ===== 平台密钥配置（文档 4.1/4.2） =====
export interface MeituanPlatformConfig {
    /** 平台总开关（第三方配送/美团） 0/1 */
    meituanDeliveryEnabled?: number;
    /** appKey（中间脱敏回显） */
    meituanDeliveryAppKey?: string;
    /** appSecret（中间脱敏回显） */
    meituanDeliveryAppSecret?: string;
    /** API 地址，空则用默认 */
    meituanDeliveryApiUrl?: string;
    /** 平台备注（第三方配送管理列表展示，选填） */
    meituanDeliveryRemark?: string;
    /**
     * 是否启用商家自送（Tab1 配送设置） 0/1
     * 后端依赖：文档未列该字段，后端补齐前前端乐观读写，缺省 0
     */
    merchantSelfDeliveryEnabled?: number;
    [property: string]: any;
}

// ===== 美团经营品类树节点（文档 5.0 categoryTree） =====
export interface MeituanCategoryNode {
    /** 品类 code（一级如 110，二级如 110002） */
    code: number;
    /** 品类名称 */
    name: string;
    /** 二级品类列表；二级节点为 null */
    children?: MeituanCategoryNode[] | null;
}

// ===== 配置项码表项（文档 5.0.1 deliveryServiceCodes/payTypes/triggerModes 通用结构） =====
export interface MeituanCodeItem {
    /** 码值（如服务码 4002） */
    code: number;
    /** 码值文案（如 飞速达） */
    name: string;
}

// ===== 门店（自营）美团配送配置可写字段（文档 5.1） =====
export interface MeituanShopConfigSaveParam {
    /** 0 关闭 / 1 开启 */
    enabled?: number;
    /** 门店经营一级品类 code（如 110；取值见 categoryTree） */
    goodsCategory?: number;
    /** 门店经营二级品类 code（如 110002；取值见 categoryTree 对应一级的 children） */
    secondCategory?: number;
    /** 服务码逗号分隔（如 4002,4011） */
    deliveryServiceCodes?: string;
    /** 1 账期 / 2 余额 */
    payType?: number;
    /** 1 手动 / 2 付款后自动 */
    triggerMode?: number;
    /** 门店名称 */
    shopName?: string;
    /** 门店联系人 */
    contactName?: string;
    /** 门店联系人电话 */
    contactPhone?: string;
    /** 城市名称 */
    cityName?: string;
    /** 门店地址 */
    address?: string;
    /** 门店经度 */
    longitude?: number | string;
    /** 门店纬度 */
    latitude?: number | string;
    /** 营业时间 */
    businessHours?: string;

    // ===== 资质信息（文档 5.1，提交审核必填） =====
    /** 企业全称（提交审核必填） */
    enterpriseName?: string;
    /** 企业地址（提交审核必填） */
    enterpriseAddress?: string;
    /** 营业执照编号（提交审核必填） */
    businessLicenseNo?: string;
    /** 营业执照是否长期有效 0 否 / 1 是（提交审核必填） */
    licenseLongTerm?: number;
    /**
     * 营业执照有效期开始 yyyy-MM-dd（前端预留；后端字段就绪前不提交）
     * licenseLongTerm=0 时与 licenseValidTo 组成区间
     */
    licenseValidFrom?: string;
    /** 营业执照有效期截止 yyyy-MM-dd（licenseLongTerm=0 时必填） */
    licenseValidTo?: string;
    /** 注册/成立日期 yyyy-MM-dd（选填） */
    establishDate?: string;
    /** 发证登记机关（选填） */
    licenseAuthority?: string;
    /** 核准日期 yyyy-MM-dd（选填） */
    licenseApprovalDate?: string;
    /** 经营范围（选填） */
    businessScope?: string;

    // ===== 法人/经营者信息（文档 5.1，提交审核必填） =====
    /** 法人/经营者姓名（提交审核必填） */
    legalPersonName?: string;
    /** 法人/经营者身份证号（明文存储，回显完整值供编辑；提交审核必填） */
    legalPersonIdCard?: string;
    /** 法人/经营者手机号（提交审核必填） */
    legalPersonPhone?: string;

    [property: string]: any;
}

// ===== 门店（自营）美团配送配置详情（文档 5.1 含只读字段） =====
export interface MeituanShopConfigDetail extends MeituanShopConfigSaveParam {
    /** 注册后美团门店 ID */
    mtShopId?: string;
    /** 门店状态码：0 未注册 … 40 上线可发单；10/50 驳回 */
    mtShopStatus?: number;
    /** 状态文案 */
    mtShopStatusName?: string;
    /** 是否已上线可发单（状态 40） */
    canDispatch?: boolean;
    /** 审核备注 / 驳回原因 */
    auditRemark?: string;
}

/** 美团门店状态归类：前端四态展示 */
export type MeituanShopStage = "unsubmitted" | "auditing" | "failed" | "online";
