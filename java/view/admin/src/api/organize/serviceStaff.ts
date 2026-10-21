/**
 * 服务人员统一 API
 * 后端接口路径: /o2o/serviceStaff/*
 * 配送员和服务员通过 type 字段区分: 1-配送员, 2-服务员
 */
import request from "@/utils/request";

// ===== 类型定义 =====

/** 人员类型 */
export type ServiceStaffType = 1 | 2;

/** 人员类型枚举 */
export const ServiceStaffTypeEnum = {
    /** 配送员（同城配送） */
    DELIVERYMAN: 1 as ServiceStaffType,
    /** 服务员（预约上门） */
    SERVICEMAN: 2 as ServiceStaffType
} as const;

/** 账号状态 */
export type ServiceStaffStatus = 0 | 1;

/** 账号状态枚举 */
export const ServiceStaffStatusEnum = {
    /** 禁用 */
    DISABLED: 0 as ServiceStaffStatus,
    /** 启用 */
    ENABLED: 1 as ServiceStaffStatus
} as const;

/** 工作状态（后端定义） */
export type WorkStatus = 0 | 1 | 2;

/** 工作状态枚举（后端定义） */
export const WorkStatusEnum = {
    /** 已下班 */
    OFFLINE: 0 as WorkStatus,
    /** 空闲中 */
    IDLE: 1 as WorkStatus,
    /** 配送中/服务中 */
    PROCESSING: 2 as WorkStatus
} as const;

/** 会员信息 */
export interface MemberInfo {
    userId: number;
    username: string;
    nickname: string;
    avatar: string;
    mobile: string;
}

/** 服务人员列表查询参数 (后端) */
export interface ServiceStaffPageQuery {
    page?: number;
    size?: number;
    storeId?: number;
    type?: number;
    status?: ServiceStaffStatus;
    workStatus?: WorkStatus;
    keyword?: string;
    searchStartTime?: string;
    searchEndTime?: string;
}

/** 服务人员VO (后端返回) */
export interface ServiceStaffVO {
    id: number;
    name: string;
    nickname: string;
    avatar: string;
    phone: string;
    memberId: number;
    storeId: number;
    type: ServiceStaffType;
    typeName: string;
    status: ServiceStaffStatus;
    statusName: string;
    workStatus: WorkStatus;
    workStatusName: string;
    completedOrderCount: number;
    completedOrderAmount: number;
    totalIncome: number;
    createTime: string;
    updateTime?: string;
    storeName: string;
    memberInfo?: MemberInfo;
}

/** 分页响应 */
export interface PageResult<T> {
    total: number;
    size: number;
    current: number;
    records: T[];
}

/** 创建服务人员参数 */
export interface ServiceStaffCreateDTO {
    name: string;
    nickname?: string;
    avatar?: string;
    phone: string;
    memberId?: number;
    storeId?: number;
    type: ServiceStaffType;
    status?: ServiceStaffStatus;
}

/** 更新服务人员参数 */
export interface ServiceStaffUpdateDTO {
    id: number;
    name?: string;
    nickname?: string;
    avatar?: string;
    phone?: string;
    memberId?: number;
    storeId?: number;
    type?: ServiceStaffType;
    status?: ServiceStaffStatus;
}

/** 批量操作参数 */
export interface BatchDTO {
    ids: number[];
    type: string;
}

/** 更新字段参数 */
export interface UpdateFieldDTO {
    id: number;
    field: "status" | "workStatus";
    val: number;
}

// ===== API 函数 =====

const PREFIX = "o2o/serviceStaff";

/**
 * 获取服务人员列表
 */
export const getServiceStaffList = (params: ServiceStaffPageQuery) => {
    return request<PageResult<ServiceStaffVO>>({
        url: `${PREFIX}/list`,
        method: "get",
        params
    });
};

/**
 * 获取服务人员详情
 */
export const getServiceStaffDetail = (params: { id: number }) => {
    return request<ServiceStaffVO>({
        url: `${PREFIX}/detail`,
        method: "get",
        params
    });
};

/**
 * 查看手机号（明文）
 * 说明：点击展示手机号时调用，参数与详情接口一致（传 id）
 */
export interface ShowMobileResult {
    /** 明文手机号（不同后端可能返回 mobile 或 phone 字段） */
    mobile?: string;
    phone?: string;
}
export const showMobile = (params: { id: number }) => {
    return request<ShowMobileResult | string>({
        url: `${PREFIX}/showMobile`,
        method: "get",
        params
    });
};

/**
 * 创建服务人员
 */
export const createServiceStaff = (data: ServiceStaffCreateDTO) => {
    return request({
        url: `${PREFIX}/create`,
        method: "post",
        data
    });
};

/**
 * 更新服务人员
 */
export const updateServiceStaff = (data: ServiceStaffUpdateDTO) => {
    return request({
        url: `${PREFIX}/update`,
        method: "post",
        data
    });
};

/**
 * 删除服务人员
 */
export const deleteServiceStaff = (data: { id: number }) => {
    return request({
        url: `${PREFIX}/del`,
        method: "post",
        data
    });
};

/**
 * 批量操作服务人员
 */
export const batchServiceStaff = (data: BatchDTO) => {
    return request({
        url: `${PREFIX}/batch`,
        method: "post",
        data
    });
};

/**
 * 更新指定字段（如状态切换）
 */
export const updateServiceStaffField = (data: UpdateFieldDTO) => {
    return request({
        url: `${PREFIX}/updateField`,
        method: "post",
        data
    });
};

// ===== 前端统一查询参数类型（用于列表页面） =====

/** 前端列表查询参数 */
export interface StaffFilterParams {
    page: number;
    size: number;
    keyword?: string;
    storeId?: number;
    status?: ServiceStaffStatus;
    workStatus?: WorkStatus;
    startDate?: string;
    endDate?: string;
}

/** 列表返回结果 */
export interface StaffFilterResult {
    records: ServiceStaffVO[];
    total: number;
}

/** 表单数据 */
export interface StaffFormData {
    id?: number;
    memberId?: number;
    memberUsername?: string;
    memberMobile?: string;
    name: string;
    phone: string;
    storeId?: number;
    status: ServiceStaffStatus;
}

/** 订单记录查询参数 */
export interface StaffOrderListParams {
    id: number;
    page: number;
    size: number;
    startDate?: string;
    endDate?: string;
    orderStatus?: number;
}

/** 订单记录项 */
export interface StaffOrderItem {
    orderId: number;
    orderSn: string;
    orderStatus: number;
    orderStatusName: string;
    totalAmount: number;
    createTime: string;
    completeTime?: string;
}

/** 订单记录返回结果 */
export interface StaffOrderListResult {
    records: StaffOrderItem[];
    total: number;
}

// ===== 便捷函数 =====

/**
 * 获取服务人员列表（统一入口）
 * @param type 人员类型：1-配送员, 2-服务员
 */
export const getStaffList = async (type: number, params: StaffFilterParams): Promise<StaffFilterResult> => {
    const query: ServiceStaffPageQuery = {
        page: params.page,
        size: params.size,
        storeId: params.storeId,
        type,
        status: params.status,
        workStatus: params.workStatus,
        keyword: params.keyword,
        searchStartTime: params.startDate,
        searchEndTime: params.endDate
    };

    const result = await getServiceStaffList(query);

    return {
        records: result.records,
        total: result.total
    };
};

/**
 * 创建服务人员（统一入口）
 */
export const createStaff = async (type: ServiceStaffType, data: StaffFormData) => {
    const createData: ServiceStaffCreateDTO = {
        name: data.name,
        phone: data.phone,
        memberId: data.memberId,
        storeId: data.storeId,
        type,
        status: data.status
    };
    return createServiceStaff(createData);
};

/**
 * 更新服务人员（统一入口）
 */
export const updateStaff = async (data: StaffFormData) => {
    if (!data.id) throw new Error("人员ID不能为空");

    const updateData: ServiceStaffUpdateDTO = {
        id: data.id,
        name: data.name,
        phone: data.phone,
        memberId: data.memberId,
        storeId: data.storeId,
        status: data.status
    };
    return updateServiceStaff(updateData);
};

/**
 * 切换账号状态（启用/禁用）
 */
export const toggleStaffStatus = (data: { id: number; val?: number }) => {
    return updateServiceStaffField({
        id: data.id,
        field: "status",
        val: data.val ?? 0
    });
};

/**
 * 获取订单记录
 * 注意：后端接口文档中未提供此接口，暂时保持空实现
 */
export const getStaffOrderList = (params: StaffOrderListParams): Promise<StaffOrderListResult> => {
    console.warn("getStaffOrderList: 后端接口待确认");
    return Promise.resolve({
        records: [],
        total: 0
    });
};

// ===== 派送订单相关类型定义 =====

/** 派送订单查询参数 */
export interface DeliveryOrderPageQuery {
    page?: number;
    size?: number;
    shopId?: number;
    type?: 1 | 2; // 1-配送订单，2-服务订单
    status?: number; // 10-待派发，20-待取货/待服务，30-配送中/服务中，40-已完成，50-已取消
    /** 排除某个状态（例如：10-待派发） */
    notIncludeStatus?: number;
    dispatchMode?: 1 | 2; // 1-抢单模式，2-手动派单
    staffId?: number;
    keyword?: string;
    searchStartTime?: string;
    searchEndTime?: string;
}

/** 派送订单VO */
export interface DeliveryOrderOrderInfo {
    /** 订单备注（买家留言） */
    buyerNote?: string;
    /** 商品数量 */
    productQuantity?: number;
    /** 订单商品总价 */
    totalAmount?: number;
}

export interface DeliveryOrderVO {
    deliveryOrderId: number;
    deliveryOrderSn: string;
    orderId: number;
    orderSn: string;
    shopId: number;
    shopName: string;
    type: 1 | 2;
    typeName: string;
    dispatchMode: 1 | 2;
    dispatchModeName: string;
    status: number;
    statusName: string;
    staffId: number;
    staffName: string;
    staffPhone: string;
    consignee: string;
    mobile: string;
    address: string;
    orderAmount: number;
    createTime: string;
    dispatchTime: string | null;
    pickupTime: string | null;
    completeTime: string | null;
    // 扩展字段（可能来自商城订单）
    productCount?: number;
    totalAmount?: number;
    payAmount?: number;
    /** 兼容字段：客户备注（旧字段） */
    customerRemark?: string;
    /** 商城订单信息（部分接口会挂载在此字段） */
    orderInfo?: DeliveryOrderOrderInfo;
}

/** 手动派单参数 */
export interface DeliveryOrderDispatchDTO {
    /** 派送订单ID */
    orderId: number;
    /** 配送员/服务员ID */
    staffId: number;
}

const DELIVERY_ORDER_PREFIX = "o2o/deliveryOrder";

/**
 * 获取派送订单列表
 */
export const getDeliveryOrderList = (params: DeliveryOrderPageQuery) => {
    return request<PageResult<DeliveryOrderVO>>({
        url: `${DELIVERY_ORDER_PREFIX}/list`,
        method: "get",
        params
    });
};

/**
 * 手动派单接口
 */
export const dispatchDeliveryOrder = (data: DeliveryOrderDispatchDTO) => {
    return request({
        url: `${DELIVERY_ORDER_PREFIX}/dispatch`,
        method: "post",
        data
    });
};

/**
 * 根据配送员/服务员ID获取派送订单列表
 */
export const getStaffDeliveryOrderList = async (params: {
    staffId: number;
    page: number;
    size: number;
    type?: 1 | 2;
}): Promise<{ records: DeliveryOrderVO[]; total: number }> => {
    const query: DeliveryOrderPageQuery = {
        page: params.page,
        size: params.size,
        staffId: params.staffId,
        type: params.type
    };

    const result = await getDeliveryOrderList(query);

    return {
        records: result.records,
        total: result.total
    };
};
