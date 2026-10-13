<template>
    <div
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
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
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding +
                commonStyle.moduleContentStyle.backgroundColor
            "
        >
            <div class="imagetext">
                <div class="image-box" :style="`border: ${module.picBorder}px solid ${module.picBorderColor}; border-radius: ${module.picRadius}px`">
                    <img class="img" :src="imageFormat(module.picUrl?.picUrl)" alt="" />
                </div>
                <div class="intro-box">
                    <div
                        class="title"
                        :style="`color: ${module.titColor}; text-align:  ${module.titTextAlignment}; margin-bottom: ${module.titMarginBottom}px`"
                    >
                        {{ module.title }}
                    </div>
                    <div class="intro" :style="`color: ${module.descColor}; text-align: ${module.descTextAlignment}`">
                        {{ module.description }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatCommonStyle } from "@/views/decorate/decorate/src/modules/index";
import { imageFormat } from "@/utils/format";
import { imageTextComponentDefaults } from "./index";
import type { ImageTextComponentModule } from "./types";

const props = defineProps<{ module: ImageTextComponentModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, imageTextComponentDefaults);
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
</script>
<style lang="less" scoped>
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}
.image-box {
    overflow: hidden;
    .img {
        width: 100%;
        display: block;
    }
}

.intro-box {
    padding: 15px 10px 5px;
    .title {
        font-size: 20px;
        font-weight: 700;
    }
    .intro {
        font-size: 14px;
        line-height: 24px;
        width: 100%;
        overflow: hidden;
        white-space: pre-wrap;
    }
}
</style>
