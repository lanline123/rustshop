/**
 * 后台消息语音播报相关类型定义
 *
 * @description 服务于 D1084-adminMsgTts 迭代，提供 5 类一级业务消息的语音播报能力
 * @see .ai-os/specs/admin-msg-tts.spec.md
 */

/** 5 类一级业务消息的播报类型枚举 */
export type VoiceMsgType =
    | "newOrder"
    | "paidOrder"
    | "aftersale"
    | "cancelOrder"
    | "customerMsg";

/**
 * SSE payload 中与语音播报相关的字段
 *
 * 播报判断基于"前后两帧计数差值 (delta>0)"，要求计数单调递增（固定 startTime 下成立）：
 * - newOrderMsgCount / paidOrderMsgCount / aftersaleMsgCount / cancelOrderMsgCount：
 *   后端 v4 新增，来源为 admin_msg 表（msgType 11/12/32/31），与消息中心同源、同过滤口径，
 *   是当前 4 类业务消息播报的实际数据源
 * - customerMsgCount：客服消息单调累计字段（IM 旁路），专供客服播报使用
 * - imMsgCount：客服未读数，可随已读回落，仅供 UI 角标，不用于播报
 * - orderCount / paidOrderCount / aftersaleCount / cancelOrderCount：v4 前的业务表来源字段，
 *   后端仍下发（红点/兼容），播报不再消费
 * adapter 用 `field in next` 检查，缺失时视为 0 / 不参与对比
 */
export interface VoiceBroadcastSSEPayload {
    newOrderMsgCount?: number;
    paidOrderMsgCount?: number;
    aftersaleMsgCount?: number;
    cancelOrderMsgCount?: number;
    orderCount?: number;
    paidOrderCount?: number;
    aftersaleCount?: number;
    cancelOrderCount?: number;
    customerMsgCount?: number;
    imMsgCount?: number;
    [key: string]: any;
}

/** voiceBroadcast pinia store state 形状 */
export interface VoiceBroadcastSettings {
    /** 新订单播报开关 */
    newOrder: boolean;
    /** 已付款订单播报开关 */
    paidOrder: boolean;
    /** 售后申请播报开关 */
    aftersale: boolean;
    /** 订单取消播报开关 */
    cancelOrder: boolean;
    /** 客服消息播报开关 */
    customerMsg: boolean;
    /** 浏览器是否已激活语音权限（用户已点过「立即开启」） */
    activated: boolean;
    /**
     * 弹窗显示开关（需求 #646738）
     *
     * 开启后：收到某类语音播报增量时，同步弹出「播报记录」抽屉并定位到对应类型 tab；
     * 关闭则仅发声、不弹窗。默认关闭（用户按需在设置抽屉底部开启）。
     */
    popupEnabled: boolean;
}

/**
 * 单条商品摘要（播报卡片渲染：缩略图 + 名称 + 单价 × 数量）
 *
 * 来源为后端语音播报专用列表 msg/adminMsg/broadcastList 的批量聚合结果，
 * 订单类取订单商品、售后类取售后商品，避免前端逐条拉取订单/售后详情。
 */
export interface VoiceBroadcastProduct {
    picThumb?: string;
    productName?: string | number;
    price?: number | string;
    quantity?: number | string;
    /** 供应商订单商品下单时的供货价快照 */
    supplyPrice?: number | string;
    /** 供货价小计 */
    supplySubtotal?: number | string;
}

/**
 * 单条播报记录（复用消息中心 admin_msg 列表项，需求 #646738）
 *
 * 基础字段来自 admin_msg；商品摘要与金额字段来自语音播报专用列表
 * msg/adminMsg/broadcastList 的一次性批量聚合（订单/售后商品 + 金额），
 * 使卡片无需再逐条请求订单/售后详情。
 */
export interface VoiceBroadcastRecord {
    msgId: number;
    msgType: number;
    title?: string;
    /** 富文本 HTML（后端消息模板生成），卡片 v-html 渲染 */
    content?: string;
    sendTime?: string;
    isReaded?: number;
    msgLink?: string;
    /** 商品摘要列表（broadcastList 聚合，订单/售后统一口径） */
    products?: VoiceBroadcastProduct[];
    /** 订单总金额（订单类消息） */
    totalAmount?: number | string;
    /** 订单供货价合计（订单类消息，供应商端展示） */
    supplyTotalAmount?: number | string;
    /** 配送费/运费（订单类消息） */
    shippingFee?: number | string;
    /** 配送类型 ID（前端据此判断运费展示名称） */
    shippingTypeId?: number;
    /** 退款金额（售后类消息） */
    refundAmount?: number | string;
    /** 跳转数据信息：订单/售后等详情 id 从此取（与消息中心 DetailBtn 同源）；客服消息记录用 conversationId 跳工作台 */
    relatedData?: {
        orderId?: number;
        aftersaleId?: number;
        orderSn?: string;
        aftersalesSn?: string;
        productId?: number;
        vendorProductId?: number;
        /** 客服消息会话 ID（点击卡片跳客服工作台并定位会话，需求 #646738） */
        conversationId?: number;
        [key: string]: any;
    };
    [key: string]: any;
}

/** 语音播报专用列表返回（broadcastList） */
export interface VoiceBroadcastRecordListResult {
    records: VoiceBroadcastRecord[];
    total: number;
}

/** VoiceMsgType → SSE 字段名 映射（v4：4 类业务消息改读 admin_msg 来源字段） */
export interface VoiceMsgFieldMap {
    newOrder: "newOrderMsgCount";
    paidOrder: "paidOrderMsgCount";
    aftersale: "aftersaleMsgCount";
    cancelOrder: "cancelOrderMsgCount";
    customerMsg: "customerMsgCount";
}
