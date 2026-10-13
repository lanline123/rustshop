<template>
    <div :class="'module-ad-con module-image_hotarea module-image_ad'" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius">
            <div class="module-ad-empty empty-image_ad" v-if="module.picList?.length == 0">
                <div class="image-empty-bg"></div>
                <div class="desc">{{ $t('点击添加热图绘制') }}</div>
            </div>
            <CommonTitle v-if="title?.showTitle" v-model="title"></CommonTitle>
            <div class="image-ad-warp image-hotarea-warp" :style="frameFormat.innerPadding">
                <div class="image-ad-con" ref="containerRef">
                    <div v-for="(pic, index) in module.picList" :key="index" class="image-ad-item">
                        <div class="item-content" :style="format.imgPadding">
                            <div class="item-content-hotatrea">
                                <img class="item-img-bg" :alt="pic.picTitle" :src="imageFormat(pic.picUrl)" style="display: block" />
                                <div
                                    v-for="(picHotarea, idx) in pic.hotarea"
                                    :key="idx"
                                    :href="picHotarea.link"
                                    class="image-hotarea-link"
                                    :style="picHotarea.position"
                                >
                                    <span class="area_box_con_text">{{ picHotarea.link?.name }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted, watch, Ref } from "vue";
import { imageFormat } from "@/utils/format";
import { ModuleType, ModuleImageHotareaType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultTitle, CommonTitle } from "@/views/decorate/decorate/src/modules/";
// 在modules加入要使用的模块
const module = defineModel<ModuleType & ModuleImageHotareaType>("module") as Ref<ModuleType & ModuleImageHotareaType>;
const defaultModule = ref({
    picList: [],
    imgPadding: 0,
    picRadioStyle: 1,
    frame: defaultFrame,
    title: defaultTitle
});
mergeDefaultModule(module.value, defaultModule.value);
const { frame, title } = module.value;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const format = computed(() => {
    return {
        imgPadding: "padding:" + module.value.imgPadding + "px;"
    };
});

const containerRef = ref<HTMLDivElement>();
const containerWidth = ref(0);
const updateWidth = () => {
    if (containerRef.value) {
        containerWidth.value = containerRef.value.offsetWidth;
    }
    module.value.picList?.forEach((pics) => {
        if (pics.hotarea) {
            pics.hotarea.forEach((picHotarea) => {
                picHotarea.position =
                    "width:" +
                    resize(picHotarea.width ?? 0) +
                    "px;height:" +
                    resize(picHotarea.height ?? 0) +
                    "px;top:" +
                    resize(picHotarea.top ?? 0) +
                    "px" +
                    ";left:" +
                    resize(picHotarea.left ?? 0) +
                    "px";
            });
        }
    });
};
const resize = (size: number) => {
    return (containerWidth.value / 500) * size; //500为绘制区域原始宽度px
};
// 监听容器宽度的变化
watch(module.value, updateWidth, { immediate: true });
onMounted(async () => {
    updateWidth();
    window.addEventListener("resize", updateWidth);
});
onUnmounted(() => {
    window.removeEventListener("resize", updateWidth);
});
</script>
