<!--
/**
 * @file 语音播报记录卡片
 * @description 播报记录抽屉内的单条记录卡片（需求 #646738）
 *  - 展示：类型图标 + 标题 + 时间；内容摘要
 *  - 订单/取消/已付款/售后类：挂载时按 relatedData 拉取订单/售后详情，结构化展示商品
 *    （缩略图 + 名称 + 单价 × 数量 + 订单金额/退款金额），与消息中心 Collapse 同源口径；
 *    整卡点击打开对应详情抽屉（复用 DialogForm，与消息中心 DetailBtn 同源）
 *  - 客服消息类：content 为纯文本摘要，点击整卡新窗口跳客服工作台并定位会话
 * @see Collapse.vue（订单/售后详情结构化渲染同源）
 * @see DetailBtn.vue（msgType→详情路径映射同源）
 */
-->
<template>
    <div class="vb-record-card" :class="{ 'is-clickable': !!detailConfig || !!imHref, unread: item.isReaded === 0 }">
        <component :is="rootTag" ref="triggerRef" class="vb-record-trigger" v-bind="rootProps">
            <div class="vb-record-inner">
                <div class="vb-record-head">
                    <span class="vb-record-title">
                        <component :is="meta.icon" class="vb-record-type-icon" :style="{ color: meta.color }" />
                        <span class="vb-record-title-text">{{ titleText }}</span>
                    </span>
                    <span class="vb-record-time">{{ item.sendTime }}</span>
                </div>

                <!-- 摘要行：订单号/金额等有效信息（content 去除「请注意查看」后） -->
                <div v-if="summaryText" class="vb-record-summary">{{ summaryText }}</div>

                <!-- 结构化商品块：随列表接口一次性带出，无需逐条拉详情 -->
                <template v-if="productList.length">
                    <div class="vb-order-items">
                        <div v-for="(product, idx) in productList" :key="idx" class="vb-order-item">
                            <div class="vb-order-item__info">
                                <img class="vb-order-item__img" :src="imageFormat(product.picThumb)" alt="" />
                                <span class="vb-order-item__name">{{ product.productName }}</span>
                            </div>
                            <div class="vb-order-item__meta">
                                <span v-if="product.price !== undefined && product.price !== ''" class="vb-order-item__price">¥{{ itemPriceText(product) }}</span>
                                <span v-if="product.quantity !== undefined && product.quantity !== ''" class="vb-order-item__qty">x{{ product.quantity }}</span>
                            </div>
                        </div>
                    </div>

                    <div v-if="orderTotal" class="vb-order-total">
                        订单金额：¥{{ orderTotal.amount }}
                        <span class="vb-order-total__fee">(含{{ orderTotal.feeLabel }}：{{ orderTotal.fee }})</span>
                    </div>
                    <div v-else-if="aftersaleRefund !== null" class="vb-order-total">退款金额：¥{{ aftersaleRefund }}</div>
                </template>
            </div>
        </component>
    </div>
</template>

<script setup lang="ts">
import { computed, markRaw, ref } from "vue";
import { useRouter } from "vue-router";
import {
    ShoppingOutlined,
    CheckCircleOutlined,
    ExclamationCircleOutlined,
    CloseCircleOutlined,
    CustomerServiceOutlined
} from "@ant-design/icons-vue";
import { DialogForm } from "@/components/dialog";
import { imageFormat, priceFormat } from "@/utils/format";
import { getOrderShippingFeeLabel } from "@/utils/orderShippingFee";
import { getSupplierOrderItemPrice, getSupplierOrderItemSubtotal } from "@/utils/orderSupplierPrice";
import type { VoiceBroadcastProduct, VoiceBroadcastRecord, VoiceMsgType } from "@/types/voiceBroadcast.d";

const router = useRouter();
const isVendorAdmin = localStorage.getItem("adminType") === "vendor";

const props = defineProps<{
    item: VoiceBroadcastRecord;
    /** 所属播报类型（决定图标/配色，与设置抽屉一致） */
    voiceType: VoiceMsgType;
}>();

interface VoiceMeta {
    label: string;
    icon: any;
    color: string;
}

const META_MAP: Record<VoiceMsgType, VoiceMeta> = {
    newOrder: { label: "新订单通知", icon: markRaw(ShoppingOutlined), color: "#14b8a6" },
    paidOrder: { label: "已付款订单通知", icon: markRaw(CheckCircleOutlined), color: "#1456f0" },
    aftersale: { label: "售后申请通知", icon: markRaw(ExclamationCircleOutlined), color: "#f5222d" },
    cancelOrder: { label: "订单取消通知", icon: markRaw(CloseCircleOutlined), color: "#fa8c16" },
    customerMsg: { label: "客服消息", icon: markRaw(CustomerServiceOutlined), color: "#06b6d4" }
};

const meta = computed<VoiceMeta>(() => META_MAP[props.voiceType]);

/**
 * 卡片标题：固定为类型名（如「新订单通知」），不展示订单号/金额/商品等信息（那些放到下方摘要与商品块）
 *  - 订单/售后类后端 title 常含订单号+金额（如「订单已被取消：xxx,金额：xxx」），此处统一用 meta.label
 *  - 客服消息 title 为「xxx 发来消息」，有意义，保留
 */
const titleText = computed(() => {
    if (props.voiceType === "customerMsg") return props.item.title || meta.value.label;
    return meta.value.label;
});

/** 订单类 msgType（详情映射到订单详情抽屉，与消息中心 DetailBtn 同源） */
const ORDER_MSG_TYPES = [11, 12, 13, 31, 54];

/**
 * 待展示商品行：直接取列表接口一次性带出的 products（订单/售后统一口径），
 * 不再逐条拉取订单/售后详情。
 */
const productList = computed(() => props.item.products ?? []);

/** 供应商端展示供货价，其余端展示后端返回的商品价 */
const itemPriceText = (product: VoiceBroadcastProduct) =>
    isVendorAdmin ? priceFormat(getSupplierOrderItemPrice(product)) : product.price;

/** 订单金额：供应商端取供货价合计，其余端取订单总金额 */
const orderAmount = computed<number | string | undefined>(() => {
    if (!isVendorAdmin) return props.item.totalAmount;

    const supplyTotalAmount = props.item.supplyTotalAmount;
    if (supplyTotalAmount !== undefined && supplyTotalAmount !== null && supplyTotalAmount !== "") {
        return supplyTotalAmount;
    }
    if (productList.value.length === 0) return undefined;
    return priceFormat(productList.value.reduce((total, product) => total + getSupplierOrderItemSubtotal(product), 0));
});

/**
 * content 纯文本摘要（订单号/金额那行）
 *  - 去 HTML 标签（后端 content 为纯文本，防御性处理）
 *  - 去掉「请注意查看」这类无效提示及其前后标点，仅保留订单号/金额等有效信息
 */
const summaryText = computed(() => {
    let text = (props.item.content || "").replace(/<[^>]+>/g, "").trim();
    text = text.replace(/[，,。．.\s]*请注意查看[。．.\s]*/g, "").trim();
    text = text.replace(/[，,。．.\s]+$/g, "").trim();
    // 摘要未包含金额且列表已带出订单金额时，补充金额展示
    const amount = orderAmount.value;
    if (text && amount !== undefined && amount !== null && amount !== "" && !/金额/.test(text)) {
        text = `${text}，金额：${amount}`;
    }
    return text;
});

/** 订单金额（含运费/配送费口径与消息中心一致），来源为列表接口聚合字段 */
const orderTotal = computed(() => {
    const amount = orderAmount.value;
    if (amount === undefined || amount === null || amount === "") return null;
    return {
        amount,
        feeLabel: getOrderShippingFeeLabel({ shippingTypeId: props.item.shippingTypeId }),
        fee: props.item.shippingFee ?? 0
    };
});

/** 售后退款金额（列表接口聚合字段） */
const aftersaleRefund = computed<number | string | null>(() => {
    const refund = props.item.refundAmount;
    return refund === undefined || refund === null || refund === "" ? null : refund;
});

/**
 * 客服消息卡片跳转地址：新窗口打开客服工作台并定位会话（需求 #646738）
 * 仅客服消息类型且携带 conversationId 时可点击；工作台 onMounted 读 query.id 自动打开会话。
 */
const imHref = computed<string | null>(() => {
    if (props.voiceType !== "customerMsg") return null;
    const conversationId = props.item.relatedData?.conversationId;
    if (!conversationId) return null;
    return router.resolve({ path: "/im/index", query: { id: conversationId } }).href;
});

/**
 * 详情弹窗配置：按 msgType 映射到订单/售后详情抽屉（与消息中心 DetailBtn 同源口径）
 *  - 订单类 11/12/13/31/54 → order/order/Info
 *  - 售后 32 → order/aftersales/Info
 *  - 其余（含客服消息 IM 来源）暂无详情抽屉 → 卡片不可点击
 */
interface DetailConfig {
    path: string;
    id: number | null | undefined;
    title: string;
    width: string;
}

const detailConfig = computed<DetailConfig | null>(() => {
    const item = props.item;
    const related = item.relatedData || {};
    if (ORDER_MSG_TYPES.includes(item.msgType)) {
        const orderId = related.orderId ?? item.orderId;
        if (!orderId) return null;
        const orderSn = related.orderSn ?? "";
        return { path: "order/order/Info", id: orderId, title: `订单详情 ${orderSn}`.trim(), width: "880px" };
    }
    if (item.msgType === 32) {
        const aftersaleId = related.aftersaleId;
        if (!aftersaleId) return null;
        const sn = related.aftersalesSn ?? "";
        return { path: "order/aftersales/Info", id: aftersaleId, title: `售后详情 ${sn}`.trim(), width: "800px" };
    }
    return null;
});

/** 传给 DialogForm 的属性（抽屉模式，仅详情不显示底部按钮） */
const dialogProps = computed(() => {
    const cfg = detailConfig.value!;
    return {
        params: { act: "detail", id: cfg.id },
        title: cfg.title,
        path: cfg.path,
        width: cfg.width,
        isDrawer: true,
        showClose: false,
        showOnOk: false,
        // DialogForm 触发器默认 style 为 inline-flex（会覆盖 .vb-record-card 的 display:block/width:100%），
        // 显式改为块级铺满，避免卡片按内容宽度收缩而超出抽屉、导致按钮/内容错位
        style: "display:block;width:100%;"
    };
});

/**
 * 卡片触发器根标签与属性：
 *  - 订单/售后类 → DialogForm（点击开详情抽屉）
 *  - 客服消息类 → a 标签（新窗口跳客服工作台）
 *  - 其余 → 普通 div（不可点击）
 */
const rootTag = computed(() => {
    if (detailConfig.value) return DialogForm;
    if (imHref.value) return "a";
    return "div";
});

const rootProps = computed(() => {
    if (detailConfig.value) return dialogProps.value;
    if (imHref.value) return { href: imHref.value, target: "_blank" };
    return {};
});

/** 触发器组件引用（订单/售后类为 DialogForm，暴露 show()） */
const triggerRef = ref();

/**
 * 程序化展开本条记录详情（供右上角弹窗提示点击后自动打开，需求 #646738 弹窗改造）
 *
 * 仅订单/售后类（detailConfig 存在，根组件为 DialogForm）有详情弹窗可展开；
 * 客服消息类为新窗口跳转、无详情弹窗，调用时静默忽略。
 */
const openDetail = () => {
    if (!detailConfig.value) return;
    triggerRef.value?.show?.();
};

defineExpose({ openDetail });
</script>

<style scoped lang="less">
.vb-record-card {
    display: block;
    width: 100%;
    box-sizing: border-box;
    background: #fff;
    border-bottom: 1px solid #eef0f3;
    transition: background 0.2s ease;

    &:last-child {
        border-bottom: none;
    }

    &:hover {
        background: #f7f8fa;
    }

    &.is-clickable {
        cursor: pointer;
    }
}

/* DialogForm 触发器默认 inline-flex，降为块级铺满整卡，使点击区域=整张卡片；作为 a 标签时去掉链接样式 */
.vb-record-trigger {
    display: block;
    width: 100%;
    color: inherit;
    text-decoration: none;
}

.vb-record-inner {
    padding: 16px;
    min-width: 0;
}

.vb-record-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;
}

.vb-record-title {
    display: flex;
    align-items: center;
    min-width: 0;
    flex: 1;

    .vb-record-type-icon {
        font-size: 14px;
        margin-right: 6px;
        flex-shrink: 0;
    }

    .vb-record-title-text {
        font-size: 14px;
        font-weight: 500;
        color: #1f2329;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
}

.vb-record-time {
    font-size: 12px;
    color: rgba(0, 0, 0, 0.45);
    flex-shrink: 0;
    margin-left: 8px;
}

/* 摘要文本（订单号/金额），加粗展示，最多两行省略 */
.vb-record-summary {
    font-size: 14px;
    line-height: 20px;
    font-weight: 600;
    color: #1f2329;
    word-break: break-all;
    overflow-wrap: anywhere;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.vb-record-detail-tip {
    margin-top: 10px;
    font-size: 12px;
    color: #a8abb2;
}

/* 结构化商品块 */
.vb-order-items {
    margin-top: 10px;
}

.vb-order-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid #f2f3f5;

    &:last-child {
        border-bottom: none;
    }

    &__info {
        display: flex;
        align-items: center;
        min-width: 0;
        flex: 1;
        margin-right: 12px;
    }

    &__img {
        width: 56px;
        height: 56px;
        object-fit: cover;
        border-radius: 6px;
        flex-shrink: 0;
        background: #f5f5f5;
    }

    &__name {
        margin-left: 10px;
        font-size: 13px;
        line-height: 18px;
        color: #1f2329;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
    }

    &__meta {
        flex-shrink: 0;
        text-align: right;
        font-size: 13px;
        line-height: 18px;
        color: #4e5969;
    }

    &__price {
        display: block;
    }

    &__qty {
        display: block;
        color: #86909c;
    }
}

.vb-order-total {
    margin-top: 12px;
    text-align: right;
    font-size: 13px;
    color: #1f2329;

    &__fee {
        color: #86909c;
    }
}
</style>
