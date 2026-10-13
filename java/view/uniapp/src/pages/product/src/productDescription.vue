<template>
    <view class="aftersale-card-box" :class="{ 'aftersale-card-box--overseas': overseas }">
        <template v-if="product.productBrief">
            <view class="aftersale-card">
                <view class="aftersale-card-title" @click="showMore = !showMore">
                    <view class="title-item"> {{ $t("商品描述") }}</view>
                    <text class="iconfont-h5 icon-a-gengduo20" :class="{ actvie: showMore }" />
                </view>
                <view v-show="showMore" class="tab_body">
                    <rich-text :nodes="product.productBrief" />
                </view>
            </view>
        </template>
    </view>
</template>

<script lang="ts" setup>
/**
 * 商品描述卡片
 * - 标题栏点击展开/收起
 * - 富文本渲染商品简介
 */
import { ref } from "vue";

defineProps({
    product: {
        type: Object,
        default: () => ({})
    },
    overseas: {
        type: Boolean,
        default: false
    }
});

const showMore = ref(false);
</script>

<style lang="scss" scoped>
.aftersale-card-box {
    margin-bottom: 14rpx;

    &.aftersale-card-box--overseas {
        margin-bottom: 0;

        .aftersale-card {
            border-top: 14rpx solid var(--product-page-bg, #f5f5f5);
            border-radius: 0;
        }
    }

    .aftersale-card {
        background-color: var(--product-content-bg, #fff);

        .aftersale-card-title {
            display: flex;
            width: 100%;
            overflow: hidden;
            font-size: 24.5rpx;
            line-height: 42rpx;
            padding: 28rpx;

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
    }
}

.tab_body {
    text-align: start;
    color: #333;
    padding: 0 28rpx 28rpx;
}
</style>
