<template>
    <!-- 始终渲染同一结构，仅靠 class 切换横滚能力：避免 v-if 切换时画布整棵卸载重建 -->
    <div class="canvas-viewport" :class="{ 'canvas-viewport--active': enabled }">
        <!-- 横滚只发生在 scroller：外层 padding 形成静态安全槽位，画布滚不进槽位，槽位里的浮层不会盖住画布 -->
        <div ref="viewportRef" class="canvas-viewport__scroller" @scroll.passive="onScroll" @wheel="onWheel">
            <slot />
        </div>
        <!-- 槽位轨道（模块标签/操作条）：位于滚动容器之外，不随画布横移 -->
        <slot name="rails" />
    </div>

    <!-- 拖拽条 teleport 到 body：脱离 perfect-scrollbar 滚动流，保证 fixed 吸底稳定 -->
    <!-- 显隐规则：画布 100% 即完整可见（宽屏）时整条隐藏不打扰；可滚动或已缩放时必须显示 ——
         缩到完整可见后 zoom !== 1 仍保留缩放控件，否则无法缩放回去 -->
    <Teleport to="body">
        <div v-if="enabled && (scrollable || zoom !== 1)" class="canvas-viewport__bar">
            <!-- 平移组不随 scrollable 增删（避免胶囊长度跳变），无需滚动时整组置灰禁用 -->
            <div class="canvas-viewport__glass-capsule" :class="{ 'is-dragging': isDragging }">
                <button
                    class="canvas-viewport__btn"
                    title="向左平移"
                    :disabled="!scrollable"
                    @click="scrollStep(-240)"
                >
                    <svg width="12" height="12" viewBox="0 0 16 16" fill="currentColor">
                        <path d="M10.5 3.5L5 8l5.5 4.5v-9z" />
                    </svg>
                </button>

                <div
                    ref="trackRef"
                    class="canvas-viewport__track"
                    :class="{ 'is-disabled': !scrollable }"
                    @pointerdown="onTrackPointerDown"
                >
                    <div class="canvas-viewport__groove" />
                    <div
                        class="canvas-viewport__thumb"
                        :style="thumbStyle"
                        @pointerdown.stop="onThumbPointerDown"
                    >
                        <div class="canvas-viewport__handle">
                            <span />
                            <span />
                            <span />
                        </div>
                    </div>
                </div>

                <button
                    class="canvas-viewport__btn"
                    title="向右平移"
                    :disabled="!scrollable"
                    @click="scrollStep(240)"
                >
                    <svg width="12" height="12" viewBox="0 0 16 16" fill="currentColor">
                        <path d="M5.5 3.5L11 8l-5.5 4.5v-9z" />
                    </svg>
                </button>

                <div class="canvas-viewport__divider" />

                <button
                    class="canvas-viewport__btn"
                    title="缩小"
                    :disabled="zoom <= ZOOM_MIN"
                    @click="zoomBy(-ZOOM_STEP)"
                >
                    <svg width="12" height="12" viewBox="0 0 16 16" fill="currentColor">
                        <path d="M3 7.25h10v1.5H3z" />
                    </svg>
                </button>

                <button class="canvas-viewport__btn canvas-viewport__zoom-value" title="恢复 100%" @click="setZoom(1)">
                    {{ zoomPercent }}
                </button>

                <button
                    class="canvas-viewport__btn"
                    title="放大"
                    :disabled="zoom >= ZOOM_MAX"
                    @click="zoomBy(ZOOM_STEP)"
                >
                    <svg width="12" height="12" viewBox="0 0 16 16" fill="currentColor">
                        <path d="M7.25 3h1.5v4.25H13v1.5H8.75V13h-1.5V8.75H3v-1.5h4.25V3z" />
                    </svg>
                </button>

                <div class="canvas-viewport__divider" />

                <button
                    class="canvas-viewport__btn canvas-viewport__btn--center"
                    title="缩放至画布完整可见"
                    @click="fitWidth"
                >
                    <svg width="13" height="13" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3.5 8h9M6 5.5L3.5 8 6 10.5M10 5.5L12.5 8 10 10.5" />
                    </svg>
                    <span class="canvas-viewport__btn-text">适配</span>
                </button>
            </div>
        </div>
    </Teleport>
</template>

<script lang="ts" setup>
import { ref, computed, watch, nextTick, onUnmounted } from "vue";

interface Props {
    /** 是否启用横向视口（仅 PC 新版首页画布开启，其他装修模式下是透明包装层） */
    enabled?: boolean;
    /** 画布基准宽度（缩放 100% 时的自然宽度），「适配」按 视口宽/基准宽 求缩放比 */
    baseWidth?: number;
    /** 画布缩放比：由父级以 CSS zoom 施加到画布窗口上，本组件只负责控件与滚动锚定 */
    zoom?: number;
}

const props = withDefaults(defineProps<Props>(), {
    enabled: false,
    baseWidth: 1600,
    zoom: 1
});

const emit = defineEmits<{
    (e: "update:zoom", value: number): void;
}>();

const viewportRef = ref<HTMLElement | null>(null);
const trackRef = ref<HTMLElement | null>(null);
const clientW = ref(0);
const scrollW = ref(0);
const scrollLeft = ref(0);
const isDragging = ref(false);

const scrollable = computed(() => scrollW.value > clientW.value + 1);

const thumbStyle = computed(() => {
    if (!scrollW.value || !clientW.value) return {};
    const maxScroll = scrollW.value - clientW.value;
    if (maxScroll <= 0) return { width: "100%", left: "0%" };

    const ratio = clientW.value / scrollW.value;
    const widthPercent = Math.max(ratio * 100, 12);
    const scrollProgress = Math.max(0, Math.min(1, scrollLeft.value / maxScroll));
    const leftPercent = scrollProgress * (100 - widthPercent);

    return {
        width: `${widthPercent}%`,
        left: `${leftPercent}%`
    };
});

const updateMetrics = () => {
    const el = viewportRef.value;
    if (!el) return;
    clientW.value = el.clientWidth;
    scrollW.value = el.scrollWidth;
    scrollLeft.value = el.scrollLeft;
};

const onScroll = () => {
    const el = viewportRef.value;
    if (el) scrollLeft.value = el.scrollLeft;
};

let resizeObserver: ResizeObserver | null = null;
watch(
    viewportRef,
    (el) => {
        resizeObserver?.disconnect();
        resizeObserver = null;
        if (el) {
            resizeObserver = new ResizeObserver(updateMetrics);
            resizeObserver.observe(el);
            if (el.firstElementChild) resizeObserver.observe(el.firstElementChild);
            updateMetrics();
        }
    },
    { flush: "post" }
);

onUnmounted(() => {
    resizeObserver?.disconnect();
    resizeObserver = null;
});

// 首次变为可滚动时默认居中展示画布（无居中按钮，仅初始定位）
watch(scrollable, (val) => {
    if (!val || !props.enabled) return;
    nextTick(() => {
        const el = viewportRef.value;
        if (!el) return;
        el.scrollTo({ left: (el.scrollWidth - el.clientWidth) / 2 });
    });
});

const scrollStep = (delta: number) => {
    const el = viewportRef.value;
    if (!el) return;
    el.scrollBy({ left: delta, behavior: "smooth" });
};

const onThumbPointerDown = (e: PointerEvent) => {
    const el = viewportRef.value;
    const track = trackRef.value;
    if (!el || !track) return;
    e.preventDefault();
    isDragging.value = true;

    const thumbEl = e.currentTarget as HTMLElement;
    const startX = e.clientX;
    const startScrollLeft = el.scrollLeft;

    const trackWidth = track.clientWidth;
    const thumbWidth = thumbEl.clientWidth;
    const maxThumbMove = trackWidth - thumbWidth;
    const maxScroll = el.scrollWidth - el.clientWidth;

    const ratio = maxThumbMove > 0 && maxScroll > 0 ? maxScroll / maxThumbMove : 1;

    const onMove = (ev: PointerEvent) => {
        el.scrollLeft = startScrollLeft + (ev.clientX - startX) * ratio;
    };

    const onUp = (ev: PointerEvent) => {
        isDragging.value = false;
        thumbEl.releasePointerCapture(ev.pointerId);
        thumbEl.removeEventListener("pointermove", onMove);
        thumbEl.removeEventListener("pointerup", onUp);
    };

    thumbEl.setPointerCapture(e.pointerId);
    thumbEl.addEventListener("pointermove", onMove);
    thumbEl.addEventListener("pointerup", onUp);
};

const onTrackPointerDown = (e: PointerEvent) => {
    const el = viewportRef.value;
    const track = trackRef.value;
    if (!el || !track) return;
    e.preventDefault();
    const rect = track.getBoundingClientRect();
    const clickRatio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    const maxScroll = el.scrollWidth - el.clientWidth;
    el.scrollTo({ left: clickRatio * maxScroll, behavior: "smooth" });
};

// —— 画布缩放 ——
const ZOOM_MIN = 0.5;
const ZOOM_MAX = 1.5;
const ZOOM_STEP = 0.1;

const zoomPercent = computed(() => `${Math.round(props.zoom * 100)}%`);

/**
 * 设置缩放并锚定滚动：把锚点（默认视口水平中心）换算到画布自然坐标系，
 * 缩放生效（父级 re-render 应用 zoom）后按新比例还原 scrollLeft，避免缩放瞬间画面横跳。
 */
const setZoom = (next: number, anchorClientX?: number) => {
    const val = Math.round(Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, next)) * 100) / 100;
    if (val === props.zoom) return;
    const el = viewportRef.value;
    if (!el) {
        emit("update:zoom", val);
        return;
    }
    const anchorOffset = anchorClientX != null ? anchorClientX - el.getBoundingClientRect().left : el.clientWidth / 2;
    const naturalX = (el.scrollLeft + anchorOffset) / props.zoom;
    emit("update:zoom", val);
    nextTick(() => {
        el.scrollLeft = naturalX * val - anchorOffset;
        updateMetrics();
    });
};

const zoomBy = (delta: number, anchorClientX?: number) => setZoom(props.zoom + delta, anchorClientX);

/** 适配：缩放到画布横向完整可见（向下取整 1% 防止边缘残留 1px 横滚） */
const fitWidth = () => {
    const el = viewportRef.value;
    if (!el) return;
    setZoom(Math.floor((el.clientWidth / props.baseWidth) * 100) / 100);
};

// 进入页面时 100% 放不下画布则自动适配一次：enabled 取决于异步装修数据，clientW 需等布局完成，
// 两者都就绪才是有效判定时机；判定后不再接管，窗口缩放与用户手动缩放均保持原样
let autoFitResolved = false;
watch([() => props.enabled, clientW], ([on, width]) => {
    if (autoFitResolved || !on || width <= 0) return;
    autoFitResolved = true;
    if (width < props.baseWidth) fitWidth();
});

/** Ctrl/Cmd + 滚轮（含触摸板捏合）缩放，以指针位置为锚点 */
const onWheel = (e: WheelEvent) => {
    if (!props.enabled || (!e.ctrlKey && !e.metaKey)) return;
    e.preventDefault();
    zoomBy(e.deltaY > 0 ? -0.05 : 0.05, e.clientX);
};
</script>

<style lang="less" scoped>
.canvas-viewport--active .canvas-viewport__scroller {
    overflow-x: auto;
    overflow-y: hidden;
    scrollbar-width: none;

    &::-webkit-scrollbar {
        display: none;
    }
}

.canvas-viewport__bar {
    position: fixed;
    left: 395px;
    right: 500px;
    bottom: 18px;
    z-index: 100;
    display: flex;
    justify-content: center;
    pointer-events: none;
}

.canvas-viewport__glass-capsule {
    pointer-events: auto;
    display: flex;
    align-items: center;
    gap: 6px;
    height: 38px;
    padding: 0 8px;
    border-radius: 19px;
    background: rgba(255, 255, 255, 0.68);
    backdrop-filter: blur(24px) saturate(200%);
    -webkit-backdrop-filter: blur(24px) saturate(200%);
    border: 1px solid rgba(255, 255, 255, 0.85);
    box-shadow:
        0 12px 32px -4px rgba(0, 0, 0, 0.12),
        0 4px 12px -2px rgba(0, 0, 0, 0.06),
        inset 0 1px 1px rgba(255, 255, 255, 0.95),
        inset 0 -1px 2px rgba(0, 0, 0, 0.02);
    transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);

    &:hover {
        background: rgba(255, 255, 255, 0.78);
        border-color: rgba(255, 255, 255, 0.95);
        box-shadow:
            0 16px 36px -4px rgba(0, 0, 0, 0.15),
            0 6px 16px -2px rgba(0, 0, 0, 0.08),
            inset 0 1px 1.5px rgba(255, 255, 255, 1);
    }

    &.is-dragging {
        background: rgba(255, 255, 255, 0.85);
        box-shadow:
            0 20px 40px -4px rgba(0, 0, 0, 0.18),
            0 8px 20px -2px rgba(0, 0, 0, 0.1),
            inset 0 1px 1.5px rgba(255, 255, 255, 1);
    }
}

.canvas-viewport__btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    // 空间不足时按钮不收缩换行，收缩量全部让给轨道
    flex-shrink: 0;
    white-space: nowrap;
    height: 26px;
    min-width: 26px;
    padding: 0 6px;
    border: none;
    border-radius: 13px;
    background: transparent;
    color: rgba(31, 35, 41, 0.65);
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.18s ease;

    &:hover {
        background: rgba(0, 0, 0, 0.06);
        color: rgba(31, 35, 41, 0.9);
    }

    &:active {
        background: rgba(0, 0, 0, 0.1);
        transform: scale(0.95);
    }

    &:disabled {
        color: rgba(31, 35, 41, 0.25);
        cursor: not-allowed;
        background: transparent;
        transform: none;
    }
}

.canvas-viewport__zoom-value {
    // 固定宽度：50%~150% 数字位数变化时不抖动相邻按钮
    width: 44px;
    padding: 0;
    font-variant-numeric: tabular-nums;
}

.canvas-viewport__btn-text {
    line-height: 1;
    font-size: 11px;
}

.canvas-viewport__divider {
    width: 1px;
    height: 14px;
    background: rgba(0, 0, 0, 0.08);
    margin: 0 2px;
}

.canvas-viewport__track {
    position: relative;
    width: clamp(140px, 16vw, 260px);
    flex-shrink: 1;
    min-width: 120px;
    height: 22px;
    border-radius: 11px;
    background: rgba(0, 0, 0, 0.04);
    box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.08);
    cursor: pointer;
    display: flex;
    align-items: center;
    padding: 0 2px;
    overflow: hidden;

    &.is-disabled {
        pointer-events: none;
        opacity: 0.45;
    }
}

.canvas-viewport__groove {
    position: absolute;
    left: 8px;
    right: 8px;
    height: 4px;
    border-radius: 2px;
    background: rgba(0, 0, 0, 0.05);
    box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.06);
    pointer-events: none;
}

.canvas-viewport__thumb {
    position: absolute;
    top: 2px;
    bottom: 2px;
    border-radius: 9px;
    background: linear-gradient(180deg, #ffffff 0%, #f2f4f8 100%);
    border: 1px solid rgba(255, 255, 255, 0.9);
    box-shadow:
        0 2px 6px rgba(0, 0, 0, 0.12),
        0 1px 2px rgba(0, 0, 0, 0.08),
        inset 0 1px 0 rgba(255, 255, 255, 1),
        inset 0 -1px 1px rgba(0, 0, 0, 0.04);
    cursor: grab;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.15s, box-shadow 0.15s, transform 0.12s;

    &:hover {
        background: linear-gradient(180deg, #ffffff 0%, #e8ecf3 100%);
        box-shadow:
            0 4px 10px rgba(0, 0, 0, 0.16),
            0 1px 3px rgba(0, 0, 0, 0.12),
            inset 0 1px 0 rgba(255, 255, 255, 1);

        .canvas-viewport__handle span {
            background: rgba(31, 35, 41, 0.45);
        }
    }

    &:active {
        cursor: grabbing;
        background: linear-gradient(180deg, #f0f3f8 0%, #dfe4ee 100%);
        box-shadow:
            0 1px 3px rgba(0, 0, 0, 0.2),
            inset 0 1px 2px rgba(0, 0, 0, 0.08);

        .canvas-viewport__handle span {
            background: rgba(31, 35, 41, 0.65);
        }
    }
}

.canvas-viewport__handle {
    display: flex;
    align-items: center;
    gap: 3px;
    pointer-events: none;

    span {
        width: 1.5px;
        height: 8px;
        border-radius: 1px;
        background: rgba(31, 35, 41, 0.25);
        transition: background 0.15s;
    }
}
</style>
