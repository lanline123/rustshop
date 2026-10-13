<template>
    <div
        v-if="scaleBoth"
        class="responsive-box-wrapper"
        :style="wrapperStyle"
    >
        <div ref="boxRef" class="responsive-box" :style="boxStyle" :class="props.class">
            <slot :scale="scale"></slot>
        </div>
    </div>
    <div
        v-else
        ref="boxRef"
        class="responsive-box"
        :style="boxStyle"
        :class="props.class"
    >
        <slot :scale="scale"></slot>
    </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from "vue";

interface Props {
    /** 最小宽度，单位px，默认700 */
    minWidth?: number;
    /** 基准宽度，单位px，默认1920 */
    baseWidth?: number;
    /** 是否宽高同时缩放，默认false（只缩放宽度） */
    scaleBoth?: boolean;
    /** 自定义样式类名 */
    class?: string;
}

const props = withDefaults(defineProps<Props>(), {
    minWidth: 700,
    baseWidth: 1920,
    scaleBoth: false
});

const boxRef = ref<HTMLElement | null>(null);
const windowWidth = ref<number>(window.innerWidth);
const originalHeight = ref<number>(0);
let resizeObserver: ResizeObserver | null = null;
let lastDevicePixelRatio = window.devicePixelRatio; // 记录上一次的 devicePixelRatio

// 计算缩放比例
const scale = computed(() => {
    if (windowWidth.value <= props.baseWidth) {
        return 1; // 小于等于基准宽度时，不缩放
    }
    return windowWidth.value / props.baseWidth;
});

// 计算实际宽度
const actualWidth = computed(() => {
    if (windowWidth.value <= props.baseWidth) {
        return props.minWidth;
    }
    return props.minWidth * scale.value;
});

// 计算外层容器样式（用于宽高同时缩放时）
const wrapperStyle = computed(() => {
    if (windowWidth.value <= props.baseWidth) {
        return {
            width: `${props.minWidth}px`,
            height: "auto"
        };
    }
    return {
        width: `${actualWidth.value}px`,
        height: `${originalHeight.value * scale.value}px`
    };
});

// 计算盒子样式
const boxStyle = computed(() => {
    const styles: Record<string, string> = {
        minWidth: `${props.minWidth}px`,
        width: `${props.minWidth}px`
    };

    if (windowWidth.value > props.baseWidth) {
        if (props.scaleBoth) {
            // 宽高同时缩放：使用transform
            styles.width = `${props.minWidth}px`;
            styles.transform = `scale(${scale.value})`;
            styles.transformOrigin = "top left";
        } else {
            // 只缩放宽度：直接设置width
            styles.width = `${actualWidth.value}px`;
            // styles.transform = `scale(${scale.value})`;
            // styles.transformOrigin = "top left";
        }
    }

    return styles;
});

// 更新原始高度（用于宽高同时缩放）
const updateOriginalHeight = () => {
    nextTick(() => {
        if (boxRef.value && props.scaleBoth) {
            // 临时移除transform以获取真实高度
            const tempTransform = boxRef.value.style.transform;
            boxRef.value.style.transform = "none";
            originalHeight.value = boxRef.value.offsetHeight;
            boxRef.value.style.transform = tempTransform;
        }
    });
};

// 监听窗口大小变化
const handleResize = () => {
    const currentDevicePixelRatio = window.devicePixelRatio;
    
    // 检测是否是浏览器缩放（devicePixelRatio 变化说明是浏览器缩放）
    if (currentDevicePixelRatio !== lastDevicePixelRatio) {
        // 是浏览器缩放，更新记录但不更新 windowWidth
        lastDevicePixelRatio = currentDevicePixelRatio;
        return;
    }
    
    // 是真正的窗口大小变化，正常更新
    windowWidth.value = window.innerWidth;
    if (props.scaleBoth) {
        updateOriginalHeight();
    }
};

// 监听scaleBoth变化
watch(() => props.scaleBoth, () => {
    if (props.scaleBoth) {
        updateOriginalHeight();
    }
});

onMounted(() => {
    windowWidth.value = window.innerWidth;
    lastDevicePixelRatio = window.devicePixelRatio;
    window.addEventListener("resize", handleResize);

    if (props.scaleBoth) {
        updateOriginalHeight();

        // 使用ResizeObserver监听内容高度变化
        if (boxRef.value) {
            resizeObserver = new ResizeObserver(() => {
                updateOriginalHeight();
            });
            resizeObserver.observe(boxRef.value);
        }
    }
});

onUnmounted(() => {
    window.removeEventListener("resize", handleResize);
    if (resizeObserver) {
        resizeObserver.disconnect();
        resizeObserver = null;
    }
});
</script>

<style lang="less" scoped>
.responsive-box-wrapper {
    position: relative;
    overflow: hidden;
}

.responsive-box {
    position: relative;
    box-sizing: border-box;
}
</style>
