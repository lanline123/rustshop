<template>
    <view class="buy" @click.stop="handleBuy">
        <slot />
    </view>
    <up-toast ref="uToastRef" />
</template>

<script setup lang="ts">
import { ref } from "vue";
import { addToCart } from "@/api/product/product";
import { addExchangeToCart } from "@/api/exchange/exchange";
import { useI18n } from "vue-i18n";
import { useConfigStore } from "@/store/config";

const { t } = useI18n();
const configStore = useConfigStore();
const props = defineProps({
    id: Number,
    number: {
        type: Number,
        default: 1
    },
    skuId: {
        type: Number
    },
    extraAttrIds: {
        type: String,
        default: ""
    },
    disabled: {
        type: Boolean,
        default: false
    },
    stock: {
        type: [Number, String],
        default: undefined
    },
    isQuick: {
        type: Boolean,
        default: false
    },
    type: {
        type: String
    },
    skuItem: {
        type: Array
    },
    shopId: {
        type: Number,
        default: 0
    },
    grouponData: {
        // 拼团信息
        type: Object
    },
    lotteryRecordId: {
        //抽奖记录id
        type: Number
    },
    checkQueryExtra: {
        // 立即购买跳结算页时附加的 query（如门店自提 tab 锁定配送方式），以 & 开头
        type: String,
        default: ""
    }
});
const emit = defineEmits(["callback"]);
const loading = ref(false);
const uToastRef = ref<any>(null);

const showToast = (message: string) => {
    uToastRef.value?.show({
        type: "default",
        message: message,
        duration: 1500,
        position: "center"
    });
};

const handleBuy = async () => {
    if (props.disabled || (props.stock !== undefined && Number(props.stock) <= 0)) return;

    try {
        if (loading.value) return;
        loading.value = true;
        const salesmanId = getSalemanId(props.id!);
        let result: any = {};
        const filterParams: any = {
            id: props.id ?? 0,
            number: props.number,
            skuId: props.skuId,
            isQuick: props.isQuick ? 1 : 0
        };
        if (props.extraAttrIds) {
            filterParams.extraAttrIds = props.extraAttrIds;
        }
        if (Array.isArray(props.skuItem) && props.skuItem.length > 0) {
            filterParams.skuItem = props.skuItem;
        }

        // 判断是否有拼团信息
        if (props.grouponData && props.grouponData.grouponPromotionId) {
            filterParams.grouponData = props.grouponData;
        }
        if (props.lotteryRecordId) {
            filterParams.lotteryRecordId = props.lotteryRecordId;
        }
        if (props.lotteryRecordId) {
            filterParams.lotteryRecordId = props.lotteryRecordId;
        }
        if (props.shopId && props.shopId > 0) {
            filterParams.shopId = props.shopId;
        }

        switch (props.type) {
            case "exchange":
                result = await addExchangeToCart(filterParams);
                break;
            default:
                filterParams.salesmanId = salesmanId > 0 ? salesmanId : undefined;
                result = await addToCart(filterParams);
        }
        emit("callback");
        if (props.isQuick == true) {
            if (configStore.closeOrder === 1) return uni.$u.toast(t("商城已关闭下单"));
            // 预约到店(9)/预约上门(10)走独立预约流程，不附加门店 tab 的配送方式锁定参数
            const extraQuery = [9, 10].includes(Number(result.flowType)) ? "" : props.checkQueryExtra;
            if (props.lotteryRecordId) {
                return uni.navigateTo({ url: `/pages/order/check?flowType=${result.flowType}&lotteryRecordId=${props.lotteryRecordId}${extraQuery}` });
            } else {
                uni.navigateTo({ url: `/pages/order/check?flowType=${result.flowType}${extraQuery}` });
            }
        } else {
            uni.showToast({ title: t("加入购物车成功"), duration: 1500 });
        }
    } catch (error: any) {
        showToast(error.message);
    } finally {
        loading.value = false;
    }
};

const getSalemanId = (id: number) => {
    const salesmanProducts = uni.getStorageSync("salesmanProducts") || [];
    if (!salesmanProducts.length) return 0;
    const salesman = salesmanProducts.find((item: any) => item.productId == id);
    return Number(salesman?.salesmanId) || 0;
};
</script>

<style lang="scss" scoped></style>
