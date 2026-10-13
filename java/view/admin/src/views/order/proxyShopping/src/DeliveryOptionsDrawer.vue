<template>
    <div class="delivery-drawer-content">
        <div class="warning-banner">{{ $t('不同配送方式的商品暂不支持合并结算，请分开下单') }}</div>

        <div v-for="(item, index) in displayOptions" :key="index" class="delivery-section">
            <div class="section-header">
                <img :src="getDeliveryIcon(item)" class="header-icon-img" />
                <span class="delivery-name">{{ getDeliveryName(item) }}</span>
                <span class="delivery-count"
                    >{{ $t('共') }}<span class="count-num">{{ getDeliveryCount(item) }}</span
                    >{{ $t('件') }}</span
                >
            </div>

            <div class="section-box">
                <div v-if="Number(item?.deliveryType) === 2 && getStoreName(item)" class="pickup-info">{{ $t('自提点：{getStoreName}', { getStoreName: getStoreName(item) }) }}</div>
                <div class="box-bottom">
                    <div class="item-images">
                        <div v-for="(prod, idx) in getProductList(item)" :key="idx" class="img-wrapper">
                            <img :src="imageFormat(prod?.picThumb || prod?.picUrl || prod?.productPic || prod?.picLarge)" class="prod-img" />
                        </div>
                    </div>
                    <el-button type="primary" class="checkout-btn" @click="onCheckout(item)">{{ $t('去结算') }}</el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import { imageFormat } from "@/utils/format";
import pickUpIcon from "@/assets/proxyShopping/pick_up.png";
import pendingCatchIcon from "@/assets/proxyShopping/pending_catch.png";
import cityDeliveryIcon from "@/assets/proxyShopping/city_delivery.png";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = withDefaults(
    defineProps<{
        visible?: boolean;
        options: any[];
    }>(),
    {
        options: () => []
    }
);

const emit = defineEmits<{
    (e: "submitCallback", item: any): void;
    (e: "okType", val: boolean): void;
}>();

const displayOptions = computed(() => props.options);

const getDeliveryName = (item: any) => {
    const type = Number(item?.deliveryType);
    return item?.deliveryTypeName || (type === 2 ? t('到店自提') : type === 3 ? t('同城配送') : t('商家配送'));
};

const getDeliveryIcon = (item: any) => {
    const type = Number(item?.deliveryType);
    if (type === 2) return pickUpIcon;
    if (type === 3) return cityDeliveryIcon;
    return pendingCatchIcon;
};

const getDeliveryCount = (item: any) => {
    return getProductList(item).length;
};

const getProductList = (item: any) => {
    const list = item?.productList || [];
    return Array.isArray(list) ? list : [];
};

const getStoreName = (item: any) => {
    return item?.storeName || "";
};

const onCheckout = (item: any) => {
    emit("submitCallback", item);
};

watch(
    () => props.visible,
    (visible) => {
        if (visible) emit("okType", true);
    },
    { immediate: true }
);
</script>

<style scoped lang="less">
.delivery-drawer-content {
    padding: 0 20px 20px;
}

.warning-banner {
    background-color: #fffbe6;;
    padding: 10px 15px;
    border: 1px solid #ffe58f;
    border-radius: 4px;
    font-size: 13px;
    margin-bottom: 20px;
}

.delivery-section {
    margin-bottom: 24px;
}

.section-header {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
}

.header-icon-img {
    width: 20px;
    height: 20px;
    object-fit: contain;
    margin-right: 8px;
}

.delivery-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-right: 12px;
}

.delivery-count {
    font-size: 13px;
    color: #909399;
}

.count-num {
    color: var(--el-color-primary);
    margin: 0 2px;
}

.section-box {
    background-color: #f5f7fa;
    border-radius: 8px;
    padding: 16px;
}

.pickup-info {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
    margin-bottom: 12px;
}

.box-bottom {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
}

.item-images {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    flex: 1;
    min-width: 0;
    padding-right: 16px;
}

.img-wrapper {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    background-color: #fff;
    overflow: hidden;
    flex-shrink: 0;
}

.prod-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.checkout-btn {
    flex-shrink: 0;
    padding: 8px 20px;
    border-radius: 4px;
}
</style>
