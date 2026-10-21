<template>
    <view class="base-image-box">
        <template v-if="!isError">
            <image
                class="base-image"
                :class="{ 'loading-end': !isLoading }"
                :src="srcData"
                :mode="mode"
                @error="handleError"
                @load="handleLoad"
                @click="showBigPic"
            />
        </template>
        <template v-if="!isLoading && isError">
            <image class="base-image empty" :src="staticResource('common/empty-img-bg3.png')" :mode="mode" />
        </template>
    </view>
</template>

<script setup lang="ts">
// #ifdef MP-WEIXIN
defineOptions({
    options: { virtualHost: true }
});
// #endif
import { onUnmounted, ref, watch } from "vue";
import { imageFormat } from "@/utils/format";
import { staticResource } from "@/utils";
import { useConfigStore } from "@/store/config";

const props = defineProps({
    src: {
        type: String,
        default: "",
        required: true
    },
    mode: {
        type: String,
        default: "widthFix"
    },
    preview: {
        type: Boolean,
        default: false
    }
});

const configStore = useConfigStore();

const isError = ref(false);

const isLoading = ref(true);

const emit = defineEmits(["error", "load"]);

const srcData = ref("");

// 最近一次加载成功的最终地址。@load 只在 src 真正变化时才会再来一次，
// 所以「这张图已经显示出来了」这个事实必须自己记住，不能靠事件重放。
const loadedSrc = ref("");

// 兜底时长：超时后即便没收到 @load 也结束 loading。图片未就绪时元素本就是空白，
// 提前放开 opacity 不会露出异常内容，但能避免事件丢失导致整张图永久不可见。
const LOAD_TIMEOUT = 3000;

let loadTimer: ReturnType<typeof setTimeout> | null = null;

const clearLoadTimer = () => {
    if (loadTimer) {
        clearTimeout(loadTimer);
        loadTimer = null;
    }
};

const restartLoadTimer = () => {
    clearLoadTimer();
    loadTimer = setTimeout(() => {
        loadTimer = null;
        if (isLoading.value && !isError.value) {
            isLoading.value = false;
        }
    }, LOAD_TIMEOUT);
};

/**
 * 刷新首屏常见竞态：分类/商品接口先于 initConfig 返回相对路径，
 * 此时 storageUrl 仍为空；域名就绪后需重拼。
 * 注意：storageUrl 变化时，已经显示出来的图不能退回 loading 态，
 * 否则同地址不触发 @load，会永久卡在 opacity:0。
 */
watch(
    [() => props.src, () => configStore.storageUrl],
    ([newVal, storageUrl]) => {
        if (!newVal) {
            clearLoadTimer();
            srcData.value = "";
            loadedSrc.value = "";
            isError.value = true;
            isLoading.value = false;
            return;
        }
        // 相对路径且域名未就绪：先不请求，等 storageUrl 到位再拼
        if (!String(newVal).includes("http") && !storageUrl) {
            clearLoadTimer();
            isError.value = false;
            isLoading.value = true;
            srcData.value = "";
            return;
        }
        const next = imageFormat(newVal);
        // 同一地址已经加载成功过：直接恢复展示态。O2O 首页会被定位与门店接口拖到 1s 后
        // 才写入 storageUrl，此时图片早已加载完成，走下面的重置分支就再也亮不回来了。
        if (next === srcData.value && next === loadedSrc.value) {
            clearLoadTimer();
            isError.value = false;
            isLoading.value = false;
            return;
        }
        // 地址没变且上次没失败：维持当前加载态，等 @load 收尾
        if (next === srcData.value && !isError.value) {
            return;
        }
        isError.value = false;
        isLoading.value = true;
        srcData.value = next;
        loadedSrc.value = "";
        restartLoadTimer();
    },
    {
        immediate: true
    }
);

const handleLoad = (e: Event) => {
    clearLoadTimer();
    loadedSrc.value = srcData.value;
    isLoading.value = false;
    isError.value = false;
    emit("load", e);
};

const handleError = (e: Event) => {
    clearLoadTimer();
    isError.value = true;
    isLoading.value = false;
    emit("error", e);
};

onUnmounted(() => {
    clearLoadTimer();
});

const showBigPic = () => {
    if (props.preview) {
        uni.previewImage({
            urls: [srcData.value],
            indicator: "none",
            loop: false
        });
    }
};
</script>

<style lang="scss" scoped>
.base-image-box {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    .base-image {
        width: 100%;
        height: 100%;
        display: block;
        opacity: 0;
        transition: opacity 0.4s ease-in-out;

        &.loading-end {
            opacity: 1;
        }

        &.empty {
            opacity: 1;
        }
    }
}
</style>
