<template>
    <view v-if="filterStateLog.length > 0" class="log-main">
        <block v-for="(item, index) in filterStateLog" :key="index">
            <view class="item">
                <view class="tit">{{ item.type }}</view>
                <view class="time">{{ item.addTime }}</view>
                <view class="real_total">
                    <format-price
                        base
                        :show-text="false"
                        :decimals-style="{
                            fontSize: '25rpx',
                            fontWeight: 'bold'
                        }"
                        :currency-style="{
                            fontSize: '23rpx',
                            fontWeight: 'bold'
                        }"
                        :price-data="item.amount"
                    />
                </view>
                <view class="actions">
                    <view class="txt">{{ $t("管理员备注") }}：{{ item.postscript || $t("无") }}</view>
                    <view class="status">{{ item.statusType }}</view>
                </view>
            </view>
        </block>
    </view>
    <view v-if="filterStateLog.length === 0 && loadend === true" class="empty-box">
        <view class="pictrue"><image :src="staticResource('common/data_empty.png')" /></view>
        <view class="txt">{{ $t("暂无数据") }}！</view>
    </view>
    <loading-box v-model="loaded" :page="filterParams.page" :length="filterStateLog.length" />
</template>

<script lang="ts" setup>
/**
 * 申请记录组件（充值/提现）
 * - 展示充值或提现申请记录列表
 * - 显示类型、时间、金额、管理员备注及状态
 * - 支持触底加载更多
 */
import { ref, reactive, watch } from "vue";
import { onReachBottom } from "@dcloudio/uni-app";
import { getRechargeOrderList } from "@/api/user/account";
import type { AccountFilterParams, AccountFilterState } from "@/types/user/account";
import { staticResource } from "@/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const activeTab = defineModel("activeTab");
const props = defineProps({
    refreshKey: {
        type: Number,
        default: 0
    }
});

const total = ref(0);
const loaded = ref(false);
const loadend = ref(false);
const filterParams = reactive<AccountFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: 10
});
const filterStateLog = ref<AccountFilterState[]>([]);
const resetAndLoad = () => {
    filterParams.page = 1;
    total.value = 0;
    loaded.value = false;
    loadend.value = false;
    filterStateLog.value = [];
    __log();
};
const __log = async () => {
    if (filterParams.page > 1) {
        loaded.value = true;
    } else {
        uni.showLoading({
            title: t("加载中")
        });
    }
    try {
        const result = await getRechargeOrderList({ ...filterParams });
        total.value = result.total;
        filterStateLog.value = [...filterStateLog.value, ...result.records];
    } catch (error: any) {
        console.error(error);
    } finally {
        uni.hideLoading();
        loaded.value = false;
        loadend.value = true;
    }
};

resetAndLoad();

watch(
    () => props.refreshKey,
    () => {
        resetAndLoad();
    }
);

onReachBottom(() => {
    if (activeTab.value && activeTab.value === "log" && filterParams.page < Math.ceil(total.value / filterParams.size)) {
        filterParams.page++;
        __log();
    }
});
</script>
<style lang="scss" scoped>
.log-main {
    margin-top: 40rpx;
    .item {
        position: relative;
        padding: 40rpx;
        margin: 0 20rpx;
        background: #fff;
        border-radius: 18rpx;
        margin-bottom: 20rpx;
        .tit {
            font-size: 28rpx;
            color: #333;
            padding-bottom: 10rpx;
            padding-right: 200rpx;
        }
        .time {
            color: #999;
            position: relative;
            font-size: 24rpx;
        }
        .real_total {
            font-size: 32rpx;
            color: var(--general);
            font-weight: bold;
            position: absolute;
            right: 40rpx;
            top: 40rpx;
        }
        .actions {
            margin-top: 10rpx;
            display: flex;
            justify-content: space-between;
            align-items: center;
            color: #999;
            .txt {
                flex: 1 1 auto;
                font-size: 22rpx;
                padding-right: 20rpx;
            }
            .status {
                flex-shrink: 0;
                font-size: 24rpx;
            }
        }
    }
}
</style>
