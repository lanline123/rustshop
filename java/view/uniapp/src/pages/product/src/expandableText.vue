<template>
    <view :id="containerId" class="expandable-text">
        <template v-if="isOverflow">
            <text>{{ firstPart }}</text>
            <text v-if="!isExpanded" class="expand-icon iconfont-h5 icon-up rotate" @click.stop="toggleExpand" />
            <text v-if="isExpanded" class="rest-part fade-in">{{ restPart }}</text>
            <text v-if="isExpanded" class="expand-icon iconfont-h5 icon-up" @click.stop="toggleExpand" />
        </template>
        <template v-else>
            <text>{{ text }}</text>
        </template>
        <!-- 隐藏的 canvas 用于测量文字宽度 -->
        <canvas v-if="showCanvas" :id="canvasId" :canvas-id="canvasId" class="measure-canvas" />
    </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick, getCurrentInstance } from "vue";

const props = defineProps({
    text: {
        type: String,
        default: ""
    },
    maxLines: {
        type: Number,
        default: 2
    },
    fontSize: {
        type: Number,
        default: 32 // rpx
    },
    offsetWidth: {
        type: Number,
        default: 0 // rpx，用于扣除同行其他元素（如标签）的宽度
    }
});

const instance = getCurrentInstance()?.proxy;
const containerId = `container-${Date.now()}`;
const canvasId = `canvas-${Date.now()}`;
const isExpanded = ref(false);
const isOverflow = ref(false);
const firstPart = ref("");
const restPart = ref("");
const showCanvas = ref(false);

// 图标宽度用 upx2px 估算即可，稍微留点余量
const iconWidth = uni.upx2px(32);

const toggleExpand = () => {
    isExpanded.value = !isExpanded.value;
};

// 使用 canvas 测量文字宽度并判断是否溢出、计算截断位置
const measureText = (containerWidth: number, fontSize: number, offsetWidth: number) => {
    const ctx = uni.createCanvasContext(canvasId, instance);
    ctx.setFontSize(fontSize);

    // 第一行需要扣除 offsetWidth
    const firstLineWidth = containerWidth - offsetWidth;
    const maxWidth = firstLineWidth + containerWidth * (props.maxLines - 1);

    // 测量文字总宽度
    let totalWidth = 0;
    const charWidths: number[] = [];
    for (const char of props.text) {
        const w = ctx.measureText(char).width;
        charWidths.push(w);
        totalWidth += w;
    }

    // 判断是否溢出
    const needOverflow = totalWidth > maxWidth;
    isOverflow.value = needOverflow;

    if (needOverflow) {
        // 计算截断位置（留出图标宽度和间距）
        const targetWidth = maxWidth - iconWidth;
        let currentWidth = 0;
        let splitIndex = props.text.length;

        for (let i = 0; i < charWidths.length; i++) {
            if (currentWidth + charWidths[i] > targetWidth) {
                splitIndex = i;
                break;
            }
            currentWidth += charWidths[i];
        }

        firstPart.value = props.text.slice(0, splitIndex);
        restPart.value = props.text.slice(splitIndex);
    }

    // 测量完成后销毁 canvas
    showCanvas.value = false;
};

const checkOverflow = () => {
    showCanvas.value = true;
    nextTick(() => {
        const query = uni.createSelectorQuery().in(instance);
        query.select(`#${containerId}`).fields({ size: true, computedStyle: ["fontSize"] }, () => {});

        query.exec((results: any[]) => {
            const containerRes = results[0];

            if (!containerRes || containerRes.width === 0) {
                setTimeout(checkOverflow, 100);
                return;
            }

            const fontSize = parseFloat(containerRes.fontSize) || uni.upx2px(props.fontSize);
            const offsetWidth = uni.upx2px(props.offsetWidth);
            measureText(containerRes.width, fontSize, offsetWidth);
        });
    });
};

watch(
    () => props.text,
    () => {
        isExpanded.value = false;
        isOverflow.value = false;
        checkOverflow();
    },
    { immediate: true }
);

onMounted(() => {
    checkOverflow();
});
</script>

<style lang="scss" scoped>
.expandable-text {
    display: inline;
    word-break: break-all;

    .expand-icon {
        display: inline;
        font-size: 28rpx;
        line-height: 28rpx;
        margin-left: 4rpx;
        vertical-align: baseline;

        &.rotate {
            display: inline-block;
            transform: rotate(180deg);
        }
    }

    .rest-part {
        &.fade-in {
            animation: fadeIn 0.3s ease-in-out;
        }
    }

    .measure-canvas {
        position: fixed;
        left: -9999px;
        top: -9999px;
        opacity: 0;
        pointer-events: none;
        width: 1px;
        height: 1px;
    }
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}
</style>
