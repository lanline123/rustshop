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
                                            <span :style="allFormat.moreColor">{{ $t('更多') }}</span>
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
import { ref, computed, Ref } from "vue";
import { imageFormat } from "@/utils/format";
import { ModuleType, ModuleImageType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultTitle } from "@/views/decorate/decorate/src/modules/";
const module = defineModel<ModuleType & ModuleImageType>("module") as Ref<ModuleType & ModuleImageType>;

const defaultModule = ref({
    frame: defaultFrame,
    title: defaultTitle
});
mergeDefaultModule(module.value, defaultModule.value);
const { frame } = module.value;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const allFormat = computed(() => {
    return {
        descColor: `color: ${module?.value?.title?.descColor};`,
        moreColor: `color: ${module?.value?.title?.moreColor};`,
        titleColor: `color: ${module?.value?.title?.titleColor};font-weight: ${module?.value?.title?.titleWeight || "normal"};`
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
