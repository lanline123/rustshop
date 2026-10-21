<!-- WaterfallLayout.vue -->
<template>
    <view class="waterfall-layout">
        <!-- 布局切换按钮 -->
        <view v-if="showToggle" class="layout-toggle">
            <view class="toggle-btn" :class="{ active: layoutMode === 'single' }" @click="switchLayout('single')">
                <text class="iconfont icon-danlie">{{ $t("单列") }}</text>
            </view>
            <view class="toggle-btn" :class="{ active: layoutMode === 'double' }" @click="switchLayout('double')">
                <text class="iconfont icon-shuanglie">{{ $t("双列") }}</text>
            </view>
        </view>

        <!-- 瀑布流容器 -->
        <view class="waterfall-container" :class="[`layout-${layoutMode}`]" :scroll-y="true">
            <!-- 单列布局 -->
            <view v-if="layoutMode === 'single'" class="single-column">
                <view v-for="(item, index) in dataList" :key="index" class="waterfall-item single-item" @click="onItemClick(item, index)">
                    <slot name="item" :item="item" :index="index" />
                </view>
            </view>

            <!-- 双列布局 -->
            <view v-if="layoutMode === 'double'" class="double-columns">
                <view class="column left-column">
                    <view
                        v-for="(item, index) in leftColumnData"
                        :key="`left-${index}`"
                        class="waterfall-item double-item"
                        @click="onItemClick(item, getColumnIndex('left', index))"
                    >
                        <slot name="item" :item="item" :index="getColumnIndex('left', index)" />
                    </view>
                </view>

                <view class="column right-column">
                    <view
                        v-for="(item, index) in rightColumnData"
                        :key="`right-${index}`"
                        class="waterfall-item double-item"
                        @click="onItemClick(item, getColumnIndex('right', index))"
                    >
                        <slot name="itemRight" :item="item" :index="getColumnIndex('right', index)" />
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
/**
 * 瀑布流布局组件
 * - 两列瀑布流展示
 * - 自动计算高度分配
 */
import { ref, computed, watch, onMounted } from "vue";

interface Props {
    // 数据列表
    list: any[];
    // 布局模式: single(单列) | double(双列)
    layout?: "single" | "double";
    // 是否显示布局切换按钮
    showToggle?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    list: () => [],
    layout: "double",
    showToggle: false
});

const emit = defineEmits<{
    (e: "update:layout", value: "single" | "double"): void;
    (e: "load-more"): void;
    (e: "item-click", item: any, index: number): void;
}>();

// 响应式数据
const layoutMode = ref<"single" | "double">(props.layout);
const leftColumnData = ref<any[]>([]);
const rightColumnData = ref<any[]>([]);
const columnHeights = ref<[number, number]>([0, 0]); // 记录两列高度

// 计算属性
const dataList = computed(() => props.list);

// 计算左侧列数据索引
const getColumnIndex = (column: "left" | "right", index: number) => {
    if (column === "left") {
        return index * 2;
    } else {
        return index * 2 + 1;
    }
};

// 切换布局模式
const switchLayout = (mode: "single" | "double") => {
    layoutMode.value = mode;
    emit("update:layout", mode);

    // 布局切换时重新分配数据
    if (mode === "double") {
        distributeDataToColumns();
    }
};

// 将数据分配到双列
const distributeDataToColumns = () => {
    leftColumnData.value = [];
    rightColumnData.value = [];
    columnHeights.value = [0, 0];
    props.list.forEach((item, index) => {
        // 简单按奇偶分配（实际项目中可根据图片高度优化）
        if (index % 2 === 0) {
            leftColumnData.value.push(item);
        } else {
            rightColumnData.value.push(item);
        }
    });
};

// 项目点击事件
const onItemClick = (item: any, index: number) => {
    emit("item-click", item, index);
};

// 监听数据变化
watch(
    () => props.list,
    (newList) => {
        if (layoutMode.value === "double") {
            distributeDataToColumns();
        }
    },
    { immediate: false }
);

// 监听布局模式变化
watch(
    () => props.layout,
    (newLayout) => {
        layoutMode.value = newLayout;
    }
);

// 组件挂载时初始化
onMounted(() => {
    if (layoutMode.value === "double") {
        distributeDataToColumns();
    }
});
</script>

<style scoped lang="scss">
.waterfall-layout {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.layout-toggle {
    display: flex;
    justify-content: flex-end;
    padding: 20rpx;
    background-color: #fff;
    border-bottom: 1rpx solid #f0f0f0;
}

.toggle-btn {
    //width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: 20rpx;
    border: 1rpx solid #ddd;
    border-radius: 10rpx;
    padding: 10rpx 20rpx;
}

.toggle-btn.active {
    border-color: #ff6b35;
    color: #ff6b35;
}

.waterfall-container {
    //flex: 1;
}

.single-column {
    padding: 10rpx;
}

.single-item {
    margin-bottom: 10rpx;
    background-color: #fff;
    border-radius: 12rpx;
    overflow: hidden;
    //box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.double-columns {
    display: flex;
    padding: 10rpx;
    column-gap: 10rpx;
}

.column {
    flex: 1;
}

.double-item {
    margin-bottom: 10rpx;
    background-color: #fff;
    border-radius: 10rpx;
    overflow: hidden;
    //box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.loading-more,
.no-more {
    text-align: center;
    padding: 30rpx;
    font-size: 24rpx;
    color: #999;
}
</style>
