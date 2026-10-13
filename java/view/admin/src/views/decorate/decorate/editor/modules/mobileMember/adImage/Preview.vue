<template>
    <div class="module-ad-con module-member-ad-image" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="ad-image-wrap" :style="wrapStyle">
                <div class="ad-image-container" :style="containerStyle">
                    <template v-if="!module.pic?.picUrl">
                        <div class="module-ad-empty">
                            <div class="image-empty-bg"></div>
                            <div class="desc">点击添加图片</div>
                        </div>
                    </template>
                    <template v-else>
                        <img class="ad-image-pic" :src="module.pic.picUrl" alt="广告图" />
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { adImageDefaults } from "./index";
import type { AdImageModule } from "./types";

const props = defineProps<{ module: AdImageModule }>();
const module = props.module;

const defaultInnerPadding = 0;
mergeDefaultModule(module, adImageDefaults);

const frame = computed(() => module.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));

const wrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "transparent",
    borderRadius: `${frame.value.boxRadius ?? 0}px`
}));

const containerStyle = computed(() => ({
    padding: `${frame.value.innerPadding ?? defaultInnerPadding}px`
}));
</script>
<style lang="less" scoped>
.ad-image-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.ad-image-container {
    box-sizing: border-box;
}
.ad-image-pic {
    width: 100%;
    display: block;
}
</style>
