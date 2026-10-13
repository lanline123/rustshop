import request from "@/utils/request";
import { OrderFilterResult, OrderFormState, OrderItem, OrderPrintFormResult, OrderPrintBillFormResult, OrderStatusCount } from "@/types/order/order.d";

/**
 * 获取订单页面配置
 * @description 获取订单列表页的配置项
 * @returns 页面配置
 */
export const getOrderPageConfig = () => {
    return request<{isChangeOrderStatus:number}>({
        url: "order/order/getOrderPageConfig",
        method: "get"
    });
};
/**
 * 获取订单列表
 * @description 分页获取订单数据，支持多条件筛选
 * @param params - 筛选和分页参数
 * @returns 订单列表
 */
export const getOrderList = (params: object) => {
    return request<OrderFilterResult>({
        url: "order/order/list",
        method: "get",
        params,
    });
};

/**
 * 获取订单各状态数量
 * @description 用于订单列表状态Tab角标，统计口径复用列表筛选条件
 * @param params - 与列表一致的筛选参数，不含 orderStatus
 * @returns 各状态订单数量
 */
export const getOrderStatusCount = (params: object) => {
    return request<OrderStatusCount>({
        url: "order/order/statusCount",
        method: "get",
        params,
    });
};

/**
 * 批量操作订单
 * @description 对订单进行批量操作（如批量发货、删除等）
 * @param type - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const batchSubmit = (type: string, data: object) => {
    return request({
        url: "order/order/batch",
        method: "post",
        data: {type, ...data},
    });
};
/**
 * 删除订单
 * @description 删除指定订单（仅限特定状态的订单）
 * @param data - 订单ID
 * @returns 操作结果
 */
export const delOrder = (data: object) => {
    return request({
        url: "order/order/delOrder",
        method: "post",
        data,
    });
};

/**
 * 更新订单字段
 * @description 更新订单的单个字段值
 * @param data - 字段更新数据
 * @returns 操作结果
 */
export const updateOrderFiled = (data: object) => {
    return request({
        url: "order/order/updateField",
        method: "post",
        data,
    });
};
/**
 * 获取订单详情
 * @description 获取订单的完整信息
 * @param action - 操作类型（detail等）
 * @param params - 订单ID等参数
 * @returns 订单详情
 */
export const getOrder = (action: string, params: object) => {
    return request<OrderFormState>({
        url: "order/order/" + action,
        method: "get",
        params,
    });
};

/**
 * 订单操作
 * @description 执行订单相关操作
 * @param action - 操作类型
 * @param data - 操作数据
 * @returns 操作结果
 */
export const operationOrder = (action: string, data: object) => {
    return request<OrderFormState>({
        url: "order/order/" + action,
        method: "post",
        data,
    });
};

/**
 * 扫码核销订单
 * @description 通过扫码核销到店自提订单
 * @param params - 核销参数
 * @returns 核销结果
 */
export const writeOffOrder = (params: object) => {
    return request<OrderFormState>({
        url: "order/order/writeOff",
        method: "post",
        params,
    });
};

/**
 * 获取配送信息
 * @description 获取订单的配送/物流相关信息
 * @param action - 操作类型
 * @param params - 订单参数
 * @returns 配送信息
 */
export const getShipping = (action: string, params: object) => {
    return request<OrderFormState>({
        url: "order/order/" + action,
        method: "get",
        params,
    });
};
/**
 * 更新订单
 * @description 执行订单更新操作
 * @param operation - 操作类型
 * @param data - 更新数据
 * @returns 操作结果
 */
export const updateOrder = (operation: string, data: object) => {
    return request({
        url: "order/order/" + operation,
        method: "post",
        data,
    });
};

/**
 * 修改订单金额
 * @description 调整订单的商品金额或运费
 * @param data - 金额修改数据
 * @returns 操作结果
 */
export const modifyOrderMoney = (data: object) => {
    return request({
        url: "order/order/modifyMoney",
        method: "post",
        data,
    });
};

/**
 * 修改收货人信息
 * @description 修改订单的收货地址和联系方式
 * @param data - 收货人信息
 * @returns 操作结果
 */
export const modifyOrderConsignee = (data: Partial<OrderFormState> & { id: number }) => {
    return request({
        url: "order/order/modifyConsignee",
        method: "post",
        data,
    });
};

/**
 * 修改配送信息
 * @description 修改订单的配送方式
 * @param data - 配送信息
 * @returns 操作结果
 */
export const modifyOrderShipping = (data: object) => {
    return request({
        url: "order/order/modifyShipping",
        method: "post",
        data,
    });
};
/**
 * 获取待添加商品信息
 * @description 根据商品ID获取可添加到订单的商品信息
 * @param data - 商品ID列表
 * @returns 商品信息列表
 */
export const getAddProductInfoByIds = (data: object) => {
    return request<OrderItem[]>({
        url: "order/order/getAddProductInfo",
        method: "post",
        data,
    });
};

/**
 * 修改订单商品
 * @description 修改订单中的商品信息（数量、价格等）
 * @param data - 商品修改数据
 * @returns 操作结果
 */
export const modifyOrderProduct = (data: object) => {
    return request({
        url: "order/order/modifyProduct",
        method: "post",
        data,
    });
};

/**
 * 获取订单操作日志
 * @description 获取订单的操作历史记录
 * @param params - 订单ID等参数
 * @returns 操作日志列表
 */
export const getOrderLogList = (params: object) => {
    return request<OrderFilterResult>({
        url: "order/orderLog/list",
        method: "get",
        params,
    });
};

/**
 * 添加订单操作日志
 * @description 手动添加订单操作记录
 * @param data - 日志内容
 * @returns 操作结果
 */
export const updateOrderLog = (data: object) => {
    return request({
        url: "order/orderLog/create",
        method: "post",
        data,
    });
};

/**
 * 添加商家备注
 * @description 为订单添加内部备注信息
 * @param data - 备注内容
 * @returns 操作结果
 */
export const setAdminNote = (data: object) => {
    return request({
        url: "order/order/setAdminNote",
        method: "post",
        data,
    });
};

/**
 * 确认订单
 * @description 商家确认订单，准备发货
 * @param data - 订单ID
 * @returns 操作结果
 */
export const setConfirmOrder = (data: object) => {
    return request({
        url: "order/order/setConfirm",
        method: "post",
        data,
    });
};
/**
 * 拆分店铺订单
 * @description 将跨店铺订单拆分为多个子订单
 * @param data - 拆分参数
 * @returns 操作结果
 */
export const splitStoreOrder = (data: object) => {
    return request({
        url: "order/order/splitStoreOrder",
        method: "post",
        data,
    });
};

/**
 * 设置订单已支付
 * @description 手动标记订单为已支付状态
 * @param data - 订单ID
 * @returns 操作结果
 */
export const setPaidOrder = (data: object) => {
    return request({
        url: "order/order/setPaid",
        method: "post",
        data,
    });
};

/**
 * 获取打印订单数据
 * @description 获取订单打印所需的数据
 * @param params - 订单ID列表
 * @returns 打印数据
 */
export const getOrderPrint = (params: object) => {
    return request<OrderPrintFormResult[]>({
        url: "order/order/printSeveral",
        method: "get",
        params,
    });
};

/**
 * 获取电子面单打印数据
 * @description 获取快递电子面单打印所需数据
 * @param params - 订单ID
 * @returns 电子面单数据
 */
export const getOrderPrintBill = (params: object) => {
    return request<OrderPrintBillFormResult>({
        url: "order/order/orderPrintBill",
        method: "get",
        params,
    });
};

/**
 * 订单发货
 * @description 为订单填写物流信息并发货
 * @param data - 发货信息（快递公司、单号等）
 * @returns 操作结果
 */
export const orderDeliver = (data: object) => {
    return request({
        url: "order/order/deliver",
        method: "post",
        data,
    });
};

/**
 * 批量发货
 * @description 批量为多个订单发货
 * @param data - 批量发货数据
 * @returns 操作结果
 */
export const orderDeliverBatch = (data: object) => {
    return request({
        url: "order/order/batch",
        method: "post",
        data,
    });
};

// 下载批量发货模板
export const downloadDeliverTemplate = (ids?: number[]) => {
    return request({
        url: "order/order/downloadDeliverTemplate",
        method: "get",
        params: ids && ids.length > 0 ? { ids } : {},
        responseType: "blob"
    });
};

// 批量发货Excel导入
export const batchDeliverImport = (data: FormData) => {
    return request({
        url: "order/order/batchDeliverImport",
        method: "post",
        data,
        headers: {
            "Content-Type": "multipart/form-data"
        }
    });
};

// 获取批量发货导入失败数据
export const getBatchDeliverImportErrorData = (data: FormData) => {
    return request({
        url: "order/order/getBatchDeliverImportErrorData",
        method: "post",
        data,
        responseType: "blob",
        headers: {
            "Content-Type": "multipart/form-data"
        }
    });
};

/**
 * 获取多个订单详情
 * @description 批量获取多个订单的详细信息
 * @param params - 订单ID列表
 * @returns 订单详情列表
 */
export const getSeveralDetail = (params: object) => {
    return request<any>({
        url: "order/order/severalDetail",
        method: "get",
        params,
    });
};

/**
 * 获取物流信息
 * @description 获取订单的物流轨迹信息
 * @param params - 订单ID
 * @returns 物流轨迹
 */
export const getShippingInfo = (params: object) => {
    return request<any>({
        url: "order/order/shippingInfo",
        method: "get",
        params,
    });
};

/**
 * 修改订单状态
 * @description 手动修改订单的状态
 * @param data - 状态修改数据
 * @returns 操作结果
 */
export const changeOrderStatus = (data: object) => {
    return request({
        url: "order/order/changeOrderStatus",
        method: "post",
        data,
    });
};

/**
 * 催发货提醒
 * @description 向商家发送催发货提醒
 * @param params - 订单ID
 * @returns 操作结果
 */
export const remindDeliver = (params: object) => {
    return request({
        url: "order/order/remindDeliver",
        method: "post",
        params,
    });
};


/**
 * 订单核销
 * @description O2O场景下核销到店自提订单
 * @param params - 核销参数
 * @returns 核销结果
 */
export const orderWriteOff = (params: object) => {
    return request({
        url: "o2o/storeProduct/writeOff",
        method: "post",
        params,
    });
};
