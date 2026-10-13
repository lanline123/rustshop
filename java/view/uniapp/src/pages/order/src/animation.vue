<template>
    <canvas id="myCanvas" class="canvas-content" hidpi canvas-id="myCanvas" @error="handleCanvasError" />
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 加入购物车动画组件
 * - 小球飞入购物车动画效果
 * - 支持H5和小程序端
 */
import { onMounted, ref, computed, onUnmounted, getCurrentInstance } from "vue";
import { useThemeStore } from "@/store/theme";
import { getElementRect } from "@/utils";

const { t } = useI18n();
interface ElementRect {
    width: number;
    height: number;
    left: number;
    top: number;
}

interface CheckPoint {
    x: number;
    y: number;
}

interface AnimationState {
    step: AnimationStep;
    progress: number;
    scale: number;
    checkProgress: number;
}

enum AnimationStep {
    CIRCLE_PROGRESS = 0,
    SCALE_UP = 1,
    SCALE_DOWN = 2,
    CHECK_MARK = 3,
    FINISHED = 4
}

const ANIMATION_CONFIG = {
    PROGRESS_SPEED: 0.03,
    SCALE_UP_SPEED: 0.08,
    SCALE_DOWN_SPEED: 0.04,
    CHECK_SPEED: 0.06,
    MAX_SCALE: 1.1,
    MIN_SCALE: 1,
    FRAME_RATE: 16,
    RADIUS_RATIO: 0.75,
    LINE_WIDTH_RATIO: 0.2,
    CHECK_LINE_WIDTH_RATIO: 0.2,
    STEP_DELAY: {
        SCALE_UP: 50,
        CHECK_MARK: 25
    }
} as const;

const CHECK_COLOR = "#fff";

const instance = getCurrentInstance();

const themeStore = useThemeStore();

const centerX = ref<number>(0);
const centerY = ref<number>(0);
const radius = ref<number>(0);

const animationState = ref<AnimationState>({
    step: AnimationStep.CIRCLE_PROGRESS,
    progress: 0,
    scale: 1,
    checkProgress: 0
});

const checkPoints = computed<CheckPoint[]>(() => [
    { x: -radius.value * 0.3, y: 0 },
    { x: 0, y: radius.value * 0.3 },
    { x: radius.value * 0.6, y: -radius.value * 0.3 }
]);

const canvasSize = computed(() => ({
    width: centerX.value * 2,
    height: centerY.value * 2
}));

const isAnimationRunning = computed(() => animationState.value.step !== AnimationStep.FINISHED);

let context: UniApp.CanvasContext | null = null;
let animationTimer: number | null = null;

const getInteger = (num: number): number => Math.ceil(num);

const roundToThreeDecimals = (num: number): number => Math.round(num * 1000) / 1000;

const startAnimation = (): void => {
    if (animationTimer || !context) return;
    draw();
};

const stopAnimation = (): void => {
    if (animationTimer) {
        clearTimeout(animationTimer);
        animationTimer = null;
    }
};

const resetAnimation = (): void => {
    stopAnimation();
    animationState.value = {
        step: AnimationStep.CIRCLE_PROGRESS,
        progress: 0,
        scale: 1,
        checkProgress: 0
    };

    if (context && canvasSize.value.width > 0) {
        context.clearRect(0, 0, canvasSize.value.width, canvasSize.value.height);
        context.draw(true);
    }
};

const drawCircleProgress = (): void => {
    const { progress } = animationState.value;

    context!.save();
    context!.beginPath();
    context!.arc(centerX.value, centerY.value, radius.value, -Math.PI / 2, -Math.PI / 2 + Math.PI * 2 * progress, false);
    context!.setStrokeStyle(themeStore.themeStyle["--general"]);
    context!.setLineWidth(radius.value * ANIMATION_CONFIG.LINE_WIDTH_RATIO);
    context!.setLineCap("round");
    context!.stroke();
    context!.restore();

    animationState.value.progress += ANIMATION_CONFIG.PROGRESS_SPEED;

    if (animationState.value.progress >= 1) {
        animationState.value.progress = 1;
        setTimeout(() => {
            animationState.value.step = AnimationStep.SCALE_UP;
        }, ANIMATION_CONFIG.STEP_DELAY.SCALE_UP);
    }
};

const drawScaleAnimation = (): void => {
    const { step, scale } = animationState.value;
    const scaleNum = roundToThreeDecimals(scale);

    if (scaleNum < ANIMATION_CONFIG.MAX_SCALE && step === AnimationStep.SCALE_UP) {
        animationState.value.scale += ANIMATION_CONFIG.SCALE_UP_SPEED;
    } else if (scaleNum >= ANIMATION_CONFIG.MAX_SCALE && step === AnimationStep.SCALE_UP) {
        animationState.value.step = AnimationStep.SCALE_DOWN;
    }

    if (step === AnimationStep.SCALE_DOWN) {
        animationState.value.scale -= ANIMATION_CONFIG.SCALE_DOWN_SPEED;
        if (scaleNum <= ANIMATION_CONFIG.MIN_SCALE) {
            animationState.value.scale = ANIMATION_CONFIG.MIN_SCALE;
            setTimeout(() => {
                animationState.value.step = AnimationStep.CHECK_MARK;
            }, ANIMATION_CONFIG.STEP_DELAY.CHECK_MARK);
        }
    }

    context!.save();
    context!.translate(centerX.value, centerY.value);

    if (step === AnimationStep.SCALE_UP) {
        context!.scale(animationState.value.scale, animationState.value.scale);
    }

    context!.beginPath();
    context!.arc(0, 0, radius.value, 0, Math.PI * 2, false);
    context!.setFillStyle(themeStore.themeStyle["--general"]);
    context!.fill();
    context!.restore();
};

const drawCheckMarkPath = (): void => {
    const { checkProgress } = animationState.value;
    const points = checkPoints.value;

    const len1 = Math.hypot(points[1].x - points[0].x, points[1].y - points[0].y);
    const len2 = Math.hypot(points[2].x - points[1].x, points[2].y - points[1].y);
    const totalLen = len1 + len2;
    const drawLen = totalLen * checkProgress;

    context!.beginPath();
    context!.moveTo(points[0].x, points[0].y);

    if (drawLen <= len1) {
        const t = drawLen / len1;
        context!.lineTo(points[0].x + (points[1].x - points[0].x) * t, points[0].y + (points[1].y - points[0].y) * t);
    } else {
        context!.lineTo(points[1].x, points[1].y);
        const t = (drawLen - len1) / len2;
        context!.lineTo(points[1].x + (points[2].x - points[1].x) * t, points[1].y + (points[2].y - points[1].y) * t);
    }
};

const drawCheckMark = (): void => {
    context!.save();
    context!.translate(centerX.value, centerY.value);
    context!.beginPath();
    context!.arc(0, 0, radius.value, 0, Math.PI * 2, false);
    context!.setFillStyle(themeStore.themeStyle["--general"]);
    context!.fill();
    context!.restore();

    context!.save();
    context!.translate(centerX.value * 0.9, centerY.value);
    context!.setStrokeStyle(CHECK_COLOR);
    context!.setLineWidth(radius.value * ANIMATION_CONFIG.CHECK_LINE_WIDTH_RATIO);
    context!.setLineCap("round");

    drawCheckMarkPath();

    context!.stroke();
    context!.restore();

    animationState.value.checkProgress += ANIMATION_CONFIG.CHECK_SPEED;

    if (animationState.value.checkProgress >= 1) {
        animationState.value.checkProgress = 1;
        animationState.value.step = AnimationStep.FINISHED;
    }
};

const draw = (): void => {
    if (!context) {
        console.error("画布上下文未初始化");
        return;
    }

    context.clearRect(0, 0, canvasSize.value.width, canvasSize.value.height);

    switch (animationState.value.step) {
        case AnimationStep.CIRCLE_PROGRESS:
            drawCircleProgress();
            break;
        case AnimationStep.SCALE_UP:
        case AnimationStep.SCALE_DOWN:
            drawScaleAnimation();
            break;
        case AnimationStep.CHECK_MARK:
            drawCheckMark();
            break;
        case AnimationStep.FINISHED:
            stopAnimation();
            return;
        default:
            console.warn("未知动画步骤", animationState.value.step);
            return;
    }

    context.draw(true);

    if (isAnimationRunning.value) {
        animationTimer = setTimeout(draw, ANIMATION_CONFIG.FRAME_RATE);
    }
};

const handleCanvasError = (error: any): void => {
    console.error(error);
    stopAnimation();
};

const handleInitError = (error: Error): void => {
    console.error("动画初始化失败", error);
    stopAnimation();
};

const initializeCanvas = async (): Promise<void> => {
    try {
        const res = (await getElementRect(".canvas-content", instance)) as ElementRect;

        if (!res || !res.width || !res.height) {
            throw new Error(t("无法获取画布尺寸"));
        }
        centerX.value = getInteger(res.width / 2);
        centerY.value = getInteger(res.height / 2);
        radius.value = getInteger(Math.min(res.width, res.height) / 2) * ANIMATION_CONFIG.RADIUS_RATIO;

        context = uni.createCanvasContext("myCanvas", instance);

        if (!context) {
            throw new Error(t("无法创建画布上下文"));
        }

        startAnimation();
    } catch (error) {
        handleInitError(error as Error);
    }
};

onMounted(async () => {
    setTimeout(() => {
        initializeCanvas();
    }, 300);
});

onUnmounted(() => {
    stopAnimation();
});

defineExpose({
    resetAnimation,
    startAnimation,
    stopAnimation
});
</script>

<style scoped>
.canvas-content {
    width: 44rpx;
    height: 44rpx;
    display: block;
}
</style>
