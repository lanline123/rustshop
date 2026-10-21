<template>
    <div class="gift-card-detail">
        <div v-if="!loading && detail" class="detail-wrapper">
            <div class="section-card borded">
                <div class="section-title">{{ $t('礼品卡信息') }}</div>
                <div class="info-grid">
                    <div class="grid-item">
                        <span class="label">{{ $t('礼品卡类型：') }}</span>
                        <span class="value">{{ cardTypeLabel }}</span>
                    </div>
                    <div class="grid-item">
                        <span class="label">{{ $t('礼品卡形态：') }}</span>
                        <span class="value">{{ cardFormLabel }}</span>
                    </div>
                    <div class="grid-item">
                        <span class="label">{{ $t('卡号：') }}</span>
                        <span class="value">{{ formatValue(detail?.cardNumber) }}</span>
                    </div>
                    <div class="grid-item">
                        <span class="label">{{ $t('卡密：') }}</span>
                        <span class="value">{{ formatValue(detail?.cardPassword) }}</span>
                    </div>
                    <div class="grid-item">
                        <span class="label">{{ $t('是否售卖：') }}</span>
                        <span class="value">{{ detail?.cardType === 2 ? (detail?.isExchange === $t('是') ? $t('是') : "--") : booleanLabel(detail?.isSold) }}</span>
                    </div>
                    <div class="grid-item">
                        <span class="label">{{ $t('是否兑换：') }}</span>
                        <span class="value">{{ booleanLabel(detail?.isExchange) }}</span>
                    </div>
                </div>
            </div>
            <div v-if="detail?.isExchange === '是'" class="section-card borded">
                <div class="section-title">{{ $t('兑换信息') }}</div>
                <div class="info-block-list">
                    <div class="info-block">
                        <div class="block-title">{{ $t('收货人信息') }}</div>
                        <div :class="detail?.groupType === 2 ? 'block-content' : 'info-grid'">
                            <div v-for="item in receiverRows" :key="item.label" class="info-row">
                                <span class="label">{{ item.label }}：</span>
                                <span class="value">
                                    <MobileCard v-if="item.type === 'mobile'" :mobile="item.value"></MobileCard>
                                    <span v-else>{{ formatValue(item.value) }}</span>
                                </span>
                            </div>
                            <div v-if="detail?.groupType === 2" class="info-row">
                                <span class="label">{{ $t('订单明细：') }}</span>
                                <template v-if="detail?.orderId">
                                    <DialogForm
                                        isDrawer
                                        :title="$t('订单详情 {orderSn}', { orderSn: detail?.orderSn || '' })"
                                        width="880px"
                                        path="order/order/Info"
                                        :params="{ act: 'detail', id: detail?.orderId }"
                                    >
                                        <a class="btn-link">{{ $t('去查看') }}</a>
                                    </DialogForm>
                                </template>
                                <span v-else class="value">--</span>
                            </div>
                        </div>
                    </div>
                    <div v-if="showDeliveryInfo" class="info-block">
                        <div class="block-title">{{ $t('配送信息') }}</div>
                        <div class="block-content">
                            <div v-for="item in deliveryRows" :key="item.label" class="info-row">
                                <span class="label">{{ item.label }}：</span>
                                <span class="value">{{ formatValue(item.value) }}</span>
                            </div>
                        </div>
                    </div>
                    <div v-if="showPaymentInfo" class="info-block">
                        <div class="block-title">{{ $t('付款信息') }}</div>
                        <div class="block-content">
                            <div v-for="item in paymentRows" :key="item.label" class="info-row">
                                <span class="label">{{ item.label }}：</span>
                                <span class="value">{{ formatValue(item.value) }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import MobileCard from "@/components/list/src/MobileCard.vue";
import type { GiftCardDetail } from "@/types/promotion/giftCard";
import { getGiftCardDetailInfo } from "@/api/promotion/giftCard";
import { priceFormat } from "@/utils/format";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    groupType: {
        type: Number,
        default: 1
    },
    isDialog: {
        type: Boolean,
        default: false
    }
});

const router = useRouter();
const loading = ref<boolean>(true);
const detail = ref<GiftCardDetail>();

const giftCardsId = computed(() => (props.isDialog ? props.id : Number(router.currentRoute.value.query.id)));

const cardTypeLabel = computed(() => {
    if (detail.value?.groupTypeName) {
        return detail.value.groupTypeName;
    }
    if (detail.value?.groupType === 2) {
        return t('兑换卡');
    }
    if (detail.value?.groupType === 1) {
        return t('储值卡');
    }
    return "--";
});

const cardFormLabel = computed(() => {
    if (detail.value?.cardTypeName) {
        return detail.value.cardTypeName;
    }
    if (detail.value?.cardType === 2) {
        return t('实体卡');
    }
    if (detail.value?.cardType === 1) {
        return t('电子卡');
    }
    return "--";
});

const showDeliveryInfo = computed(() => detail.value?.groupType === 2 && !!detail.value?.exchangeInfo?.deliveryInfo);
const showPaymentInfo = computed(() => detail.value?.groupType === 2);
const isStoredValueCard = computed(() => detail.value?.groupType === 1);

const formatValue = (value?: string | number | boolean | null) => {
    if (value === null || value === undefined || value === "") {
        return "--";
    }
    return String(value);
};

const booleanLabel = (value?: string | number | boolean | null) => {
    if (value === t('是') || value === t('否')) {
        return value as string;
    }
    if (value === true || value === 1 || value === "1") {
        return t('是');
    }
    if (value === false || value === 0 || value === "0") {
        return t('否');
    }
    return formatValue(value);
};

const receiverRows = computed(() => {
    const info = detail.value?.exchangeInfo?.userAddressInfo;
    const address = [info?.regionNames, info?.address].filter(Boolean).join(" ");
    const rows: Array<{ label: string; value?: string; type?: string }> = [
        { label: t('会员名称'), value: info?.username },
        { label: t('收货人'), value: info?.consignee },
        { label: t('联系电话'), value: info?.mobile, type: "mobile" },
        { label: t('电子邮件'), value: info?.email },
        { label: t('收货人地址'), value: address || info?.address }
    ];
    if (info?.buyerNote) {
        rows.push({ label: t('订单备注'), value: info?.buyerNote });
    }
    if (isStoredValueCard.value) {
        rows.push({ label: t('兑换时间'), value: detail.value?.exchangeInfo?.payInfo?.exchangeTime });
        rows.push({
            label: t('兑换金额'),
            value: detail.value?.exchangeInfo?.payInfo?.exchangeAmount ? priceFormat(detail.value?.exchangeInfo?.payInfo?.exchangeAmount) : "--"
        });
        return rows.filter((row) => ![t('收货人'), t('收货人地址'), t('订单备注')].includes(row.label));
    }
    return rows;
});

const deliveryRows = computed(() => [
    { label: t('配送方式'), value: detail.value?.exchangeInfo?.deliveryInfo?.shippingTypeName },
    { label: t('发货时间'), value: detail.value?.exchangeInfo?.deliveryInfo?.shippingTime },
    { label: t('物流名称'), value: detail.value?.exchangeInfo?.deliveryInfo?.logisticsName },
    { label: t('物流单号'), value: detail.value?.exchangeInfo?.deliveryInfo?.trackingNo }
]);

const paymentRows = computed(() => {
    const info = detail.value?.exchangeInfo?.payInfo;
    if (!info) {
        return [];
    }
    const rows = [
        { label: t('支付方式'), value: info.payTypeName },
        { label: t('付款时间'), value: info.exchangeTime }
    ];
    if (isStoredValueCard.value) {
        rows.push({ label: t('兑换时间'), value: info.exchangeTime });
        rows.push({ label: t('兑换金额'), value: priceFormat(info.exchangeAmount) });
    }
    return rows;
});

const fetchDetail = async () => {
    if (!giftCardsId.value) {
        loading.value = false;
        return;
    }
    try {
        const result = await getGiftCardDetailInfo({
            giftCardsId: giftCardsId.value
        });
        detail.value = result;
    } catch (error: any) {
        message.error(error.message || t('获取礼品卡详情失败'));
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    fetchDetail();
});
</script>

<style scoped lang="less">
.detail-wrapper {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.section-card {
    background-color: #f7f8fa;
    padding: 20px 24px;
    &.borded {
        border: 1px solid rgba(245, 247, 250, 1);
        box-shadow: 0px 0px 2px 0px rgba(0, 0, 0, 0.12);
    }
}

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2329;
    margin-bottom: 25px;
    display: flex;
    align-items: center;
}

.info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
}

.grid-item {
    display: flex;
    gap: 12px;
    align-items: center;

    .label {
        min-width: 84px;
        text-align: right;
    }

    .value {
        color: #a4adb3;
        font-weight: 500;
    }
}

.info-block-list {
    display: flex;
    flex-direction: row;
    gap: 20px;
    white-space: nowrap;
}

.info-block {
    flex: 1;
}

.block-title {
    color: #646566;
    margin-bottom: 22px;
}

.block-content {
    display: flex;
    flex-direction: column;
    gap: 18px;
    min-width: 180px;
}

.info-row {
    display: flex;
    flex-wrap: wrap;
    font-size: 12px;
    color: #4b4f56;

    .label {
        color: #8a9099;
        text-align: right;
    }

    .value {
        flex: 1;
        color: #000;
        white-space: pre-wrap;
        overflow-wrap: break-word;
    }
}
</style>
