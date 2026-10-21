<template>
    <tig-layout :title="$t('赚钱攻略')">
        <view class="notifycenter-box">
            <view class="notifycenter-list">
                <block v-for="(item, index) in list" :key="item.id">
                    <view class="notifycenter-item" @click="handleClick(item.id)">
                        <view class="notifycenter-item-left">
                            <view class="item-left-title">
                                <view class="item-left-title-info line2"
                                    ><view v-if="item.isTop === 1" class="title-tip">{{ $t("置顶") }}</view> {{ item.title }}</view
                                >
                            </view>
                            <view v-if="item.describe" class="item-left-desc line2">{{ item.describe }}</view>
                            <view class="item-left-time">{{ item.startTime }}</view>
                        </view>
                        <view class="notifycenter-item-right">
                            <tig-image :src="item.img" />
                        </view>
                    </view>
                </block>
            </view>
        </view>
        <empty v-if="!loading && list.length === 0" :styles="{ width: '320rpx', height: '320rpx' }" />
    </tig-layout>
</template>

<script setup lang="ts">
/**
 * 通知中心页
 * - 展示分销员相关通知列表
 * - 支持分页加载更多
 */
import { reactive, ref } from "vue";
import { salesmanContentList } from "@/api/salesman/salesman";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";
import type { SalesmanContentListFilterResult } from "@/types/salesman/salesman";
import empty from "@/pages/salesman/src/empty.vue";

const filterParams = reactive({
    page: 1,
    size: 10
});
const total = ref(0);
const list = ref<SalesmanContentListFilterResult[]>([]);
const loading = ref(false);
const getSalesmanContentList = async () => {
    loading.value = true;
    try {
        const res = await salesmanContentList(filterParams);
        list.value = res.records;
        total.value = res.total;
    } catch (err) {
    } finally {
        loading.value = false;
    }
};

const handleClick = (id: number) => {
    uni.navigateTo({
        url: `/pages/salesman/notifyCenterDetail?id=${id}`
    });
};

onLoad(() => {
    getSalesmanContentList();
});
onReachBottom(() => {
    if (filterParams.page < Math.ceil(total.value / filterParams.size)) {
        filterParams.page++;
        getSalesmanContentList();
    }
});
</script>

<style lang="scss" scoped>
.notifycenter-list {
    position: relative;
    padding: 0 32rpx;
    background-color: #fff;
    .notifycenter-item {
        padding: 32rpx 0;
        border-top: 2rpx solid #f7f8f9;
        display: flex;
        justify-content: space-between;

        .notifycenter-item-left {
            display: flex;
            flex-direction: column;
            justify-content: space-between;

            .item-left-title {
                line-height: 48rpx;
                font-size: 32rpx;
                font-weight: 700;
                flex: 1 1 0;

                .item-left-title-info {
                    .title-tip {
                        display: inline-block;
                        width: 76rpx;
                        text-align: center;
                        line-height: 32rpx;
                        font-size: 24rpx;
                        color: #fff;
                        border-radius: 16rpx 2rpx;
                        padding: 0 12rpx;
                        background: linear-gradient(90deg, #32caff, #1989fa);
                        margin-right: 8rpx;
                        align-self: flex-start;
                        transform: translateY(-4rpx);
                        box-sizing: border-box;
                    }
                }
            }
            .item-left-desc {
                color: #999;
                font-size: 28rpx;
                margin-top: 20rpx;
            }

            .item-left-time {
                margin-top: 32rpx;
                line-height: 38rpx;
                font-size: 26rpx;
                color: #969799;
            }
        }

        .notifycenter-item-right {
            flex-shrink: 0;
            width: 208rpx;
            height: 156rpx;
            border-radius: 8rpx;
            margin-left: 32rpx;
            overflow: hidden;
        }
    }
}
</style>
