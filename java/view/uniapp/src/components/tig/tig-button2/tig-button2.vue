<template>
    <button
        class="tig-button2 reset-button flex justify-center align-center"
        :class="{ disabled, theme, plain, activeBg: !animation }"
        hover-class="none"
        :hover-start-time="Number(hoverStartTime)"
        :hover-stay-time="Number(hoverStayTime)"
        :form-type="formType"
        :open-type="openType"
        :app-parameter="appParameter"
        :hover-stop-propagation="hoverStopPropagation"
        @getphonenumber="$emit('getphonenumber', $event)"
        @getuserinfo="$emit('getuserinfo', $event)"
        @error="$emit('error', $event)"
        @opensetting="$emit('opensetting', $event)"
        @launchapp="$emit('launchapp', $event)"
        @agreeprivacyauthorization="$emit('agreeprivacyauthorization', $event)"
        @tap="handleClick"
        @touchstart="handleTouchStart"
        @touchend="handleTouchEnd"
    >
        <slot />
        <view v-show="showRipple" class="ripple" :style="{ ...rippleStyle, ...rippleBg }" />
    </button>
</template>

<script setup lang="ts">
import { ref, reactive, getCurrentInstance, computed, onMounted, watchEffect, onUnmounted } from "vue";
import { useThemeStore } from "@/store/theme";
import { getElementRect } from "@/utils";

// #ifdef MP-WEIXIN
defineOptions({
    options: { virtualHost: true, styleIsolation: "shared" }
});
// #endif

const props = defineProps({
    hoverStartTime: {
        type: [String, Number],
        default: 50
    },
    hoverStayTime: {
        type: [String, Number],
        default: 600
    },
    formType: {
        type: String,
        default: ""
    },
    openType: {
        type: String,
        default: ""
    },
    appParameter: {
        type: String,
        default: ""
    },
    hoverStopPropagation: {
        type: Boolean,
        default: false
    },
    disabled: {
        type: Boolean,
        default: false
    },
    loading: {
        type: Boolean,
        default: false
    },
    theme: {
        type: Boolean,
        default: false
    },
    plain: {
        type: Boolean,
        default: false
    },
    rippleBgColor: {
        type: String,
        default: "rgba(0, 0, 0, 0.2)"
    },
    animation: {
        type: Boolean,
        default: true
    },
    delay: {
        type: [String, Number],
        default: 0
    }
});

const emit = defineEmits(["getphonenumber", "getuserinfo", "error", "opensetting", "launchapp", "agreeprivacyauthorization", "click"]);

const themeStore = useThemeStore();

const rippleBg = computed(() => {
    let bg = props.rippleBgColor;

    if (props.theme || props.plain) {
        if (props.theme && !props.plain) {
            bg = "rgba(255, 255, 255, 0.2)";
        } else if (props.plain) {
            bg = themeStore.hexToRgba(themeStore.themeStyle["--general"], 0.1);
        }
    }

    return { background: bg };
});

const showRipple = ref(false);
const rippleStyle = reactive({
    left: "0px",
    top: "0px",
    transform: "scale(0)",
    width: "0px",
    height: "0px",
    marginLeft: "0px",
    marginTop: "0px",
    opacity: "1"
});

let currentClickEvent: Event | null = null;

let timeoutIds: number[] = [];

const instance = getCurrentInstance();

const buttonRect = ref<any>(null);

const initButtonRect = async () => {
    try {
        const rect = await getElementRect(".tig-button2", instance);
        buttonRect.value = rect;
    } catch (error) {
        console.warn("Failed to get button rect:", error);
    }
};

onMounted(() => {
    const timeoutId = setTimeout(initButtonRect, Number(props.delay) + 16);
    timeoutIds.push(timeoutId);
});

let rippleStartTime = 0;
const RIPPLE_DURATION = 500;

const createRipple = async (x: number, y: number) => {
    if (!buttonRect.value || buttonRect.value.width === 0 || buttonRect.value.height === 0) {
        console.warn("Button rect not available");
        await initButtonRect();
        return;
    }

    rippleStartTime = Date.now();

    const maxRadius = Math.sqrt(Math.pow(Math.max(x, buttonRect.value.width - x), 2) + Math.pow(Math.max(y, buttonRect.value.height - y), 2));

    rippleStyle.left = `${x}px`;
    rippleStyle.top = `${y}px`;
    rippleStyle.width = `${maxRadius * 2}px`;
    rippleStyle.height = `${maxRadius * 2}px`;
    rippleStyle.marginLeft = `${-maxRadius}px`;
    rippleStyle.marginTop = `${-maxRadius}px`;
    rippleStyle.transform = "scale(0)";
    rippleStyle.opacity = "1";

    showRipple.value = true;

    const timeoutId = setTimeout(() => {
        rippleStyle.transform = "scale(1)";
    }, 16);
    timeoutIds.push(timeoutId);
};

const handleTouchStart = (event: TouchEvent) => {
    if (!props.animation) {
        return;
    }

    if (props.disabled || props.loading || showRipple.value) return;

    if (buttonRect.value) {
        const touch = event.touches[0];
        const x = touch.clientX - buttonRect.value.left;
        const y = touch.clientY - buttonRect.value.top;

        createRipple(x, y);
    }
};

const handleTouchEnd = () => {
    if (!props.animation) {
        return;
    }

    if (props.disabled || props.loading || !showRipple.value) return;

    const currentTime = Date.now();
    const elapsedTime = currentTime - rippleStartTime;
    const halfDuration = RIPPLE_DURATION / 2;

    const remainingTime = Math.max(0, halfDuration - elapsedTime);

    const timeoutId = setTimeout(() => {
        rippleStyle.opacity = "0";

        const fadeTimeoutId = setTimeout(() => {
            showRipple.value = false;
        }, 200);

        timeoutIds.push(fadeTimeoutId);
    }, remainingTime);

    timeoutIds.push(timeoutId);
};

const handleClick = (event: any) => {
    if (props.disabled || props.loading) return;

    if (!props.animation) {
        return emit("click", event);
    }

    if (!showRipple.value && buttonRect.value) {
        const touch = event.touches[0];
        const x = touch.clientX - buttonRect.value.left;
        const y = touch.clientY - buttonRect.value.top;
        createRipple(x, y);

        const timeoutId = setTimeout(() => {
            rippleStyle.opacity = "0";

            emit("click", event);

            const fadeTimeoutId = setTimeout(() => {
                showRipple.value = false;
            }, 200);

            timeoutIds.push(fadeTimeoutId);
        }, RIPPLE_DURATION / 2);

        timeoutIds.push(timeoutId);
    } else {
        currentClickEvent = event;
    }
};

const stop = watchEffect(() => {
    if (!showRipple.value && currentClickEvent) {
        emit("click", currentClickEvent);
        currentClickEvent = null;
    }
});

if (!props.animation) {
    stop();
}

const clearAllTimeouts = () => {
    timeoutIds.forEach((id) => clearTimeout(id));
    timeoutIds = [];
};

onUnmounted(() => {
    clearAllTimeouts();
});
</script>

<style lang="scss">
button {
    color: rgb(26, 26, 26);
    border-color: rgb(224, 225, 229);
    border-radius: 4px;
    border-style: solid;
    border-width: 1px;
    padding: 3px 9px;
    font-size: 24.5rpx;
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
    line-height: normal;
    background: none;

    &.disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    &.activeBg:active {
        &::after {
            background-color: rgba(0, 0, 0, 0.2);
        }
    }

    .ripple {
        position: absolute;
        border-radius: 50%;
        pointer-events: none;
        z-index: 1;

        will-change: transform;
        transform: translateZ(0);

        transition:
            transform 0.5s cubic-bezier(0.4, 0, 0.2, 1),
            opacity 0.2s ease-out;
    }

    &.theme .ripple {
        background-color: rgba(255, 255, 255, 0.3);
    }

    &.plain .ripple {
        background-color: rgba(0, 0, 0, 0.5);
    }
}

button {
    &.theme {
        background-color: var(--main-bg);
        color: var(--main-text);
        border-color: var(--main-bg);
    }
    &.plain {
        background-color: transparent;
        color: var(--main-bg);
    }
}
</style>
