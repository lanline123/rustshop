<template>
    <view v-if="filterState.length > 0" class="log-main">
        <block v-for="(item, index) in filterState" :key="index">
            <view class="item">
                <view class="tit">{{ item.changeTypeName }}</view>
                <view class="time">{{ item.changeTime }}</view>
                <view class="desc">{{ item.changeDesc }}</view>
                <view class="real_total">
                    <text v-if="item.changeTypeName === '增加'">+</text>
                    <text v-else>-</text>
                    <text>{{ item.balance }}</text>
                </view>
            </view>
        </block>
    </view>
    <view v-if="filterState.length === 0 && loadend === true" class="empty-box">
        <view class="pictrue"><image :src="staticResource('common/data_empty.png')" /></view>
        <view class="txt">{{ $t("暂无数据") }}</view>
    </view>
    <loading-box v-model="loaded" :page="filterParams.page" :length="filterState.length" />
</template>

<script lang="ts" setup>
/**
 * 账户明细组件
 * - 展示余额变动记录列表
 * - 显示变动类型、时间、描述及金额
 * - 支持触底加载更多
 */
import { ref, reactive, watch } from "vue";
import { onReachBottom } from "@dcloudio/uni-app";
import { getAccountList, getCommissionAccountList } from "@/api/user/account";
import type { AccountFilterParams, AccountDetailFilterState } from "@/types/user/account";
import { staticResource } from "@/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const activeTab = defineModel("activeTab");
const props = defineProps({
    refreshKey: {
        type: Number,
        default: 0
    },
    // D1107：钱包类型，balance=余额明细 commission=佣金明细
    wallet: {
        type: String,
        default: "balance"
    }
});
// 当前组件对应的 activeTab 标识与取数接口
const myTab = props.wallet === "commission" ? "commissionDetail" : "detail";
const listApi = props.wallet === "commission" ? getCommissionAccountList : getAccountList;

const total = ref(0);
const loaded = ref(false);
const loadend = ref(false);
const filterParams = reactive<AccountFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: 10,
    status: 1
});
const filterState = ref<AccountDetailFilterState[]>([]);
const resetAndLoad = () => {
    filterParams.page = 1;
    total.value = 0;
    loaded.value = false;
    loadend.value = false;
    filterState.value = [];
    __detail();
};
const __detail = async () => {
    if (filterParams.page > 1) {
        loaded.value = true;
    } else {
        uni.showLoading({
            title: t("加载中")
        });
    }
    try {
        const result = await listApi({ ...filterParams });
        total.value = result.total;
        filterState.value = [...filterState.value, ...result.records];
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
    if (activeTab.value && activeTab.value === myTab && filterParams.page < Math.ceil(total.value / filterParams.size)) {
        filterParams.page++;
        __detail();
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
        .time,
        .desc {
            color: #999;
            position: relative;
            font-size: 24rpx;
            padding-top: 10rpx;
        }

        .real_total {
            font-size: 32rpx;
            color: var(--general);
            font-weight: bold;
            position: absolute;
            right: 40rpx;
            top: 50%;
            transform: translateY(-50%);
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
