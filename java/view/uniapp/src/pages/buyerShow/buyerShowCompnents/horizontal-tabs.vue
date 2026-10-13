<template>
    <view class="horizontal-tabs">
        <!-- Tabs容器 -->
        <scroll-view class="tabs-container" scroll-x="true" :scroll-left="scrollLeft" scroll-with-animation>
            <view class="tabs-wrapper">
                <template v-for="(tab, index) in tabs" :key="index">
                    <view class="tab-item" :class="{ active: currentIndex === index }" @click="switchTab(index)">
                        <text class="tab-text">{{ $t(tab.title) }}</text>
                    </view>
                </template>
            </view>
        </scroll-view>

        <!-- 内容区域 -->
        <view class="tabs-content">
            <slot :current-index="currentIndex" />
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 横向Tab切换组件
 * - 买家秀分类Tab
 * - 支持滑动切换
 */
import { ref, watch, nextTick, onMounted, getCurrentInstance } from "vue";
import { getElementRect } from "@/utils";

interface TabItem {
    title: string;
}

interface Props {
    tabs: TabItem[];
}

const props = withDefaults(defineProps<Props>(), {});

const currentIndex = defineModel<number>("currentTab");
const scrollLeft = ref(0);
const tabWidths = ref<number[]>([]);

// 切换tab
const switchTab = (index: number) => {
    currentIndex.value = index;
};

const instance = getCurrentInstance();

// 获取每个tab的实际宽度
const calculateTabWidths = async () => {
    return new Promise<void>((resolve) => {
        nextTick(async () => {
            const rects: any = await getElementRect(".tab-item", instance, true);
            const safeRects = Array.isArray(rects) ? rects : rects ? [rects] : [];
            tabWidths.value = safeRects.map((rect: any) => rect.width);
            resolve();
        });
    });
};

// 动态计算滚动位置
const scrollToActiveTab = async (newIndex: number) => {
    // 如果还没有计算宽度，先计算一次
    if (tabWidths.value.length === 0) {
        await calculateTabWidths();
    }
    if (tabWidths.value.length > 0 && newIndex < tabWidths.value.length) {
        // 计算到选中tab的累计宽度
        let cumulativeWidth = 0;
        for (let i = 0; i < newIndex; i++) {
            cumulativeWidth += tabWidths.value[i];
        }
        // 选中tab的宽度
        const activeTabWidth = tabWidths.value[newIndex];
        // 屏幕宽度
        const containerWidth = uni.getSystemInfoSync().windowWidth;
        // 计算滚动位置使选中tab居中
        const targetScroll = cumulativeWidth - (containerWidth - activeTabWidth) / 2;
        scrollLeft.value = Math.max(0, targetScroll);
    } else {
        const avgWidth = 120;
        const containerWidth = uni.getSystemInfoSync().windowWidth;
        const targetScroll = newIndex * avgWidth - (containerWidth - avgWidth) / 2;
        scrollLeft.value = Math.max(0, targetScroll);
    }
};

// 监听当前索引变化，自动滚动到可视区域
watch(currentIndex, (newIndex: any) => {
    scrollToActiveTab(newIndex);
});

// 监听tabs变化，重新计算宽度
watch(
    () => props.tabs,
    async () => {
        await calculateTabWidths();
        // 重新计算当前选中tab的位置
        if (currentIndex.value !== undefined) {
            scrollToActiveTab(currentIndex.value);
        }
    },
    { immediate: true }
);

// 组件挂载后计算tab宽度
onMounted(async () => {
    await calculateTabWidths();
    // 初始化时滚动到当前选中的tab
    if (currentIndex.value !== undefined) {
        scrollToActiveTab(currentIndex.value);
    }
});
</script>

<style scoped lang="scss">
.horizontal-tabs {
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 100%;
}

.tabs-container {
    width: 100%;
    height: 80rpx;
    white-space: nowrap;
    position: relative;
    background-color: #fff;
    border-bottom: 1rpx solid #f0f0f0;
    padding: 0 10px;
    box-sizing: border-box;
}

.tabs-wrapper {
    display: inline-block;
    min-width: 100%;
    height: 100%;
}

.tab-item {
    display: inline-block;
    //min-width: 120rpx;
    padding: 0 20rpx;
    text-align: center;
    position: relative;
    height: 60rpx;
    margin-top: 10rpx;
}

.tab-text {
    height: 100%;
    font-weight: 400;
    font-size: 24rpx;
    color: #333333;
    line-height: 60rpx;
}

.tab-item.active {
    //background: #f4f4f4;
    border-radius: 30rpx;
}

.tab-item.active .tab-text {
    font-weight: 600;
    font-size: 24rpx;
    color: #333;
    box-shadow: none;
}

.tabs-content {
    flex: 1;
    overflow: hidden;
}
</style>
