<template>
    <view class="container">
        <text class="title">{{ $t("价格") }}</text>
        <view class="price-labels">
            <format-price :font-style="{ color: '##1a1a1a', 'font-size': '25rpx' }" :price-data="leftValue" />
            <format-price :font-style="{ color: '##1a1a1a', 'font-size': '25rpx' }" :price-data="rightValue" />
        </view>
        <view class="slider-bar-box">
            <view class="slider-bar" @click="clickBar">
                <view
                    class="active-bar"
                    :style="{
                        left: `${Math.min(leftPercent, rightPercent)}%`,
                        width: `${Math.abs(leftPercent - rightPercent)}%`
                    }"
                />

                <view
                    class="block left"
                    :style="{ left: `${leftPercent}%` }"
                    @touchmove.stop.prevent="onDrag('left', $event)"
                    @touchend="emitValue"
                    @mousedown.stop.prevent="onMouseDown('left', $event)"
                />

                <view
                    class="block right"
                    :style="{ left: `${rightPercent}%` }"
                    @touchmove.stop.prevent="onDrag('right', $event)"
                    @touchend="emitValue"
                    @mousedown.stop.prevent="onMouseDown('right', $event)"
                />
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref, getCurrentInstance } from "vue";

const props = defineProps({
    maxPrice: {
        type: Number,
        default: 0
    },
    currentMax: {
        type: Number,
        default: 0
    }
});

const emit = defineEmits(["sliderMany"]);
const { proxy } = getCurrentInstance()!;

const min = 0;
const max = props.maxPrice;

const leftValue = ref(0);
const rightValue = ref(props.maxPrice || max);

const leftPercent = ref(valueToPercent(leftValue.value));
const rightPercent = ref(valueToPercent(rightValue.value));

function valueToPercent(val: number) {
    return ((val - min) / (max - min)) * 100;
}

function percentToValue(percent: number) {
    return Math.round((percent / 100) * (max - min) + min);
}

const init = () => {
    leftValue.value = 0;
    rightValue.value = props.maxPrice || max;
    if (props.currentMax && props.currentMax <= props.maxPrice) {
        rightValue.value = props.currentMax;
    }
    leftPercent.value = valueToPercent(leftValue.value);
    rightPercent.value = valueToPercent(rightValue.value);
};
init();

function emitValue() {
    const minPrice = Math.min(leftValue.value, rightValue.value);
    const maxPrice = Math.max(leftValue.value, rightValue.value);
    emit("sliderMany", { minPrice, maxPrice });
}

function onDrag(which: string, e: any) {
    const touch = e.touches[0] || e.changedTouches[0];
    uni.createSelectorQuery()
        .in(proxy)
        .select(".slider-bar")
        .boundingClientRect((data) => {
            if (!data || Array.isArray(data)) return;
            const { left, width } = data;
            if (typeof left !== "number" || typeof width !== "number") return;

            let percent = ((touch.pageX - left) / width) * 100;
            percent = Math.max(0, Math.min(percent, 100));
            const value = percentToValue(percent);

            if (which === "left") {
                if (percent > rightPercent.value) {
                    rightPercent.value = percent;
                    rightValue.value = value;
                } else {
                    leftPercent.value = percent;
                    leftValue.value = value;
                }
            } else {
                if (percent < leftPercent.value) {
                    leftPercent.value = percent;
                    leftValue.value = value;
                } else {
                    rightPercent.value = percent;
                    rightValue.value = value;
                }
            }
        })
        .exec();
}

function clickBar(e: any) {
    const touch = e.touches?.[0] || e.changedTouches?.[0] || e;
    uni.createSelectorQuery()
        .in(proxy)
        .select(".slider-bar")
        .boundingClientRect((data) => {
            if (!data || Array.isArray(data)) return;
            const { left, width } = data;
            if (typeof left !== "number" || typeof width !== "number") return;

            let percent = ((touch.pageX - left) / width) * 100;
            percent = Math.max(0, Math.min(percent, 100));
            const value = percentToValue(percent);
            if (Math.abs(value - leftValue.value) < Math.abs(value - rightValue.value)) {
                leftPercent.value = percent;
                leftValue.value = value;
            } else {
                rightPercent.value = percent;
                rightValue.value = value;
            }
        })
        .exec();
}

// 鼠标拖动相关变量
const isDragging = ref(false);
const currentDragWhich = ref<string>("");

function onMouseDown(which: string, e: MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    isDragging.value = true;
    currentDragWhich.value = which;

    // 添加全局鼠标移动和松开事件监听
    document.addEventListener("mousemove", onMouseMove);
    document.addEventListener("mouseup", onMouseUp);
}

function onMouseMove(e: MouseEvent) {
    if (!isDragging.value) return;

    uni.createSelectorQuery()
        .in(proxy)
        .select(".slider-bar")
        .boundingClientRect((data) => {
            if (!data || Array.isArray(data)) return;
            const { left, width } = data;
            if (typeof left !== "number" || typeof width !== "number") return;

            let percent = ((e.pageX - left) / width) * 100;
            percent = Math.max(0, Math.min(percent, 100));
            const value = percentToValue(percent);

            if (currentDragWhich.value === "left") {
                if (percent > rightPercent.value) {
                    rightPercent.value = percent;
                    rightValue.value = value;
                } else {
                    leftPercent.value = percent;
                    leftValue.value = value;
                }
            } else {
                if (percent < leftPercent.value) {
                    leftPercent.value = percent;
                    leftValue.value = value;
                } else {
                    rightPercent.value = percent;
                    rightValue.value = value;
                }
            }
        })
        .exec();
}

function onMouseUp() {
    if (isDragging.value) {
        isDragging.value = false;
        currentDragWhich.value = "";
        emitValue();

        document?.removeEventListener("mousemove", onMouseMove);
        document?.removeEventListener("mouseup", onMouseUp);
    }
}

defineExpose({
    init
});
</script>

<style scoped lang="scss">
.container {
    padding: 20rpx;
}
.title {
    font-size: 28rpx;
    font-weight: bold;
    margin-bottom: 10rpx;
}

.slider-bar-box {
    border-radius: 4rpx;
    padding: 30rpx 0;
    width: 100%;

    .slider-bar {
        position: relative;
        background: #f3f3f3;
        height: 8rpx;
        width: calc(100% - 36rpx);
        left: 50%;
        transform: translateX(-50%);
        // transform: translateX(18rpx);

        .active-bar {
            position: absolute;
            height: 8rpx;
            background: #1a1a1a;
            left: 0;
            width: 100%;
            border-radius: 4rpx;
        }

        .block {
            position: absolute;
            top: 50%;
            width: 26rpx;
            height: 26rpx;
            background: #fff;
            border: 6rpx solid rgb(26, 26, 26);
            border-radius: 50%;
            transform: translate(-50%, -50%);
            box-sizing: content-box;
            cursor: pointer;
            transition: transform 0.1s ease;

            &:hover {
                transform: translate(-50%, -50%) scale(1.1);
            }

            &:active {
                transform: translate(-50%, -50%) scale(0.95);
            }
        }
    }
}

.price-labels {
    display: flex;
    justify-content: space-between;
    margin-top: 30rpx;
    font-size: 25rpx;
}
</style>
