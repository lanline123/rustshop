<template>
    <view class="navbar-box" :style="navbarBoxStyle">
        <template v-if="needStatusBar">
            <view class="status-bar" :style="{ paddingTop: `${statusBarTop}px` }" />
        </template>

        <view v-if="needStatusBar" class="navbar flex" :style="{ height: `${navbarHeight}px`, paddingRight: `${paddingRight}px` }">
            <slot />
        </view>
        <view v-else class="navbar flex" :style="{ height: `${navbarHeight}px` }">
            <slot />
        </view>
    </view>
    <template v-if="fixed && placeholder">
        <view class="placeholder" :style="{ paddingTop: `calc(${needStatusBar ? configStore.safeAreaInsets.top : 0}px + ${navbarHeight}px)` }" />
    </template>
</template>

<script setup lang="ts">
import { useConfigStore } from "@/store/config";
import { computed } from "vue";
import { getPx } from "@/utils";
import { onShow } from "@dcloudio/uni-app";

const props = defineProps({
    height: {
        type: [Number, String],
        default: 45
    },
    fixed: {
        type: Boolean,
        default: false
    },
    background: {
        type: String,
        default: "transparent"
    },
    placeholder: {
        type: Boolean,
        default: true
    },
    needStatusBar: {
        type: Boolean,
        default: true
    },
    /** fixed 时相对视口的 top 偏移（如海外版 mainHeader 高度），单位 px */
    offsetTop: {
        type: Number,
        default: 0
    },
    /** 覆盖默认层级；购物车顶栏等需压在 mainHeader 下时可传入较小值 */
    zIndex: {
        type: [Number, String],
        default: undefined
    }
});

const navbarBoxStyle = computed(() => {
    const style: Record<string, string> = {
        position: props.fixed ? "fixed" : "static",
        background: props.background
    };
    if (props.fixed) {
        style.top = `${props.offsetTop || 0}px`;
    }
    if (props.zIndex !== undefined && props.zIndex !== null && props.zIndex !== "") {
        style.zIndex = String(props.zIndex);
    }
    return style;
});

const configStore = useConfigStore();

const statusBarTop = computed(() => {
    if (configStore.menuButtonInfo.top) {
        return configStore.menuButtonInfo.top - 2;
    }
    return configStore.safeAreaInsets.top;
});

const navbarHeight = computed(() => {
    let heightNum = getPx(props.height);

    if (configStore.menuButtonInfo.top) {
        heightNum = configStore.menuButtonInfo.height + 4;
    }

    return heightNum;
});

const paddingRight = computed(() => {
    return configStore.menuButtonInfo.width ? configStore.menuButtonInfo.width + 5 : 0;
});

onShow(() => {
    configStore.getMenuButtonInfo();
    configStore.getSystemInfo();
});

defineExpose({
    navbarHeight
});
</script>

<style lang="scss" scoped>
.placeholder {
    width: 100%;
    height: 0;
}

.navbar-box {
    top: 0;
    z-index: 999000;
    width: 100%;

    .status-bar {
        width: 100%;
        height: 0;
    }

    .navbar {
        width: 100%;
        align-items: center;
        justify-content: center;
    }
}
</style>
