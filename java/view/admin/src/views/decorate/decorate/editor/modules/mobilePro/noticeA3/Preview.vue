<template>
    <div
        class="module-ad-con module-notice-a3"
        :style="
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <div
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundColor +
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.paddingTopBottom +
                commonStyle.moduleContentStyle.paddingLeftRight +
                wrapStyle
            "
        >
            <div class="notice-a3-empty" v-if="!module.noticeList || module.noticeList.length === 0">
                <div class="desc">点击添加公告内容</div>
            </div>
            <div v-else class="notice-a3-wrap">
                <div class="notice-a3-header" v-if="module.showHeader === 1 && module.headerImage?.picUrl">
                    <img :src="imageFormat(module.headerImage.picUrl)" alt="" :style="headerImgStyle" />
                    <div class="notice-a3-divider"></div>
                </div>
                <div class="notice-a3-body" :style="bodyStyle">
                    <div class="notice-a3-scroll">
                        <transition name="notice-slide">
                            <div class="notice-a3-item" :key="currentIndex">
                                <span v-if="module.showPrefix === 1 && currentItem?.prefix" class="notice-a3-prefix">{{ currentItem.prefix }}</span>
                                <span class="notice-a3-text" :style="textStyle">{{ currentItem?.title }}</span>
                            </div>
                        </transition>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref, onMounted, onUnmounted, watch } from "vue";
import { mergeDefaultModule, formatCommonStyle } from "@/views/decorate/decorate/src/modules/index";
import { imageFormat } from "@/utils/format";
import { noticeA3Defaults } from "./index";
import type { NoticeA3Module } from "./types";

const props = defineProps<{ module: NoticeA3Module }>();
const module = props.module;

// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, noticeA3Defaults);

const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {});
});

const noticeList = computed(() => module.noticeList || []);

const currentIndex = ref(0);
const currentItem = computed(() => noticeList.value[currentIndex.value] || noticeList.value[0]);

let timer: ReturnType<typeof setInterval> | null = null;

const startAutoplay = () => {
    stopAutoplay();
    if (noticeList.value.length <= 1) return;
    timer = setInterval(() => {
        currentIndex.value = (currentIndex.value + 1) % noticeList.value.length;
    }, 3000);
};

const stopAutoplay = () => {
    if (timer) {
        clearInterval(timer);
        timer = null;
    }
};

watch(() => noticeList.value.length, () => {
    currentIndex.value = 0;
    startAutoplay();
});

onMounted(() => startAutoplay());
onUnmounted(() => stopAutoplay());

const wrapStyle = computed(() => {
    const styles: string[] = [];
    if (module.showBorder === 1 && module.borderColor) {
        styles.push(`border: 1px solid ${module.borderColor};`);
    }
    return styles.join("");
});

const bodyStyle = computed(() => {
    if (module.contentBgColor) {
        return `background-color: ${module.contentBgColor};`;
    }
    return "";
});

const headerImgStyle = computed(() => {
    return `width: ${module.headerImageWidth}px; height: auto;`;
});

const textStyle = computed(() => {
    const color = module.textColor || "#666666";
    return `color: ${color};`;
});
</script>
<style lang="less" scoped>
.module-notice-a3 {
    min-height: auto !important;

    .module-ad-content {
        overflow: hidden;
    }
}

.notice-a3-empty {
    padding: 20px 0;
    text-align: center;
    color: #999;
    font-size: 14px;
}

.notice-a3-wrap {
    display: flex;
    align-items: center;
    overflow: hidden;
}

.notice-a3-header {
    display: flex;
    align-items: center;
    margin-right: 8px;
    max-height: 20px;
    overflow: hidden;

    img {
        height: auto;
        display: block;
        object-fit: cover;
    }
}

.notice-a3-divider {
    width: 1px;
    height: 12px;
    background-color: #e2e8f0;
    margin-left: 8px;
    margin-right: 2px;
    flex-shrink: 0;
}

.notice-a3-body {
    flex: 1;
    overflow: hidden;
}

.notice-a3-scroll {
    overflow: hidden;
    position: relative;
    height: 30px;
}

.notice-a3-item {
    display: flex;
    align-items: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 13px;
    flex-shrink: 0;
    height: 30px;
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
}

.notice-slide-enter-active,
.notice-slide-leave-active {
    transition: all 0.5s ease;
}

.notice-slide-enter-from {
    transform: translateY(100%);
}

.notice-slide-leave-to {
    transform: translateY(-100%);
}

.notice-a3-prefix {
    display: inline-block;
    border: 1px solid #de872a;
    border-radius: 2px;
    font-size: 12px;
    margin-right: 4px;
    flex-shrink: 0;
    font-weight: 500;
    width: 36px;
    height: 20px;
    line-height: 18px;
    text-align: center;
    overflow: hidden;
    background-color: #fff0cf;
    color: #de872a;
}

.notice-a3-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

</style>
