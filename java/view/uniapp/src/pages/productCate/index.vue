<template>
    <tig-layout>
        <template v-if="decorateLoaded">
            <template v-if="effectiveStyleType === 1">
                <styleOneCate v-model:cat-id="catId" :module="moduleConfigV1" />
            </template>
            <template v-if="effectiveStyleType === 2">
                <styleTwoCate :height="height" :module="moduleConfigV2" />
            </template>
            <template v-if="effectiveStyleType === 3">
                <styleThreeCate :height="height" :module="moduleConfigV2" />
            </template>
        </template>
    </tig-layout>
</template>

<script lang="ts" setup>
/**
 * 商品分类页
 * - 优先读后台分类页装修（pageType=4）：cateCenterV1 / cateCenterV2
 * - 未配置时降级到全局 configStore.categoryDecorateType
 */
import { onLoad, onShow } from "@dcloudio/uni-app";
import { ref, computed } from "vue";
import styleOneCate from "./styleOneCate.vue";
import styleTwoCate from "./styleTwoCate.vue";
import styleThreeCate from "./styleThreeCate.vue";
import { setPageMeta } from "@/utils/seo";
import { useConfigStore } from "@/store/config";
import { useTabbarStore } from "@/store/tabbar";

import { getH5CategoryDecorate } from "@/api/home/home";
import { handleDecorateLoadError } from "@/utils/handleDecorateLoadError";

import type { CateCenterV1Data } from "@/components/modules/cate-center/cate-center-v1/types";
import type { CateCenterV2Data } from "@/components/modules/cate-center/cate-center-v2/types";
import { cateCenterV1Defaults } from "@/components/modules/cate-center/cate-center-v1/config";
import { cateCenterV2Defaults } from "@/components/modules/cate-center/cate-center-v2/config";

const configStore = useConfigStore();

const tabbarStore = useTabbarStore();

const catId = ref(0);
const previewId = ref<string | number>("");

const moduleConfigV1 = ref<CateCenterV1Data>({ ...cateCenterV1Defaults });
const moduleConfigV2 = ref<CateCenterV2Data>({ ...cateCenterV2Defaults });

const decorateStyleType = ref<number | null>(null);
const decorateLoaded = ref(false);

const effectiveStyleType = computed(() => {
    return decorateStyleType.value ?? configStore.categoryDecorateType;
});

/**
 * 把后台 JSON 反序列化产生的"true"/"false"/"1"/"0" 等字符串值统一转回真实类型。
 * 后端 module 字段以 JSON 字符串存储，反序列化后所有原生 boolean/number 都会变成字符串，
 * 在前端使用前必须规范化，避免类似 `showRecommend !== false` 这种判断失效。
 */
const normalizeModuleValues = (raw: Record<string, any> | undefined | null): Record<string, any> => {
    if (!raw || typeof raw !== "object") return {};
    const result: Record<string, any> = {};
    Object.keys(raw).forEach((key) => {
        const val = raw[key];
        if (typeof val === "string") {
            if (val === "true") {
                result[key] = true;
            } else if (val === "false") {
                result[key] = false;
            } else if (val !== "" && !isNaN(Number(val)) && /^-?\d+(\.\d+)?$/.test(val)) {
                result[key] = Number(val);
            } else {
                result[key] = val;
            }
        } else {
            result[key] = val;
        }
    });
    return result;
};

/**
 * 拉取分类页装修配置
 * 接口：home/home/h5Category，预览模式通过 previewId 参数区分
 */
const fetchCategoryDecorate = async () => {
    try {
        const result: any = await getH5CategoryDecorate(previewId.value);
        const moduleList: any[] = result?.moduleList || result?.data?.moduleList || [];

        for (const m of moduleList) {
            let data: any = m?.module;
            if (typeof data === "string") {
                try {
                    data = JSON.parse(data);
                } catch {
                    // ignore
                }
            }
            const normalized = normalizeModuleValues(data);
            if ((m?.type === "cate-center-v1" || m?.type === "cateCenterV1") && Object.keys(normalized).length > 0) {
                moduleConfigV1.value = { ...cateCenterV1Defaults, ...normalized };
                decorateStyleType.value = 1;
            }
            if ((m?.type === "cate-center-v2" || m?.type === "cateCenterV2") && Object.keys(normalized).length > 0) {
                moduleConfigV2.value = { ...cateCenterV2Defaults, ...normalized };
                decorateStyleType.value = normalized.styleType ?? 2;
            }
        }
    } catch (error) {
        if (await handleDecorateLoadError(error)) return;
        console.error("获取分类页装修配置失败:", error);
    } finally {
        decorateLoaded.value = true;
    }
};

onLoad((options) => {
    previewId.value = options?.previewId ?? "";
    if (options && options.categoryId) {
        catId.value = options.categoryId;
    }
    fetchCategoryDecorate();
    // #ifdef H5
    setPageMeta({
        title: `商品分类 - ${configStore.baseInfo.shopTitle}`,
        description: `浏览${configStore.baseInfo.shopTitle}的全部商品分类`,
        type: "website"
    });
    // #endif
});

onShow(() => {
    uni.hideTabBar({
        fail: () => {}
    });
});

const height = computed(() => {
    return `calc(${configStore.windowInfo.screenHeight}px - var(${tabbarStore.currentActiveValue > -1 ? "--tabbar-height" : "--safe-bottom"}) - var(--nav-height))`;
});
</script>
<style>
/* 兼容小程序 */
page {
    background-color: #fff !important;
}
</style>
