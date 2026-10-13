<template>
    <view class="attr-card-box" :class="{ 'attr-card-box--overseas': overseas }">
        <view class="attr-card">
            <view class="attr-card-title" @click="showMore = !showMore">
                <view class="title-item"> {{ $t("规格参数") }}</view>
                <text class="iconfont-h5 icon-a-gengduo20" :class="{ actvie: showMore }" />
            </view>

            <view v-if="showMore" class="attr-card-content">
                <template v-for="(item, index) in normal" :key="index">
                    <view class="attr-card-item">
                        <view class="item-name line1">{{ item.attrName }}</view>
                        <view class="item-value line1">{{ getAllAttrValue(item.attrList) }}</view>
                    </view>
                </template>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 规格参数卡片
 * - 标题栏点击展开/收起
 * - 逐行展示参数名与参数值
 */
import { ref } from "vue";

defineProps({
    normal: {
        type: Array as () => any[],
        default: () => []
    },
    overseas: {
        type: Boolean,
        default: false
    }
});

const showMore = ref(false);

const getAllAttrValue = (arr: any[]) => {
    return arr.map((item) => item.attrValue).join("、");
};
</script>

<style lang="scss" scoped>
.attr-card-box {
    margin-bottom: 14rpx;

    &.attr-card-box--overseas {
        margin-bottom: 0;

        .attr-card {
            border-top: 14rpx solid var(--product-page-bg, #f5f5f5);
            border-radius: 0;
        }
    }

    .attr-card {
        background-color: var(--product-content-bg, #fff);

        .attr-card-title {
            display: flex;
            width: 100%;
            overflow: hidden;
            padding: 28rpx;
            font-size: 24.5rpx;
            line-height: 42rpx;

            .title-item {
                color: #000;
                display: flex;
                align-items: center;
                flex: 1;
                font-weight: 700;
                font-size: 28rpx;
            }

            .icon-a-gengduo20 {
                font-size: 16rpx;
                color: rgb(136, 139, 148);
                display: flex;
                align-items: center;
                transform: rotate(90deg);

                &.actvie {
                    transform: rotate(-90deg);
                }
            }
        }

        .attr-card-content {
            display: flex;
            flex-direction: column;
            padding: 0 28rpx 28rpx;

            .attr-card-item {
                display: flex;
                align-items: center;

                .item-name {
                    line-height: 35rpx;
                    padding: 14rpx 0;
                    font-size: 24.5rpx;
                    color: #888b94;
                    width: 30%;
                }

                .item-value {
                    line-height: 35rpx;
                    padding: 14rpx 0;
                    font-size: 24.5rpx;
                    color: #1a1a1a;
                    padding-left: 28rpx;
                    flex: 1;
                }
            }
        }
    }
}
</style>
