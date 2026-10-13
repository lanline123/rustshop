<template>
    <div
        :class="'module-ad-con module-title_ad ad-title_align__' + module?.title?.titleAlign"
        :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom"
    >
        <div class="module-ad-content" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
            <div class="title-ad-warp" :style="paddingStyle.paddingLeftRight">
                <div class="title-ad-con">
                    <div class="title-ad-item">
                        <div class="item-content">
                            <div :class="'image-ad-title ad-title_style__' + module?.title?.titleStyle">
                                <div class="image-ad-title-l">
                                    <div class="image-ad-title-h" :style="allFormat.titleColor">{{ module?.title?.titleText }}</div>
                                    <div class="image-ad-title-d" :style="allFormat.descColor">{{ module?.title?.descText }}</div>
                                </div>
                                <template v-if="module?.title?.showMore === 1">
                                    <slot>
                                        <div class="image-ad-title-r">
                                            <span :style="allFormat.moreColor">更多</span>
                                            <i class="iconfont-h5 icon-youjiantou" :style="allFormat.moreColor"></i>
                                        </div>
                                    </slot>
                                </template>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { titleAdDefaults } from "./index";
import type { TitleAdModule } from "./types";

const props = defineProps<{ module: TitleAdModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, titleAdDefaults);
const { frame } = module;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const allFormat = computed(() => {
    return {
        descColor: `color: ${module?.title?.descColor};`,
        moreColor: `color: ${module?.title?.moreColor};`,
        titleColor: `color: ${module?.title?.titleColor};font-weight: ${module?.title?.titleWeight || "normal"};`
    };
});

const paddingStyle = computed(() => {
    return {
        paddingLeftRight: `padding-left: ${frame?.leftAndRightPadding}px;padding-right: ${frame?.leftAndRightPadding}px;`
    };
});
</script>

<style scoped>
.modules-item .module-ad-con {
    min-height: 0;
}

.image-ad-title-r {
    font-size: 12px;
}
</style>
