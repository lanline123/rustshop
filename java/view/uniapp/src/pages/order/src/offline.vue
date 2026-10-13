<template>
    <view class="offline-warp">
        <view :class="'offline-menu ' + (enabledPaymentTypes.length > 1 ? '' : 'single-menu')">
            <view
                v-if="isEnabled('thirdPartyPaymentTransfer')"
                class="offline-menu-item"
                :class="{ active: activeName === '第三方支付转账' }"
                @click="handleTabsActive('第三方支付转账')"
                >{{ $t("第三方支付转账") }}</view
            >
            <view
                v-if="isEnabled('offlinePayBank')"
                class="offline-menu-item"
                :class="{ active: activeName === '银行汇款' }"
                @click="handleTabsActive('银行汇款')"
                >{{ $t("银行汇款") }}</view
            >
            <view
                v-if="isEnabled('offlinePayCompany')"
                class="offline-menu-item"
                :class="{ active: activeName === '企业汇款' }"
                @click="handleTabsActive('企业汇款')"
                >{{ $t("企业汇款") }}</view
            >
            <view
                v-if="isEnabled('otherPaymentRemittance')"
                class="offline-menu-item"
                :class="{ active: activeName === '其他汇款方式' }"
                @click="handleTabsActive('其他汇款方式')"
                >{{ $t("其他汇款方式") }}</view
            >
        </view>
        <block v-if="activeName === '第三方支付转账'">
            <view class="offline-content">
                <!-- <rich-text :nodes="offlinePaymentList?.thirdPartyPaymentTransfer" /> -->
                <up-parse :content="offlinePaymentList?.thirdPartyPaymentTransfer" />
            </view>
        </block>
        <block v-if="activeName === '银行汇款'">
            <view class="offline-content">
                <rich-text :nodes="offlinePaymentList?.offlinePayBank" />
            </view>
        </block>
        <block v-if="activeName === '企业汇款'">
            <view class="offline-content">
                <rich-text :nodes="offlinePaymentList?.offlinePayCompany" />
            </view>
        </block>
        <block v-if="activeName === '其他汇款方式'">
            <view class="offline-content">
                <rich-text :nodes="offlinePaymentList?.otherPaymentRemittance" />
            </view>
        </block>
    </view>
</template>

<script setup lang="ts">
/**
 * 线下支付信息弹窗组件
 * - 展示线下支付方式的收款账户信息
 * - 支持复制银行账号、支付宝账号等
 */
import { computed, ref, watch } from "vue";
import type { OfflinePaymentList } from "@/types/order/pay";

interface Props {
    offlinePaymentList: OfflinePaymentList;
}
const props = defineProps<Props>();

// 支付方式映射
const paymentTypeMap = [
    { key: "第三方支付转账", field: "thirdPartyPaymentTransfer" },
    { key: "银行汇款", field: "offlinePayBank" },
    { key: "企业汇款", field: "offlinePayCompany" },
    { key: "其他汇款方式", field: "otherPaymentRemittance" }
];
const enabledPaymentTypes = computed(() => {
    return paymentTypeMap.filter((item) => {
        const value = props.offlinePaymentList?.[item.field as keyof OfflinePaymentList];
        return value && value.trim();
    });
});

// 获取第一个已启用的支付方式
const getFirstEnabledPayment = () => {
    for (const item of paymentTypeMap) {
        const value = props.offlinePaymentList?.[item.field as keyof OfflinePaymentList];
        if (value && value.trim()) {
            return item.key;
        }
    }
    return "";
};

const activeName = ref(getFirstEnabledPayment());

const isEnabled = (field: keyof OfflinePaymentList) => {
    const value = props.offlinePaymentList?.[field];
    return typeof value === "string" && value.trim().length > 0;
};

// 监听 offlinePaymentList 变化，更新默认选中项
watch(
    () => props.offlinePaymentList,
    () => {
        activeName.value = getFirstEnabledPayment();
    },
    { immediate: true, deep: true }
);

const handleTabsActive = (str: string) => {
    activeName.value = str;
};
</script>

<style lang="scss" scoped>
.offline-warp {
    border-radius: 20rpx;
    padding: 10rpx;
    background-color: #fff;
    margin: 20rpx 0 0;
    overflow: hidden;

    .offline-menu {
        height: 100rpx;
        width: 100%;
        background-color: #fff;
        display: flex;
        align-items: center;
        box-sizing: content-box;
        margin-bottom: 15rpx;
        border-bottom: 1px solid #f5f5f5;

        .offline-menu-item {
            flex: 1;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 25rpx;
            color: #999;
            position: relative;

            &.active {
                color: #333;
                font-weight: bold;

                &:after {
                    content: "";
                    position: absolute;
                    bottom: 0;
                    height: 5rpx;
                    width: 80rpx;
                    background-color: var(--general);
                    border-radius: 10rpx;
                }
            }
        }
        &.single-menu {
            .offline-menu-item {
                flex: 0 0 auto;
                padding: 0 30rpx;
            }
        }
    }
}
.offline-content {
    padding: 20rpx;
    line-height: 45rpx;
}
</style>
