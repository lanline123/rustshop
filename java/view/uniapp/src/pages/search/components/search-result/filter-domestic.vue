<template>
    <!-- 国内版排序/筛选栏（layoutStyle=1）：默认 / 销量 / 价格 + 右侧筛选入口 -->
    <view class="tab-box flex align-center justify-between">
        <template v-for="item in tabList" :key="item.value">
            <view class="item flex align-center" :class="{ active: tabIndex === item.value }" @click="emit('changeTab', item)">
                <text>{{ $t(item.label) }}</text>
                <view v-show="item.value === 'price'" class="price-ico-box flex flex-column">
                    <text class="iconfont-h5 icon-up" style="font-size: 16rpx" :class="{ order: tabIndex === 'price' && item.order === 'asc' }" />
                    <text class="iconfont-h5 icon-arrowdown" style="font-size: 16rpx" :class="{ order: tabIndex === 'price' && item.order === 'desc' }" />
                </view>
            </view>
        </template>

        <view class="item flex" @click="emit('showDrawer')">
            <text>{{ $t("筛选") }}</text>
            <text class="iconfont-h5 icon-gengduo1" />
        </view>
    </view>
</template>

<script lang="ts" setup>
/**
 * 搜索结果页 - 国内版排序筛选栏
 * 从 result.vue 内联结构抽出，交互与样式保持一致；激活态颜色跟随装修主题色。
 */
export interface SortTabItem {
    label: string;
    value: string;
    order?: string;
}

defineProps<{
    tabList: SortTabItem[];
    tabIndex: string;
}>();

const emit = defineEmits<{
    (e: "changeTab", item: SortTabItem): void;
    (e: "showDrawer"): void;
}>();
</script>

<style lang="scss" scoped>
.tab-box {
    background-color: var(--search-item-bg, #fff);
    padding: 0 var(--search-item-padding-lr, 20rpx);
    box-sizing: border-box;
    .item {
        width: 100%;
        margin: 20rpx 0;
        flex: 1;
        color: #333;
        font-size: 24rpx;
        align-items: center;
        justify-content: center;
        .price-ico-box {
            margin-left: 10rpx;
            .iconfont-h5 {
                color: #c3c0c0 !important;
                line-height: 6px;
            }
            .order {
                /* --search-theme 由装修主题色下发，未配置时回落站点主题色 */
                color: var(--search-theme, var(--general)) !important;
            }
        }
        .icon-gengduo1 {
            font-size: 16rpx;
            color: #c3c0c0;
            margin-left: 10rpx;
            display: inline-block;
            transform: translateY(1rpx);
        }
    }
    .active {
        color: var(--search-theme, var(--general));
    }
}
</style>
